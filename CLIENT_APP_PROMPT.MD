# Client App Build Prompt

Copy everything below the line into a fresh agent session to build the
mobile client. It encodes the license rules and the design system so the
client ships compliant and on-brand from day one.

---

Build the mobile client app for **منصة المتحكمات** (Minassat
Al-Mutahakkamat) — the phone side of a phone-as-PC-gamepad system. The
finished PC server lives at `https://github.com/Amr-Os/minassat-al-mutahakkamat`;
read its `docs/TECHNICAL.MD` (protocol) and `docs/DESIGN.MD` (theme) first
and treat both as normative.

## 0. License rules (do not break these)

1. The server is **GPL-3.0**. The wire protocol itself is free to
   implement from the spec (interoperability), but:
   - If you copy ANY code from the server repo (e.g. the Colfer
     decoder), the client must also be licensed **GPL-3.0** with the
     license text included. Prefer this option — license the client
     GPL-3.0 from the start.
   - Never remove or obscure existing copyright/attribution notices.
2. Credit visibly in the app (About screen) AND in the README:
   protocol and original concept by **kitswas**
   (`https://github.com/kitswas/VirtualGamePad-PC`), server rewrite
   (Flutter) by **Amr-Os**
   (`https://github.com/Amr-Os/minassat-al-mutahakkamat`).
3. No proprietary SDKs, no tracking, no ads — local LAN traffic only.

## 1. Stack & language

- Flutter (Dart), Material 3, same repo layout conventions as the server
  (`lib/`, `test/`, `docs/`).
- UI in **Arabic by default with full RTL**, plus an English option —
  mirror the server's `lib/l10n.dart` approach (every string in both
  languages, no hardcoded text).

## 2. Design (must follow docs/DESIGN.MD exactly)

- Monochrome dark theme using the exact palette tokens (bg `#0E0E0E`,
  cards `#151515`, borders `#262626`, bright `#F5F5F5`, muted `#A3A3A3`).
- Noto Naskh Arabic typography (bundle Regular 400 + Bold 700).
- Same component language: 12-radius cards, 8-radius buttons, white
  primary buttons with black text, outlined ghost buttons.
- Brand name stays `منصة المتحكمات` untranslated; use the repo's
  `assets/logo.png` style (gamepad artwork on black, never recolored).
- Status by shape + text, never by color.

## 3. Function (must interoperate with the server)

- Connect over TCP to a user-entered IP:port (manual entry + QR render
  of `IP:port`; camera QR *scanning* is a bonus, not required).
- Stream `GamepadReading` packets per the Colfer wire format in
  `docs/TECHNICAL.MD` (field order, varint/fixed forms, `127`
  terminator) at ~60Hz while controls are touched; send button
  up/down transitions immediately.
- Gamepad screen: D-pad, A/B/X/Y, shoulders, Menu/View, two analog
  sticks, two analog triggers — thumbsticks report −1.0…1.0 (up
  positive), triggers 0.0…1.0.
- Settings page: server address (remember last), portrait/landscape
  option, language (Arabic default / English).
- Never break the wire format; verify against the real server
  (`flutter run -d linux` in the server repo + a loopback test).

## 4. Verify before finishing

- `flutter analyze` clean; widget test that a button press emits the
  exact expected packet bytes; integration test of connect → stream →
  disconnect against a socket stub.
- Show a screenshot of the gamepad screen in the client README,
  which must also carry the GPL-3.0 license + the credits from §0.
