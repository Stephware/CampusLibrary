"""Regression checks for confirmed final-review defects; isolated SQL runner only."""
import os
import unittest
import uuid
from test_milestone4 import request
import test_milestone5 as mvc_tests


class FinalApiReviewTests(unittest.TestCase):
    def test_book_inputs_reject_blank_and_oversized_values_before_sql(self):
        isbn = uuid.uuid4().hex[:20]
        status, book, _ = request('POST', '/api/books', {'title': 'Valid book', 'isbn': isbn})
        self.assertEqual(status, 201)
        try:
            for patch in ({'title': ''}, {'title': ' '}, {'title': 'x' * 201},
                          {'isbn': ''}, {'isbn': ' '}, {'isbn': 'x' * 21},
                          {'author': 'x' * 101}, {'yearPublished': -1}):
                for method, path in [('POST', '/api/books'), ('PUT', f"/api/books/{book['id']}")]:
                    with self.subTest(method=method, patch=patch):
                        actual, problem, _ = request(method, path, {'title': 'Valid book', 'isbn': isbn, **patch})
                        self.assertEqual(actual, 400)
                        self.assertEqual(problem['status'], 400)
                        self.assertIn('errors', problem)
        finally:
            request('DELETE', f"/api/books/{book['id']}")

    def test_api_book_case_only_isbn_edit_does_not_conflict_with_itself(self):
        isbn = 'abc-' + uuid.uuid4().hex[:12]
        status, book, _ = request('POST', '/api/books', {'title': 'Case edit', 'isbn': isbn})
        self.assertEqual(status, 201)
        try:
            self.assertEqual(request('PUT', f"/api/books/{book['id']}",
                                     {'title': 'Edited', 'isbn': isbn.upper()})[0], 204)
            status, updated, _ = request('GET', f"/api/books/{book['id']}")
            self.assertEqual(updated['isbn'], isbn.upper())
            self.assertEqual(request('POST', '/api/books', {'title': 'Duplicate', 'isbn': isbn})[0], 409)
        finally:
            request('DELETE', f"/api/books/{book['id']}")

    def test_api_book_route_ids_must_be_positive(self):
        for method, path, body in [('GET', '/api/books/0', None),
                                   ('PUT', '/api/books/-1', {'title': 'Valid', 'isbn': 'VALID'}),
                                   ('DELETE', '/api/books/0', None)]:
            with self.subTest(method=method):
                status, problem, _ = request(method, path, body)
                self.assertEqual(status, 400)
                self.assertEqual(problem['status'], 400)


@unittest.skipUnless(mvc_tests.MVC, 'Run tests/run_milestone5.py for MVC review checks.')
class FinalMvcReviewTests(unittest.TestCase):
    def setUp(self):
        self.mvc = mvc_tests.Milestone5Tests()
        self.mvc.setUp()

    def test_mvc_book_case_only_isbn_edit_does_not_conflict_with_itself(self):
        isbn = 'abc-' + uuid.uuid4().hex[:12]
        status, book, _ = request('POST', '/api/books', {'title': 'MVC case edit', 'isbn': isbn})
        self.assertEqual(status, 201)
        try:
            self.mvc.post_form(f"/Books/Edit/{book['id']}", {'Title': 'Changed', 'Isbn': isbn.upper()})
            self.assertEqual(request('GET', f"/api/books/{book['id']}")[1]['isbn'], isbn.upper())
        finally:
            request('DELETE', f"/api/books/{book['id']}")

    def test_invalid_status_page_codes_return_not_found_instead_of_crashing(self):
        for query in ('', '?code=-1', '?code=204', '?code=9999'):
            with self.subTest(query=query):
                html, _ = self.mvc.call('GET', '/Home/Status' + query, status=404)
                self.assertIn('Page not found', html)
