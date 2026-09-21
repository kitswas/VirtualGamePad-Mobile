# Design System — منصة المتحكمات

Single source of truth for the app's look. The server implements it in
`lib/theme.dart` (+ `lib/widgets.dart`); any client app must follow this
file so both ends feel like one product.

## Principles

1. **Monochrome only** — black, white, and grays. No hue anywhere.
2. **Status by shape + text, never color** — `● يعمل` vs `○ متوقف`.
3. **Dark interface**, Arabic-first with full RTL mirroring.
4. Cards group content; one primary action per row at most.

## Palette

| Token | Hex | Usage |
|---|---|---|
| bg | `#0E0E0E` | Window / scaffold background, primary button text |
| sideBg | `#141414` | Sidebar background |
| cardBg | `#151515` | Cards, dialogs, status pill |
| inputBg | `#101010` | Lists, text fields, dropdowns |
| selectedBg | `#242424` | Active nav item, selected tab |
| pressed | `#2E2E2E` | Selected list row, slider track fill end, scrollbar handle |
| border | `#262626` | Card/list/input borders, dividers, sidebar edge |
| borderStrong | `#3A3A3A` | Ghost button border, selected-nav border `#333333` |
| text | `#D6D6D6` | Body text, checkbox/radio labels |
| bright | `#F5F5F5` | Headings, primary button background, slider handle |
| muted | `#A3A3A3` | Hints, details, secondary text, unselected tabs |
| faint | `#6E6E6E` | Version label, placeholders, focused-input border |
| white surface | `#FFFFFF` | **QR code tile only** (black QR, must stay scannable) |

Hover: `#1E1E1E` surfaces, `#E8E8E8` primary button. Pressed primary: `#D9D9D9`.
Disabled: `#2A2A2A` background with `#6E6E6E`/`#5A5A5A` text.
Table header labels: `#9A9A9A`, 11px. Selected-row text: `#FFFFFF`.

## Typography

- Family: **Noto Naskh Arabic** (bundled under `assets/fonts/`, Regular 400
  + Bold 700). Engine falls back to system fonts for uncovered glyphs.
- Sizes: page title 20 / bold 700 · section/card titles 13–14 / 600 ·
  body 13 · hints/details 12 · micro labels 11.
- Digits stay Western (0–9) in both languages.
- Brand name is always `منصة المتحكمات` (never translated).

## Shape & spacing

- Cards/dialogs/QR tile: radius **12**, border 1px `border`, padding 12.
- Buttons/inputs/list rows: radius **8** (rows 6), button padding 8×18
  primary / 7×14 ghost, input padding 6×8.
- Page margins 20 sides / 14 top; card gaps 12; sidebar width 208 with a
  1px `border` edge; dividers use `border`.

## Components

- **Status pill**: filled `cardBg` row, bold bright text
  (`● يعمل · المنفذ 45678 · جهازان متصلان`), white **primary button**
  on the side (black text, `bright` bg).
- **Primary button**: `bright` bg, `bg` text, weight 700.
- **Ghost button**: transparent bg, 1px `borderStrong`, `#F0F0F0` text.
- **Nav item**: transparent; active gets `selectedBg` + 1px `#333333`
  border + white 600 text.
- **Lists**: `inputBg` container, 8px rows, selected row `pressed` with
  white text; headers 11px `#9A9A9A` (جهاز / العنوان / الحالة).
- **Inputs**: `inputBg`, 1px `#2E2E2E`, focus border `#6E6E6E`.
- **Slider**: 4px `#2A2A2A` groove, 14px `bright` round handle.
- **Tabs**: transparent, selected `selectedBg` + `#333333` border.
- **Dialogs/snackbars**: `cardBg` + `border`; actions text-button style.

## Layout & RTL

- Desktop: right (RTL) sidebar 208px + content column (title 20px,
  content, 12px status line).
- Mobile clients: same cards stacked vertically; sidebar becomes bottom
  navigation or drawer with the same three entries and the same active
  styling. Mirror everything in RTL; do not hardcode left/right —
  use directional padding/alignment.
- Pluralize counts properly in Arabic (جهاز واحد / جهازان / N أجهزة).

## Logo

`assets/logo.png` (gamepad artwork, cropped, 512px): About page at 64px,
launcher icon. Never recolor it, never place it on non-black, never
stretch it. Sidebar uses the text brand only.
