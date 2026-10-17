---
name: "MotorPH payroll"
description: "A native motor-service workfolio for people, attendance and payroll."
colors:
  forest: "#14392b"
  action: "#16704c"
  nav-selected: "#27563f"
  ink: "#1b2d26"
  muted: "#596a61"
  workspace: "#f4f6f0"
  paper: "#ffffff"
  line: "#d6e0d7"
  row-selected: "#dcfce7"
  danger: "#a52e2b"
  disabled: "#e8ece6"
  rail-focus: "#aed8bd"
  rail-muted: "#b9cfbf"
  scroll-thumb: "#a3b5a8"
  error: "#b91c1c"
typography:
  headline:
    fontFamily: "Segoe UI"
    fontSize: "25px"
    fontWeight: 700
  title:
    fontFamily: "Segoe UI"
    fontSize: "16px"
    fontWeight: 700
  body:
    fontFamily: "Segoe UI"
    fontSize: "13px"
    fontWeight: 400
  label:
    fontFamily: "Segoe UI"
    fontSize: "12px"
    fontWeight: 400
  button:
    fontFamily: "Segoe UI"
    fontSize: "13px"
    fontWeight: 700
  table-header:
    fontFamily: "Segoe UI"
    fontSize: "12px"
    fontWeight: 700
rounded:
  button: "4px"
  surface: "0px"
spacing:
  label-gap: "7px"
  action-gap: "8px"
  field-row-gap: "14px"
  section-gap: "16px"
  field-column-gap: "20px"
  section-inline: "22px"
  workspace-block: "24px"
  workspace-inline: "26px"
components:
  button-primary:
    backgroundColor: "{colors.action}"
    textColor: "{colors.paper}"
    typography: "{typography.button}"
    rounded: "{rounded.button}"
    padding: "9px 13px"
  button-secondary:
    backgroundColor: "{colors.paper}"
    textColor: "{colors.ink}"
    typography: "{typography.button}"
    rounded: "{rounded.button}"
    padding: "9px 13px"
  button-danger:
    backgroundColor: "{colors.danger}"
    textColor: "{colors.paper}"
    typography: "{typography.button}"
    rounded: "{rounded.button}"
    padding: "9px 13px"
  button-disabled:
    backgroundColor: "{colors.disabled}"
    textColor: "{colors.muted}"
    typography: "{typography.button}"
    rounded: "{rounded.button}"
    padding: "9px 13px"
  input:
    backgroundColor: "{colors.paper}"
    textColor: "{colors.ink}"
    typography: "{typography.body}"
    rounded: "{rounded.surface}"
    padding: "7px 9px"
    height: "36px"
  navigation:
    backgroundColor: "{colors.forest}"
    textColor: "{colors.paper}"
    typography: "{typography.body}"
    width: "192px"
  navigation-selected:
    backgroundColor: "{colors.nav-selected}"
    textColor: "{colors.paper}"
    typography: "{typography.body}"
    rounded: "{rounded.button}"
    padding: "7px 13px"
    height: "36px"
  section:
    backgroundColor: "{colors.paper}"
    textColor: "{colors.ink}"
    rounded: "{rounded.surface}"
    padding: "20px 22px"
  table-header:
    backgroundColor: "{colors.workspace}"
    textColor: "{colors.ink}"
    typography: "{typography.table-header}"
    padding: "8px 10px"
    height: "38px"
---

# Design System: MotorPH payroll

## Overview

**Creative North Star: "Motor-service workfolio"**

MotorPH is a Windows desktop workspace built in Java 17 Swing with Nimbus. A deep forest navigation rail anchors warm pale work areas and white working surfaces. The application prioritizes readable employee, attendance and payroll records, with an explicit identity, scope and current section.

The built system uses compact Segoe UI typography, flat native painters, restrained hairlines and line icons. Its character is practical and orderly: tasks share the same shell, controls retain recognizable native behavior, and dense data receives scrollable space. MotorPH and the supplied organization identity remain product truth; this document makes no additional branding or licensing claims.

**Key Characteristics:**

- Persistent forest navigation with a distinct filled current section.
- Warm neutral workspace, white forms and flat controls.
- Compact readable type with grouped, adaptive fields.
- Semantic actions, explicit prerequisites and recoverable feedback.

This is a scan of source revision `8fce0c0`, particularly `AppTheme.java`, `WorkspaceUI.java`, `WorkspaceShell.java`, sign-in, profile and employee records. The finish verdict dated 2026-10-17 resolves F1-F6 with disposition SHIP. Native self-print captures use synthetic data; the final attendance pair in `.impeccable/review/f6-correction` supersedes the matching round-two pair. No approved image comp or browser rendering is claimed. This document does not attest completion of the separate final release gate.

