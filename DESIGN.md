---
name: CampusLibrary
description: Bootstrap MVC library administration visual system.
colors:
  accent: "#1859a9"
  accent-hover: "#10447f"
  ink: "#202b3b"
  muted: "#586575"
  ground: "#f5f7fa"
  surface: "#fff"
  border: "#d8dfe8"
  table-heading-bg: "#eef2f7"
  table-heading-text: "#47556a"
  input-border: "#9caabb"
  empty-border: "#acb8c7"
  focus-tint: "#dbe9fb"
  danger: "#a52834"
  error-text: "#842029"
  error-bg: "#f8d7da"
  error-border: "#e7a2a8"
  on-loan-text: "#164f85"
  on-loan-bg: "#e3edf8"
  success-text: "#0a3622"
  success-bg: "#d1e7dd"
  success-border: "#a3cfbb"
  notice-text: "#664d03"
  notice-bg: "#fff3cd"
  notice-border: "#ffe69c"
typography:
  headline:
    fontFamily: "system-ui, -apple-system, \"Segoe UI\", Roboto, \"Helvetica Neue\", \"Noto Sans\", \"Liberation Sans\", Arial, sans-serif, \"Apple Color Emoji\", \"Segoe UI Emoji\", \"Segoe UI Symbol\", \"Noto Color Emoji\""
    fontSize: "2rem"
    fontWeight: 650
    lineHeight: 1.2
    letterSpacing: "-.025em"
  headline-mobile:
    fontFamily: "system-ui, -apple-system, \"Segoe UI\", Roboto, \"Helvetica Neue\", \"Noto Sans\", \"Liberation Sans\", Arial, sans-serif, \"Apple Color Emoji\", \"Segoe UI Emoji\", \"Segoe UI Symbol\", \"Noto Color Emoji\""
    fontSize: "1.75rem"
    fontWeight: 650
    lineHeight: 1.2
    letterSpacing: "-.025em"
  title:
    fontFamily: "system-ui, -apple-system, \"Segoe UI\", Roboto, \"Helvetica Neue\", \"Noto Sans\", \"Liberation Sans\", Arial, sans-serif, \"Apple Color Emoji\", \"Segoe UI Emoji\", \"Segoe UI Symbol\", \"Noto Color Emoji\""
    fontSize: "1.375rem"
    fontWeight: 600
    lineHeight: 1.3
  body:
    fontFamily: "system-ui, -apple-system, \"Segoe UI\", Roboto, \"Helvetica Neue\", \"Noto Sans\", \"Liberation Sans\", Arial, sans-serif, \"Apple Color Emoji\", \"Segoe UI Emoji\", \"Segoe UI Symbol\", \"Noto Color Emoji\""
    fontSize: "1rem"
    fontWeight: 400
    lineHeight: 1.55
  label:
    fontFamily: "system-ui, -apple-system, \"Segoe UI\", Roboto, \"Helvetica Neue\", \"Noto Sans\", \"Liberation Sans\", Arial, sans-serif, \"Apple Color Emoji\", \"Segoe UI Emoji\", \"Segoe UI Symbol\", \"Noto Color Emoji\""
    fontSize: "1rem"
    fontWeight: 600
  small:
    fontFamily: "system-ui, -apple-system, \"Segoe UI\", Roboto, \"Helvetica Neue\", \"Noto Sans\", \"Liberation Sans\", Arial, sans-serif, \"Apple Color Emoji\", \"Segoe UI Emoji\", \"Segoe UI Symbol\", \"Noto Color Emoji\""
    fontSize: ".875rem"
  status:
    fontFamily: "system-ui, -apple-system, \"Segoe UI\", Roboto, \"Helvetica Neue\", \"Noto Sans\", \"Liberation Sans\", Arial, sans-serif, \"Apple Color Emoji\", \"Segoe UI Emoji\", \"Segoe UI Symbol\", \"Noto Color Emoji\""
    fontSize: ".8rem"
    fontWeight: 600
rounded:
  control: ".375rem"
  surface: ".5rem"
  status: ".3rem"
spacing:
  half: ".5rem"
  three-quarter: ".75rem"
  one: "1rem"
  five-quarter: "1.25rem"
  three-half: "1.5rem"
  two: "2rem"
  three: "3rem"
  four: "4rem"
