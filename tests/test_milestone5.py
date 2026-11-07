"""MVC form contracts against a disposable SQL database; run via run_milestone5.py."""
import http.cookiejar
from html.parser import HTMLParser
import os
import unittest
import urllib.error
import urllib.parse
import urllib.request
import uuid
from test_milestone4 import request as api_request

MVC = os.environ.get('CAMPUSLIBRARY_MVC_TEST_URL', '').rstrip('/')


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


class FormParser(HTMLParser):
    def __init__(self):
        super().__init__()
        self.tokens = []

    def handle_starttag(self, tag, attributes):
        attrs = dict(attributes)
        if tag == 'input' and attrs.get('name') == '__RequestVerificationToken':
            self.tokens.append(attrs['value'])


@unittest.skipUnless(MVC, 'Run tests/run_milestone5.py to start an isolated MVC app.')
class Milestone5Tests(unittest.TestCase):
    def setUp(self):
        self.client = urllib.request.build_opener(
            urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()), NoRedirect())

    def call(self, method, path, fields=None, status=200):
        data = None if fields is None else urllib.parse.urlencode(fields).encode()
        req = urllib.request.Request(MVC + path, data=data, method=method)
        try:
            response = self.client.open(req, timeout=30)
        except urllib.error.HTTPError as error:
            response = error
        html = response.read().decode()
        self.assertEqual(response.status, status, (method, path, html[:2000]))
        return html, response.headers

    def token(self, path):
        html, _ = self.call('GET', path)
        parser = FormParser()
        parser.feed(html)
        self.assertTrue(parser.tokens, 'Form must contain an anti-forgery token.')
        return parser.tokens[0]

    def post_form(self, form, fields, target=None, status=302):
        token = self.token(form)
        return self.call('POST', target or form,
                         {**fields, '__RequestVerificationToken': token}, status)

    def new_book(self):
        tag = uuid.uuid4().hex[:20]
        status, book, _ = api_request('POST', '/api/books', {'title': 'MVC fixture', 'isbn': tag})
        self.assertEqual(status, 201)
        return book

    def test_navigation_and_empty_states(self):
        _, headers = self.call('GET', '/', status=302)
        self.assertTrue(headers['Location'].endswith('/Books'))
        for path, heading in [('/Books', 'Books'), ('/Members', 'Members'), ('/Loans', 'Open loans')]:
            html, _ = self.call('GET', path)
            self.assertIn(f'<h1>{heading}</h1>', html)
            self.assertIn('aria-current="page"', html)
            self.assertIn('href="#main-content"', html)
        html, _ = self.call('GET', '/Loans')
        # Each test returns its fixture loans; no open loans remain.
        self.assertIn('No open loans', html)

    def test_book_forms_crud_validation_and_encoding(self):
        isbn = uuid.uuid4().hex[:20]
        title = '<script>alert("book")</script>'
        invalid, _ = self.post_form('/Books/Create', {'Title': ' ', 'Isbn': ''}, status=200)
        self.assertIn('field-validation-error', invalid)
        self.post_form('/Books/Create', {'Title': title, 'Isbn': isbn, 'Author': 'Test Author', 'YearPublished': 2024})
        status, books, _ = api_request('GET', '/api/books')
        self.assertEqual(status, 200)
        book = next(b for b in books if b['isbn'] == isbn)
        html, _ = self.call('GET', '/Books')
        self.assertNotIn(title, html)
        self.assertIn('&lt;script&gt;', html)
        duplicate, _ = self.post_form('/Books/Create', {'Title': 'Duplicate', 'Isbn': isbn}, status=200)
        self.assertIn('already exists', duplicate)
        self.post_form(f"/Books/Edit/{book['id']}",
                       {'Title': 'Edited book', 'Isbn': isbn, 'Author': 'Updated', 'YearPublished': 2020})
        status, updated, _ = api_request('GET', f"/api/books/{book['id']}")
        self.assertEqual(updated['title'], 'Edited book')
        self.call('GET', f"/Books/Delete/{book['id']}")
        self.assertEqual(api_request('GET', f"/api/books/{book['id']}")[0], 200)
        self.post_form(f"/Books/Delete/{book['id']}", {})
        self.assertEqual(api_request('GET', f"/api/books/{book['id']}")[0], 404)

    def test_member_creation_validation_and_duplicates(self):
        tag = uuid.uuid4().hex[:12]
        fields = {'Name': 'Library Test Member', 'Email': tag + '@example.com', 'StudentId': tag}
        html, _ = self.post_form('/Members/Create', {**fields, 'Email': 'invalid'}, status=200)
        self.assertIn('field-validation-error', html)
        self.post_form('/Members/Create', fields)
        html, _ = self.call('GET', '/Members')
        self.assertIn(tag, html)
        duplicate, _ = self.post_form('/Members/Create', fields, status=200)
        self.assertIn('already exists', duplicate)

    def test_all_posts_reject_missing_or_invalid_antiforgery(self):
        book = self.new_book()
        before = api_request('GET', f"/api/books/{book['id']}")[1]
        for path, body in [('/Books/Create', {'Title': 'CSRF', 'Isbn': 'csrf'}),
                           (f"/Books/Edit/{book['id']}", {'Title': 'Changed', 'Isbn': before['isbn']}),
                           (f"/Books/Delete/{book['id']}", {}),
                           ('/Members/Create', {'Name': 'CSRF', 'Email': 'csrf@example.com', 'StudentId': 'csrf'}),
                           ('/Loans/Return/1', {})]:
            self.call('POST', path, body, 400)
            self.call('POST', path, {**body, '__RequestVerificationToken': 'forged'}, 400)
        self.assertEqual(api_request('GET', f"/api/books/{book['id']}")[1], before)
        api_request('DELETE', f"/api/books/{book['id']}")

    def test_history_protection_and_return(self):
        book = self.new_book()
        status, loan, _ = api_request('POST', '/api/loans', {'bookId': book['id'], 'memberId': 1})
        self.assertEqual(status, 201)
        html, _ = self.call('GET', '/Loans')
        self.assertIn('MVC fixture', html)
        self.assertIn('Alice Johnson', html)
        catalog, _ = self.call('GET', '/Books')
        self.assertIn('status-on-loan', catalog)
        self.assertIn('On Loan', catalog)
        protected, _ = self.post_form(f"/Books/Delete/{book['id']}", {}, status=200)
        self.assertIn('loan history', protected)
        self.post_form('/Loans', {}, target=f"/Loans/Return/{loan['id']}")
        self.assertIsNotNone(api_request('GET', f"/api/loans/{loan['id']}")[1]['returnDate'])
        catalog, _ = self.call('GET', '/Books')
        self.assertNotIn('status-on-loan', catalog)
        self.assertIn('status-available', catalog)
        self.post_form('/Books/Create', {}, target=f"/Loans/Return/{loan['id']}")
        html, _ = self.call('GET', '/Loans')
        self.assertIn('already been returned', html)
        protected, _ = self.post_form(f"/Books/Delete/{book['id']}", {}, status=200)
        self.assertIn('loan history', protected)

    def test_missing_records_and_route_id_cannot_be_overridden(self):
        for path in ('/Books/Edit/2147483647', '/Books/Delete/2147483647', '/Books/Edit/0'):
            self.call('GET', path, status=404)
        book, other = self.new_book(), self.new_book()
        self.post_form(f"/Books/Edit/{book['id']}",
                       {'id': other['id'], 'Title': 'Route-selected book', 'Isbn': book['isbn']})
        self.assertEqual(api_request('GET', f"/api/books/{book['id']}")[1]['title'], 'Route-selected book')
        self.assertEqual(api_request('GET', f"/api/books/{other['id']}")[1]['title'], 'MVC fixture')
        self.post_form(f"/Books/Delete/{book['id']}", {'id': other['id']})
        self.assertEqual(api_request('GET', f"/api/books/{other['id']}")[0], 200)
        api_request('DELETE', f"/api/books/{other['id']}")
