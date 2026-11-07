"""Real HTTP/MSSQL contract tests. Run only against a disposable test database.

CAMPUSLIBRARY_TEST_URL=http://127.0.0.1:<port> python3 -m unittest discover -s tests -v
The API must use an isolated migrated database. Fixtures intentionally retain loan history.
"""
import concurrent.futures
import datetime as dt
import json
import os
import unittest
import threading
import urllib.error
import urllib.request
import urllib.parse
import uuid

BASE = os.environ['CAMPUSLIBRARY_TEST_URL'].rstrip('/')


def request(method, path, body=None):
    data = None if body is None else json.dumps(body).encode()
    req = urllib.request.Request(BASE + path, data=data, method=method,
                                 headers={'Content-Type': 'application/json'})
    try:
        response = urllib.request.urlopen(req, timeout=30)
    except urllib.error.HTTPError as error:
        response = error
    raw = response.read()
    return response.status, json.loads(raw) if raw else None, response.headers


class Milestone4Tests(unittest.TestCase):
    def call(self, method, path, body=None, status=200):
        actual, value, headers = request(method, path, body)
        self.assertEqual(actual, status, (method, path, value))
        return value, headers

    def member(self):
        tag = uuid.uuid4().hex[:12]
        body = {'name': ' Test Member ', 'email': f'{tag}@example.com', 'studentId': tag}
        member, headers = self.call('POST', '/api/members', body, 201)
        created, _ = self.call("GET", urllib.parse.urlsplit(headers["Location"]).path)
        self.assertEqual(created, member)
        return member

    def book(self):
        book, _ = self.call('POST', '/api/books',
                            {'title': 'Milestone 4 test', 'isbn': uuid.uuid4().hex[:20],
                             'author': 'Campus Test Author'}, 201)
        return book

    def borrow(self, book, member):
        loan, headers = self.call('POST', '/api/loans',
                                  {'bookId': book['id'], 'memberId': member['id']}, 201)
        created, _ = self.call("GET", urllib.parse.urlsplit(headers["Location"]).path)
        self.assertEqual(created, loan)
        return loan

    def test_members_crud_uniqueness_and_registration(self):
        member = self.member()
        self.assertEqual(member['name'], 'Test Member')
        self.assertIsNotNone(member['registeredDate'])
        self.call('GET', f"/api/members/{member['id']}")
        members, _ = self.call('GET', '/api/members')
        self.assertIn(member['id'], [m['id'] for m in members])
        other = self.member()
        update = {**member, 'name': 'Updated'}
        self.call('PUT', f"/api/members/{member['id']}", update, 204)
        got, _ = self.call('GET', f"/api/members/{member['id']}")
        self.assertEqual(got['name'], 'Updated')
        self.assertEqual(got['registeredDate'], member['registeredDate'])
        for field in ('email', 'studentId'):
            duplicate = {**other, field: member[field]}
            self.call('POST', '/api/members', duplicate, 409)
            self.call('PUT', f"/api/members/{other['id']}", duplicate, 409)
        self.call('DELETE', f"/api/members/{member['id']}", status=204)
        self.call('GET', f"/api/members/{member['id']}", status=404)
        self.call('DELETE', f"/api/members/{other['id']}", status=204)

    def test_validation_and_missing_records(self):
        valid = {'name': 'Name', 'email': 'valid@example.com', 'studentId': 'VALID'}
        for patch in ({'name': ' '}, {'email': 'bad'}, {'studentId': ''},
                      {'name': 'x' * 101}, {'studentId': 'x' * 21}, {'email': None}):
            for method, path in [('POST', '/api/members'), ('PUT', '/api/members/1')]:
                problem, _ = self.call(method, path, {**valid, **patch}, 400)
                self.assertEqual(problem['status'], 400)
                self.assertIn('errors', problem)
        for body in ({}, {'bookId': 0, 'memberId': 1}, {'bookId': 1, 'memberId': -1}):
            self.call('POST', '/api/loans', body, 400)
        for path in ('/api/members/0', '/api/loans/-1', '/api/loans?memberId=0',
                     '/api/loans?openOnly=bad'):
            self.call('GET', path, status=400)
        for path in ('/api/members/2147483647', '/api/loans/2147483647'):
            self.call('GET', path, status=404)
        self.call('PUT', '/api/members/2147483647', valid, 404)
        self.call('DELETE', '/api/members/2147483647', status=404)
        self.call('POST', '/api/loans/2147483647/return', status=404)
        self.call('POST', '/api/loans', {'bookId': 2147483647, 'memberId': 1}, 404)
        self.call('POST', '/api/loans', {'bookId': 1, 'memberId': 2147483647}, 404)

    def assert_availability(self, book, available):
        detail, _ = self.call('GET', f"/api/books/{book['id']}")
        books, _ = self.call('GET', '/api/books')
        listed = next(b for b in books if b['id'] == book['id'])
        self.assertEqual(listed, detail)
        self.assertIs(detail['isAvailable'], available)
        for field in ('id', 'title', 'isbn', 'author', 'yearPublished'):
            self.assertEqual(detail[field], book[field])

    def test_development_swagger_ui_and_availability_schema(self):
        with urllib.request.urlopen(BASE + '/swagger/index.html', timeout=30) as response:
            self.assertEqual(response.status, 200)
            html = response.read().decode()
            self.assertIn('src="./swagger-ui-bundle.js"', html)
            self.assertIn('src="index.js"', html)
        with urllib.request.urlopen(BASE + '/swagger/index.js', timeout=30) as response:
            self.assertEqual(response.status, 200)
            script = response.read().decode()
            self.assertIn('SwaggerUIBundle', script)
            self.assertIn('/openapi/v1.json', script)
        document, _ = self.call('GET', '/openapi/v1.json')
        schema = document['components']['schemas']['BookDto']
        self.assertEqual(schema['properties']['isAvailable']['type'], 'boolean')
        self.assertIn('/api/Books', document['paths'])
        self.assertIn('/api/Loans', document['paths'])

    def test_borrow_return_history_filters_and_reborrow(self):
        member, book = self.member(), self.book()
        other = self.book()
        self.assertIs(book['isAvailable'], True)
        self.assert_availability(book, True)
        loan = self.borrow(book, member)
        self.assertEqual(loan['bookTitle'], book['title'])
        self.assertEqual(loan['bookAuthor'], book['author'])
        self.assert_availability(book, False)
        self.assert_availability(other, True)
        self.call('DELETE', f"/api/books/{other['id']}", status=204)
        borrowed = dt.datetime.fromisoformat(loan['borrowDate'].replace('Z', '+00:00'))
        due = dt.datetime.fromisoformat(loan['dueDate'].replace('Z', '+00:00'))
        self.assertEqual(due - borrowed, dt.timedelta(days=14))
        self.assertIsNone(loan['returnDate'])
        got, _ = self.call('GET', f"/api/loans/{loan['id']}")
        self.assertEqual(got, loan)
        self.call('POST', '/api/loans', {'bookId': book['id'], 'memberId': member['id']}, 409)
        self.call('DELETE', f"/api/members/{member['id']}", status=409)
        self.call('DELETE', f"/api/books/{book['id']}", status=409)
        loans, _ = self.call('GET', f"/api/loans?memberId={member['id']}&openOnly=true")
        self.assertEqual([x['id'] for x in loans], [loan['id']])
        self.assertEqual(loans[0]['bookTitle'], book['title'])
        self.assertEqual(loans[0]['bookAuthor'], book['author'])
        self.call('POST', f"/api/loans/{loan['id']}/return", status=204)
        returned, _ = self.call('GET', f"/api/loans/{loan['id']}")
        self.assertIsNotNone(returned['returnDate'])
        self.assert_availability(book, True)
        self.call('POST', f"/api/loans/{loan['id']}/return", status=409)
        again, _ = self.call('GET', f"/api/loans/{loan['id']}")
        self.assertEqual(again['returnDate'], returned['returnDate'])
        loans, _ = self.call('GET', f"/api/loans?memberId={member['id']}&openOnly=true")
        self.assertEqual(loans, [])
        self.call('DELETE', f"/api/members/{member['id']}", status=409)
        self.call('DELETE', f"/api/books/{book['id']}", status=409)
        new_loan = self.borrow(book, member)
        self.assertNotEqual(new_loan['id'], loan['id'])
        self.assert_availability(book, False)
        history, _ = self.call('GET', f"/api/loans?memberId={member['id']}")
        self.assertEqual(len(history), 2)
        self.call('POST', f"/api/loans/{new_loan['id']}/return", status=204)

    def test_concurrent_borrow_and_return(self):
        member, book = self.member(), self.book()
        with concurrent.futures.ThreadPoolExecutor(max_workers=8) as pool:
            results = list(pool.map(lambda _: request('POST', '/api/loans',
                           {'bookId': book['id'], 'memberId': member['id']}), range(8)))
        self.assertEqual(sorted(x[0] for x in results), [201] + [409] * 7)
        loan = next(x[1] for x in results if x[0] == 201)
        with concurrent.futures.ThreadPoolExecutor(max_workers=8) as pool:
            results = list(pool.map(lambda _: request('POST', f"/api/loans/{loan['id']}/return"), range(8)))
        self.assertEqual(sorted(x[0] for x in results), [204] + [409] * 7)

    def test_concurrent_member_duplicates(self):
        tag = uuid.uuid4().hex[:12]
        body = {'name': 'Concurrent', 'email': tag + '@example.com', 'studentId': tag}
        with concurrent.futures.ThreadPoolExecutor(max_workers=8) as pool:
            results = list(pool.map(lambda _: request('POST', '/api/members', body), range(8)))
        self.assertEqual(sorted(x[0] for x in results), [201] + [409] * 7)

    def test_concurrent_member_deletes_return_not_found_instead_of_server_error(self):
        for _ in range(3):
            member = self.member()
            barrier = threading.Barrier(8)

            def delete_member(_):
                barrier.wait(timeout=10)
                return request('DELETE', f"/api/members/{member['id']}")[0]

            with concurrent.futures.ThreadPoolExecutor(max_workers=8) as pool:
                statuses = list(pool.map(delete_member, range(8)))
            self.assertEqual(sorted(statuses), [204] + [404] * 7)

    def test_existing_books_crud(self):
        book = self.book()
        self.call('GET', '/api/books')
        self.call('PUT', f"/api/books/{book['id']}", {**book, 'title': 'Updated'}, 204)
        got, _ = self.call('GET', f"/api/books/{book['id']}")
        self.assertEqual(got['title'], 'Updated')
        self.call('DELETE', f"/api/books/{book['id']}", status=204)
        self.call('GET', f"/api/books/{book['id']}", status=404)


if __name__ == '__main__':
    unittest.main()
