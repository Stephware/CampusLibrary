"""Read-only CampusLibrary API/MVC smoke check against the running local app.

Never creates, updates, deletes or migrates records. Avoids printing
personal records, connection strings and HTTP response bodies.
"""
import argparse
import json
from pathlib import Path
import sys
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode
from urllib.request import urlopen


def get(url):
    try:
        with urlopen(url, timeout=12) as response:
            if response.status != 200:
                raise RuntimeError(f"HTTP {response.status}")
            return response.read().decode("utf-8")
    except (HTTPError, URLError, TimeoutError, UnicodeError) as exc:
        raise RuntimeError(f"GET failed: {type(exc).__name__}") from exc


def check(name, action, results):
    try:
        note = action()
        results.append({"check": name, "status": "PASS", "detail": str(note)})
    except (RuntimeError, ValueError, TypeError, KeyError, AssertionError) as exc:
        results.append({"check": name, "status": "FAIL", "detail": str(exc)[:250]})


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--api", default="http://127.0.0.1:5143")
    parser.add_argument("--mvc", default="http://127.0.0.1:5080")
    parser.add_argument("--member-id", type=int, default=1)
    parser.add_argument("--report", type=Path, help="Write a non-sensitive JSON result summary")
    args = parser.parse_args()
    api = args.api.rstrip("/")
    mvc = args.mvc.rstrip("/")
    results = []
    data = {}

    def books():
        items = json.loads(get(api + "/api/books"))
        assert isinstance(items, list), "Books response is not an array"
        assert len({x["id"] for x in items}) == len(items), "Duplicate book IDs"
        assert all(isinstance(b.get("isAvailable"), bool) and b.get("title") for b in items), "Book title/availability missing"
        data["books"] = {x["id"]: x for x in items}
        return f"{len(items)} book record(s), each with a title and availability"

    def members():
        items = json.loads(get(api + "/api/members"))
        assert isinstance(items, list), "Members response is not an array"
        assert any(x.get("id") == args.member_id for x in items), "Configured demo member missing"
        return f"Configured demo member ID {args.member_id} is present"

    def loans():
        items = json.loads(get(api + "/api/loans?openOnly=true"))
        assert isinstance(items, list), "Loans response is not an array"
        book_index = data.get("books")
        assert book_index is not None, "Book check must pass first"
        for loan in items:
            book = book_index.get(loan["bookId"])
            assert book is not None, "Open loan references a missing book"
            assert loan.get("bookTitle") == book["title"], "Loan bookTitle is missing or does not match Books"
            assert loan.get("bookAuthor") == book.get("author"), "Loan bookAuthor does not match Books"
            assert loan.get("returnDate") is None, "Open-loan response contains a returned loan"
            assert book["isAvailable"] is False, "Open loan book incorrectly marked Available"
        return f"{len(items)} open loan(s); book names and availability consistent"

    def my_loans():
        query = urlencode({"memberId": args.member_id, "openOnly": "true"})
        items = json.loads(get(api + "/api/loans?" + query))
        assert isinstance(items, list) and all(x.get("memberId") == args.member_id for x in items), "Member filtering incorrect"
        assert all(x.get("bookTitle") for x in items), "Loan missing book title"
        return f"{len(items)} demo member open loan(s), all with book titles"

    def swagger():
        page = get(api + "/swagger/index.html")
        assert "Swagger" in page, "Swagger UI not found"
        doc = json.loads(get(api + "/openapi/v1.json"))
        assert isinstance(doc.get("paths"), dict) and doc["paths"], "OpenAPI paths empty"
        return "Development Swagger UI and OpenAPI document reachable"

    def page(route, expected):
        body = get(mvc + route)
        assert "<h1" in body and expected in body, f"MVC {route} missing expected heading"
        return f"MVC {route} renders with heading"

    check("Books API", books, results)
    check("Demo member", members, results)
    check("Open loan metadata/availability", loans, results)
    check("Demo member loans", my_loans, results)
    check("Development Swagger", swagger, results)
    check("MVC Books", lambda: page("/Books", "Books"), results)
    check("MVC Members", lambda: page("/Members", "Members"), results)
    check("MVC Loans", lambda: page("/Loans", "Open loans"), results)

    report = {"scope": "read-only running API/MVC with configured database", "checks": results,
              "passed": sum(x["status"] == "PASS" for x in results), "total": len(results),
              "disclaimer": "Read-only smoke check; does not test mutations, emulator UI or production security."}
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    for result in results:
        print(f'{result["status"]}: {result["check"]} — {result["detail"]}')
    return 0 if report["passed"] == report["total"] else 1


if __name__ == "__main__":
    sys.exit(main())