components:
  button-primary:
    backgroundColor: "{colors.accent}"
    textColor: "{colors.surface}"
    rounded: "{rounded.control}"
    padding: ".6rem 1rem"
  button-primary-hover:
    backgroundColor: "{colors.accent-hover}"
    textColor: "{colors.surface}"
  button-outline-primary:
    backgroundColor: "transparent"
    textColor: "{colors.accent}"
    rounded: "{rounded.control}"
    padding: ".5rem .8rem"
  button-outline-secondary:
    backgroundColor: "transparent"
    textColor: "{colors.muted}"
    rounded: "{rounded.control}"
    padding: ".6rem 1rem"
  button-danger:
    backgroundColor: "{colors.danger}"
    textColor: "{colors.surface}"
    rounded: "{rounded.control}"
    padding: ".6rem 1rem"
  button-outline-danger:
    backgroundColor: "transparent"
    textColor: "{colors.danger}"
    rounded: "{rounded.control}"
    padding: ".5rem .8rem"
  input:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.ink}"
    rounded: "{rounded.control}"
    padding: ".375rem .75rem"
  editor-form:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.surface}"
    padding: "{spacing.two}"
  editor-form-mobile:
    padding: "{spacing.five-quarter}"
  nav-link:
    textColor: "{colors.muted}"
    padding: ".65rem .85rem"
  nav-link-current:
    textColor: "{colors.accent}"
  loan-status:
    backgroundColor: "{colors.on-loan-bg}"
    textColor: "{colors.on-loan-text}"
    rounded: "{rounded.status}"
    padding: ".3rem .55rem"
  loan-status-overdue:
    backgroundColor: "{colors.error-bg}"
    textColor: "{colors.error-text}"
  list-surface:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.surface}"
---
# Design System: CampusLibrary

## Overview

CampusLibrary extends the existing Bootstrap MVC starter into a direct library administration surface. Lists and full-page forms carry the work; system typography, white surfaces, muted supporting text and restrained blue actions keep records easy to scan.

This document captures the implemented MVC interface, not an institutional identity. The product name is the only supplied brand asset. There are no image, illustration, logo or composition assets in this visual system.

**Key Characteristics:**
- Lists and forms in the first viewport.
- Flat bordered surfaces on a pale background.
- Native HTML inputs and Bootstrap navigation and button conventions.
- Visible labels, recovery feedback and keyboard focus.
- Desktop tables become labelled stacked records on mobile.

## Colors

The palette uses a restrained blue primary accent, cool neutral structure and semantic feedback colors. The frontmatter is normative; descriptive names below identify their roles rather than establish another palette.

### Primary

- **Accent blue** (`accent`) marks primary actions, links, current navigation and focus. **Deep accent blue** (`accent-hover`) marks primary hover and active states.

### Neutral

- **Ink**, **muted**, **ground**, **surface** and **border** separate readable content, supporting information, page background, white panels and thin dividers.
- **Table heading background/text** distinguish the compact column header row. **Input border** is stronger than panel borders so editable fields remain identifiable. **Empty border** is the dashed empty-state outline.
- **Focus tint** supports field focus and text selection.

### Semantic feedback

- **Danger** identifies destructive actions, invalid input borders and field error text. **Error text/background/border** identify validation summaries; the text/background pair also marks overdue loans.
- **On-loan text/background** identify an open loan without an overdue warning.
- **Success text/background/border** and **notice text/background/border** retain Bootstrap's success and warning alert colors. These are feedback roles, not additional brand accents.

## Typography

The complete system font stack is recorded in the frontmatter and comes from bundled Bootstrap. There is no separate display, decorative or monospaced font.

- **Headline:** page titles use the headline role, changing to headline-mobile below the medium breakpoint. Tight tracking and a stronger weight establish the page without a hero treatment.
- **Title:** secondary headings introduce empty states and supporting sections.
- **Body:** regular system text; page descriptions have a maximum line length of 68ch, empty-state and delete explanations 65ch.
- **Label:** visible form labels and action text are semibold. Table headings, helper text, secondary cell content, counts and footer text use the small scale where implemented.
- **Status:** compact semibold labels identify loan state. Identifiers and dates use tabular numerals; they do not change font family.

## Layout

The shell uses Bootstrap's full-width centered container, with the application stylesheet overriding its maximum width to 1200px at every breakpoint. Desktop gutters retain Bootstrap's .75rem horizontal container padding; the mobile rule below replaces that padding. The header, main content and footer share that alignment. The body fills at least the viewport and the main region flexes to keep the footer below short content.

Desktop main content starts with 2.75rem top space and ends with 4rem bottom space. Page headings align a title/description group with an action, use a 1.5rem gap and leave 2rem before the content. Lists and forms begin in the first viewport; there is no separate dashboard or image introduction. Table cells use 1rem vertical and 1.2rem horizontal padding. Row actions align right with a .5rem gap.

Full-page editor and confirmation surfaces have a maximum width of 42rem with 2rem padding. Field groups use 1.5rem bottom spacing. The year field is capped at 20rem. Form action groups wrap with a .75rem gap.

At widths up to 767.98px, the container uses 1.25rem horizontal padding; main spacing becomes 2rem above and 3rem below. Page headings stack with a 1rem gap. Editors use 1.25rem padding. Navigation collapses below 768px; the contextual administration label is visible only from 992px.

