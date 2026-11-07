---
name: CampusLibrary
description: Bound in Ink — an editorial library design for MVC administrators and Android readers.
colors:
  shell: "#30243E"
  ground: "#F4F1F7"
  surface: "#FFFFFF"
  ink: "#252130"
  muted: "#625A6D"
  accent: "#354CB0"
  accent-hover: "#263984"
  border: "#D9D2E2"
  available-text: "#205244"
  available-background: "#E2F0E9"
  loan-text: "#634611"
  loan-background: "#F5E9C9"
  danger: "#A02845"
typography:
  web-heading: "Source Serif 4 SemiBold, Georgia, serif"
  android-heading: "Native Material typography with FontFamily.Serif"
  controls: "Platform-native sans-serif"
rounded:
  web: "0.2rem"
  android: "4–8dp"
---

# Bound in Ink — CampusLibrary Design System

## Identity
CampusLibrary is a practical campus library catalog and circulation tool, not a generic dashboard. **Bound in Ink** is its distinctive visual language: an aubergine masthead, pale lilac reading surfaces, editorial serif headings, ink-blue actions, compact identifiers, and explicit written loan states.

The two clients share an identity but are designed for their different contexts: staff need tabular scanning and efficient forms; Android readers need readable book details, availability and trustworthy borrowing feedback. Avoid generic dashboard cards, synthetic book covers, gradients, unnecessary shadows and promotional hero layouts.

This document describes the **implemented** system in `src/CampusLibrary.WebMvc/wwwroot/css/site.css`, `android/CampusLibraryApp/app/src/main/java/com/campus/library/MainActivity.kt`, and the Compose screens. It supersedes the former bright-blue Bootstrap design description. See `.impeccable/library-surfaces.md` for the visual direction and `.impeccable/review/web/` for MVC screenshot evidence.

## Color and typography

| Purpose | MVC web | Android Compose |
|---|---|---|
| Brand/navigation | Aubergine `#30243E` | Light secondary container `#30243E` |
| Canvas | Lilac `#F4F1F7` | Light `#F4F1F7`, dark `#19151F` |
| Content | White `#FFFFFF` | Theme surface: light white, dark `#231D2B` |
| Body text | Ink `#252130` | Theme onSurface/onBackground |
| Actions | Ink-blue `#354CB0` | Theme primary (adaptive to dark mode) |
| Supporting text | `#625A6D` | Theme onSurfaceVariant |
| Loan state | Green Available, gold On Loan, red Overdue | Written availability/overdue labels with semantic theme colors |

Self-hosted **Source Serif 4 SemiBold** is used for web titles and brand (with OFL license in `wwwroot/fonts/`). Android uses its native serif typography for headings and Material sans-serif for supporting copy and controls, allowing native text scaling.

## MVC administration (Razor and Bootstrap)
- Aubergine navigation holds the wordmark and Books / Members / Loans; the current page is underlined.
- A page starts with an editorial serif heading, concise task context and relevant action, not a dashboard hero.
- White bordered catalog tables emphasize titles, authors and identifiers. Book rows show Available or On Loan in text, alongside Edit/Delete controls.
- Members and open loans stay easy to scan; due dates and overdue statuses are legible and ordered.
- Create/edit/delete use a stable white form surface, visible labels, meaningful validation feedback and confirmation before destructive actions.
- Controls have at least 44px touch height; long fields wrap and focus remains visible. A skip link, `aria-current`, table headings and reduced-motion CSS are retained.
- At widths under 768px, navigation collapses and tables become labeled stacked records rather than requiring a desktop-wide table.
- The layout uses a 1320px max container, desktop 2rem gutters and mobile 1.25rem gutters. Small radii, subtle borders, no decorative shadows.

## Android reader (Jetpack Compose)
- The Material top bar carries Campus Library branding, navigation/refresh, and visible `Demo member · #1`. This is a **demonstration identity**, not authentication.
- Catalog rows show full titles, authors, ISBN/year metadata and explicit Available to borrow / On loan status. Unavailable books remain visible and discoverable.
- Book details include title, author, catalog metadata, availability and a borrow action disabled while on loan. Loading/error states provide retry.
- My loans displays the **actual book name and author** returned from the additive API fields `bookTitle` and `bookAuthor`. A numeric `Book #<id>` is a compatibility fallback for older/missing metadata only.
- Successful borrow navigates to My loans with a snackbar confirmation. Return feedback uses existing snackbar behavior; open loans refresh after returns.
- Screens use Material spacing, large touch targets, lifecycle-aware state collection, scrollable content, light/dark palettes and standard Android back semantics.
- Architecture remains Compose → ViewModel + StateFlow → Retrofit repository → Web API; no database operations in screens.

## Accessibility and acceptance
Visual state must always be paired with readable text; do not encode loan status solely through color. Preserve keyboard and mobile tap affordances, visible focus, error recovery and font scaling.

The prior follow-up records 21 browser-rendered MVC states and one Android catalog image. The user-provided My loans emulator screenshot helped identify the numeric-title fallback. Source/design review confirms the intended implementation but **does not substitute for an actual Impeccable executable finish review or a live Android emulator sign-off**.

Before submission, verify catalog, book details and loans on an emulator at normal/large text, portrait/landscape and light/dark; test borrow → title/author → return → reborrow with the updated local API. Document the actual outcome rather than assuming it passes.