The companion `.impeccable/design.json` contains portable HTML/CSS previews of the native patterns for the design panel, not application components. Its eight-step OKLCH ramps are synthesized swatch metadata, not additional shipped palette tokens. Its single breakpoint describes the native field-panel threshold in logical units; the runtime has no CSS media query.

## Colors

The palette combines forest structure, emerald actions and warm paper neutrals. Frontmatter contains the normative values.

### Primary

- **Forest** (`forest`): persistent navigation and the sign-in identity panel.
- **Emerald action** (`action`): primary tasks, input caret, selection and workspace focus.
- **Selected forest** (`nav-selected`): the current navigation section.

### Secondary

- **Destructive red** (`danger`): destructive action buttons.
- **Error red** (`error`): sign-in error status, accompanied by explanatory text.
- **Selected mint** (`row-selected`): selected table rows while retaining ink text.

### Neutral

- **Warm workspace** (`workspace`): main canvas, table headers, noneditable fields and scrollbar tracks.
- **White paper** (`paper`): sections, editable inputs, table bodies and text on solid actions.
- **Forest ink** (`ink`): working text and disabled combo-box values.
- **Muted ink** (`muted`): field labels, help, empty messages and readable disabled text.
- **Hairline** (`line`): input strokes, table frames and section dividers.
- **Disabled neutral** (`disabled`): disabled non-navigation buttons.
- **Pale rail focus** (`rail-focus`): visible keyboard focus on the dark rail.
- **Rail muted** (`rail-muted`): supporting brand and scope text.
- **Sage scroll thumb** (`scroll-thumb`): scrollbar thumb at rest; dragging uses the action color.

**The State Has Meaning Rule.** Filled navigation identifies the current section; a focus outline identifies keyboard focus. Preserve both independently.

## Typography

**Body and heading family:** Segoe UI, resolved by the Java font system. No custom display or monospace family is introduced.

### Hierarchy

- **Headline:** bold page headings and rail wordmark, using the frontmatter headline role.
- **Title:** bold section headings, using the title role.
- **Body:** regular controls, table cells, navigation and working text.
- **Label:** regular field labels, help and identity/scope; table headers use the bold table-header role.
- **Button:** bold action text; rail buttons use regular body text.
- **Supporting rail type:** existing navigation group labels use bold 10-point text; footer scope uses regular 11-point text. These small structural labels are not a substitute for body or field text.
- **Sign-in exceptions:** the MotorPH wordmark is bold 48-point, the three-line service statement is regular 31-point, and the welcome heading is bold 29-point. These belong to sign-in rather than the shared workspace ramp.

Frontmatter uses portable pixel strings for Java's numeric font sizes. Native rendering follows Java point sizing, font metrics and OS scaling; these are not measured browser CSS pixels. Line height and tracking are native metrics, not explicit tokens.

**The Readable Density Rule.** Reflow fields and scroll records before reducing shared labels or body type.

## Layout

The shared shell has a persistent left rail (192 logical units), main padding (24 vertically, 26 horizontally), and identity/scope above the page title. The rail includes four personal destinations; HR additionally receives five people-operations destinations. Sign out remains in the footer. Its navigation viewport scrolls vertically if required, with no horizontal scrollbar or surrounding frame.

Navigation rows have a constrained height (36). Main header-to-content spacing is 18; header blocks use 16. Toolbars wrap with horizontal gap 8 and vertical gap 6. White sections use padding 20 vertically and 22 horizontally, with 16 between stacked sections.

Field labels sit above controls with gap 7. Shared controls prefer height 36 and width 220, with minimum width 120. Form fields switch from two columns to one when the **field panel's own width** falls below 600; the two-column gap is 20 and row gap 14. Form canvases track viewport width and scroll vertically. This is a native component resize threshold, not a browser breakpoint.

Tables occupy available working width. Tables with more than seven columns disable automatic column resizing and use horizontal scrolling; fewer columns resize subsequent columns. Columns retain a minimum width of 90, with first-column preferred width 100 and other preferred widths derived from header length, bounded from 125 to 185. Do not truncate the data model to make a table fit.

The shell initially targets 1180 by 800, capped to usable desktop bounds; its minimum is 800 by 600, also capped. Navigation preserves window bounds between pages. Sign-in has its own two-panel layout: forest brand panel with preferred width 330 and white credential area; initial size 940 by 650 and minimum 800 by 610. Its code keeps both panels rather than implementing a stacked mobile layout.

## Elevation & Depth