Tables retain semantic headings and captions but visually become stacked records on mobile. Column headings are visually hidden; each labelled data cell shows its label above its value. Each row becomes a grid block with .75rem vertical padding; cells use .5rem by 1.2rem padding. Long titles, emails and identifiers wrap, and actions move to the left. On desktop, the table has a 680px minimum width within a responsive, keyboard-focusable region.

## Elevation & Depth

Surfaces are flat at rest: thin borders, pale table headings and background contrast provide separation. Do not add ambient card shadows to the existing panels. Focus uses outlines or Bootstrap control rings rather than raised surfaces. Inputs use an accent border with a focus-tint ring; Bootstrap buttons use their inherited variant focus rings. Generic focus-visible elements use a 3px accent outline offset by 3px. The programmatically focused main region suppresses its outline while the skip link exposes a direct keyboard path to content.

Bootstrap buttons and inputs retain their 0.15s ease-in-out color, border and focus transitions. Reduced-motion preference removes transitions and restores automatic scrolling.

## Shapes

Panels, editor surfaces and empty states use the surface radius; controls, alerts and validation summaries use the smaller control radius. Loan status labels use their own compact status radius. Borders are thin (1px), with a dashed outline reserved for the implemented empty state. Rounded rectangles remain restrained rather than pill-shaped. Buttons have a 44px minimum height, including compact row actions; text inputs have a 46px minimum height.

## Components

### Buttons

Primary blue buttons create or save records. Outline blue buttons edit, return or provide a secondary catalog action. Outline muted buttons cancel or preserve a record. Outline danger buttons open deletion confirmation; solid danger confirms deletion. Standard buttons use the primary padding token; compact row actions use the outline variant padding and small type while retaining the same minimum target height.

Primary hover and active states deepen the blue; outline primary hover fills blue and active fills deep blue. Outline danger fills the danger color on hover. The solid danger button retains Bootstrap's inherited hover/active shades; the muted outline variant retains Bootstrap's inherited secondary hover/active fill. Do not assume every inherited Bootstrap variable was recolored by the application overrides. Focus rings remain visible; disabled buttons retain Bootstrap's reduced opacity. Submitted forms replace the action label with progress text and disable that submit button until navigation; browser history restoration resets that state.

### Inputs / Fields

Native HTML text, email and number inputs receive Bootstrap form-control styling with white backgrounds, stronger neutral borders, the control radius and an accent caret. Fields retain visible labels, required/optional wording and adjacent helper text. Focus changes the border to accent and adds the focus-tint ring (0 0 0 .2rem). Invalid fields gain a danger border and a nearby small text error. Model-level errors use a padded, bordered error summary before the fields. Bootstrap's disabled control treatment is inherited; no custom replacement control is introduced.

### Navigation

A white header with a thin lower border contains the CampusLibrary wordmark as text, Books, Members and Loans. The brand uses 1.25rem bold text and tight tracking. Links are muted at rest, blue on hover, and blue/semibold with a 2px bottom rule for the current page. Current navigation is also marked with aria-current. The native Bootstrap toggler expands the menu on narrow screens; mobile links retain 44px minimum height. A keyboard-only skip link appears at the top when focused.

### Lists / Containers

White, bordered table surfaces use the surface radius. Column headings sit on the pale heading background with small semibold text; row heading cells carry the primary record identifier. Supporting identifiers sit on a separate small muted line where needed. Last rows omit their bottom border. Keep accessible captions, row/column headings, labelled mobile cells and keyboard access to the responsive region.

The editor surface is the shared container for create/edit forms and delete confirmation. Empty lists use a dashed white panel, a secondary heading, a short explanation and an appropriate next action; no illustration is required.

### Loan status

Compact rounded rectangular text labels distinguish “On loan” and “Overdue”. They are informational labels, not interactive filters. Pair every color with the written status.

### Feedback

Success feedback appears above the page content as a status region; notices use Bootstrap warning alerts. Validation summaries and field errors explain how to recover, and destructive confirmation presents record details before its action. Error/status pages retain the same shell and straightforward recovery links. Keep feedback in the task flow, using the semantic colors instead of an additional decorative treatment.

## Do's and Don'ts

### Do:

- **Do** put the page title, short task description and relevant action before the record list or form.
- **Do** reuse the shared container, native inputs and Bootstrap states.
- **Do** pair status and validation colors with explicit text.
- **Do** keep mobile record labels, wrapping long values and reachable row actions.
- **Do** preserve visible keyboard focus, the skip link and reduced-motion behavior.

### Don't:

- **Don't** introduce school logos, custom fonts or image assets without a new design decision.
- **Don't** turn the administration lists into decorative dashboards or promotional hero sections.
- **Don't** use danger styling for ordinary navigation or primary save actions.
- **Don't** hide validation messages, replace visible labels with placeholders or encode status through color alone.
- **Don't** force desktop-width table columns on narrow screens.