Working surfaces are flat. Tonal differences, spacing, single-unit strokes and selected fills establish hierarchy; there is no shared drop-shadow vocabulary. Nimbus remains the native look-and-feel foundation, with custom flat button, text-field, table-header and scrollbar treatment. Combo and date affordances remain recognizable native controls.

**The Flat Work Surface Rule.** Use neutral fills and hairlines for working controls and containers; do not restore metallic gradients or recessed field treatment to shared surfaces.

State painting is immediate. Enabled solid buttons use Java `Color.brighter()` on rollover and `Color.darker()` while pressed; secondary rollover uses the workspace fill. There is no custom easing, animation duration or decorative motion token.

## Shapes

Sections, fields and table frames are square. Buttons have softly curved corners: native `fillRoundRect` and `drawRoundRect` use an arc width/height of 8, represented by a 4-unit corner radius in the portable frontmatter. The painter is inset by one unit; this is not an 8-unit CSS radius.

Borders are generally one unit; focus is two. Navigation icons are native vector line drawings (16 square, stroke 1.4). They use foreground color and sit 10 units from the label. Keep these purpose-built pictograms rather than substituting text glyphs.

## Components

### Buttons

Primary actions use emerald with white bold text; secondary actions use paper and ink with a hairline; destructive actions use red with white text. Shared action padding is 9 vertically and 13 horizontally. Minimum control height is 36; preferred button height follows native font metrics and padding.

Disabled non-rail buttons use the disabled neutral fill and muted text, while retaining their role's border. Reevaluate foreground when enabled state changes. A disabled primary label must remain readable. Focus draws a two-unit action outline; rail focus uses pale green. Hover and press are immediate native paint states, independent of focus.

### Inputs / Fields

Editable fields use paper, ink, a hairline and padding 7 vertically and 9 horizontally. Noneditable or disabled text fields use the workspace fill; disabled text remains muted and disabled combo values stay ink. A focused text or combo field receives a two-unit emerald border while preserving its total inset. Focusing a text field scrolls it into view. Text selection uses emerald and white.

Labels are associated with controls through `setLabelFor`. Sign-in additionally supplies field/button mnemonics, a default Sign in button and a named status region. Do not replace persistent labels with placeholders.

### Navigation

The rail carries the MotorPH wordmark, supporting service text, grouped destinations, role scope and sign out. White regular body text and native line icons share consistent alignment. Selected fill, rollover and keyboard outline are distinct. Landing workspaces can have no selected destination; do not imply that My profile is current when it is not. HR destinations reflect the existing role rules.

### Cards / Containers

Form sections are white, square panels with a bottom hairline, title, optional help and adaptive fields. They are working groups, not floating cards. Keep identity/scope separate from the heading. Compact identity summaries may precede request details; they do not replace editable fields or invent employee facts.

### Tables and empty states

Rows are 34 high; headers prefer height 38 and use bold small type with padding 8 vertically and 10 horizontally. Selected rows use mint with ink. Shared tables suppress vertical grid lines and fill the viewport. A header tooltip explains horizontal scrolling, and the accessible description explains arrow-key selection.

Empty tables show contextual text within the visible body, clear of the scrollbar, such as the employee-record instruction to add an employee. Empty content does not imply a successful save or a connection failure. Empty attendance uses the final corrected viewport-aware placement.

### Prerequisites and feedback

Record-dependent actions require a valid row selection and existing permission/state prerequisites. Refresh these states on selection, model changes and reloads. Empty payroll approvals disable record-dependent actions; eligible selected records expose the existing Verify/report behavior. Viewing, editing and creation have truthful headings and action sets.

Persistence failures use a native Data unavailable dialog with connection/retry guidance. Authorization failures use Access denied and existing session handling. Unsupported actions explicitly say that no changes were saved. Keep error text, permission checks and the existing workflows; color alone does not communicate failure.

## Do's and Don'ts

### Do:

- **Do** preserve MotorPH and the supplied organization identity without invented business claims.
- **Do** reuse the shared native shell, theme and field helpers for new workspace surfaces.
- **Do** keep identity, role scope, current section and keyboard focus distinguishable.
- **Do** reflow form columns and provide horizontal data scrolling before shrinking readable text.
- **Do** explain empty states and enforce permission, selection and record-state prerequisites.
- **Do** keep disabled labels readable and failures actionable with text.

### Don't:

- **Don't** introduce web-only layout, hover or accessibility assumptions as native runtime guarantees.
- **Don't** add decorative shadows, metallic gradients or recessed treatments to shared work surfaces.
- **Don't** replace complete labels or table data with clipped text to conceal a layout issue.
- **Don't** equate a focus outline with the current navigation selection.
- **Don't** present synthetic capture data or a native self-print as production or physical OS-input proof.

