# memorie.md — لَعّيب agent memory

> Read this first. It exists so you do NOT re-walk unchanged files every turn.
> Only re-read a file if you are about to edit it or suspects it changed.

## 1. What this project is
- Android client (Kotlin, Jetpack Compose, Material3, Navigation-Compose).
  Phone touch/sensors → TCP socket → PC server (VirtualGamePad).
- App name: **لَعّيب** (`app_name` in both `values/` + `values-ar/`).
  `Amr-Os/VirtualGamePad-Mobile` was RENAMED to `Amr-Os/Laeeb-Gamepad`
  (same repo id; old URL redirects). Local origin URL updated accordingly.
  About links: mobile repo + server releases
  `Amr-Os/minassat-al-mutahakkamat` + kitswas site.
- Theme is FIXED monochrome dark (DESIGN.MD): `Theme.kt monoDarkScheme()`
  ignores darkMode/baseColor; defaultColorScheme = DARK. Naskh font in
  `res/font/naskh_*.ttf`, wired in `ui/theme/Type.kt`. Gamepad screen +
  face-button colors + profile previews are EXEMPT from mono (user).
- Root `README.MD` is the Arabic user guide; `screenshots/gamepad.png`
  (mono main menu, real device shot).
- Package: `io.github.kitswas.virtualgamepadmobile`
- App module: `app/src/main/java/.../virtualgamepadmobile/`
- Protocol submodule: `VGP_Data_Exchange/` (git submodule, colfer-generated).
  Schema `GamePadReading.colf`: `ButtonsUp/Down uint32`, `Left/RightTrigger f32`,
  `Left/RightThumbstickX/Y f32`. Wired as sourceset in `app/build.gradle.kts:80-84`
  (`java.directories.add("../VGP_Data_Exchange")`). Send path:
  `network/ConnectionViewModel.kt:234` `gamepadState.marshal(outputStream)`.
- Build: `compileSdk 36, minSdk 26, targetSdk 34, v0.4.2 (11), Java/JvmTarget 21`.
  Version catalog `gradle/libs.versions.toml` (AGP 9.1.0, Kotlin 2.3.20).
  Signing via root `signing.properties`, fallback to debug key.

## 2. Runtime data flow (do not re-derive)
- `MainActivity.kt` builds `SettingsRepository` + `ConnectionViewModel`
  (via `ConnectionViewModelFactory`), hosts `NavHost(start="main_menu")`.
  Routes: `main_menu`, `connect_screen`, `connecting_screen/{ip}/{port}`,
  `gamepad`, `settings_screen`, `gamepad_customization/{profileId}`,
  `gamepad_customization_new`, `about_screen`.
- Gamepad input: shared mutable `GamepadReading()` mutated by touch composables
  (`ButtonsDown/Up` bitmasks via `GameButtons`, sticks `-1..1`, triggers `0..1`).
  `ui/screens/Gamepad.kt:296` poll loop `enqueueGamepadState()` every `pollingDelay`
  (default 80ms), then `ButtonsUp=0`. `ON_DESTROY` sends neutral state.
- Gyro: `TYPE_GAME_ROTATION_VECTOR ?: TYPE_ROTATION_VECTOR`, azimuth→X,
  roll-bend→Y, auto-calibrated on first reading. Assignment derived from
  per-stick `ButtonConfig.gyro` flags via `GyroAssign.fromConfigs()`.
- Analog feel: `StickResponseMode.map()` curve (`exponent/sensitivity`).
  **Per-stick, per-profile**: `ButtonConfig.responseMode` (default RESPONSIVE).
  Global `SettingsRepository.stickResponseMode` is LEGACY — do not use in UI.
- Profiles: built-ins (`game_controller`, `racing_1/2` transforms in MainActivity)
  + custom JSON in `filesDir/custom_profiles/*.json` via `CustomProfileStorage`.
  Selecting a profile writes `setAllButtonConfigs()` + `setActiveProfileId()`.
  Editor (`GamepadCustomizationScreen`) edits `editableConfigs` map, saves via
  `saveAndExit()` → `CustomProfileStorage.saveProfile()` + `setAllButtonConfigs()`.

## 3. Where things live (stable map)
- `data/`: `SettingsRepository.kt` (DataStore: polling, haptic, fullscreen,
  saveCreds, activeProfile, buttonConfigs JSON, appLanguage),
  `ButtonComponent.kt` (`ButtonConfig`: visible/scale/offset/anchor/gyro/
  analogInnerScale/responseMode), `Defaults.kt`, `CustomProfileStorage.kt`,
  `GyroAssign.kt`, `StickResponseMode.kt` (@Serializable enum),
  `AppLanguage.kt` (AR default), `ColorScheme.kt`/`BaseColor.kt` (legacy theme,
  settings UI removed — `MainActivity` uses fixed defaults).
- `network/`: `ConnectionViewModel.kt` (queue + socket), `NetworkCommand.kt`,
  `NetworkDiagnostics.kt`, `ConnectionState.kt`.
- `ui/screens/`: `MainMenu.kt` (portrait dashboard, fully localized via
  `main_*`/`profile_*`/`menu_home` strings — NO hardcoded display text),
  `Gamepad.kt` (clean pad, NO pills — Joystick/Gyro/Recenter live only in the
  editor stick widgets; `USER_LANDSCAPE`→ fixed `LANDSCAPE` via activity lock,
  LTR-locked), `ConnectScreen.kt`
  (IP/port + QR), `ConnectingScreen.kt`,
  `GamepadCustomization.kt` (landscape editor + per-stick Response/Gyro/Knob),
  `SettingsScreen.kt` (language + polling + switches, NO theme pickers),
  `AboutScreen.kt` (portrait).
- `ui/composables/`: `Gamepad.kt` (`DrawGamepad` uses `config.responseMode`
  per stick), `AnalogStick.kt`, `Dpad.kt`, `FaceButtons.kt`, `Trigger.kt`,
  `CentralButtons.kt`, `ButtonConfigEditor.kt` (generic, unused by pill),
  `ColorSchemePicker.kt` (legacy, unused), `QRCodeScanner.kt`.
- `ui/utils/`: `LockScreenOrientation.kt`, `LocaleHelper.kt`, `HapticUtils.kt`.
- `res/`: `values/strings.xml` (EN) + `values-ar/strings.xml` (AR),
  `xml/locales_config.xml` (ar, en), manifest `portrait` + `localeConfig`
  + `CAMERA` permission.

## 4. Conventions / gotchas (learned, do not rediscover)
- Orientation: manifest `portrait`. Authority is the activity-level
  destination listener in `MainActivity.NavTree` (gamepad + both customization
  routes → fixed `LANDSCAPE`, everything else → `PORTRAIT`); per-screen
  `LockScreenOrientation` calls agree with it. `LockScreenOrientation` is
  SET-ONLY (no restore on dispose) — restoring yanked the next screen after
  the nav transition finished (~1s late flip back to portrait). Do NOT
  reintroduce sensor/user rotation or dispose-restores without asking.
- Locale: Arabic default. `MainActivity.attachBaseContext` wraps via
  `LocaleHelper` reading DataStore synchronously. Language change recreates
  activity (locale NOT in `configChanges`). Gamepad forced
  `LocalLayoutDirection.Ltr` so Arabic RTL never mirrors sticks.
- Gamepad has NO overlay pills by design (user request). Stick feel + gyro
  live ONLY in profile editor stick widgets, saved per-profile.
- Pills/strings in gamepad + editor are hardcoded English (e.g. "Size",
  "Knob", "Gyro", "Response") — intentional, matches existing pill style.
  Do not "fix" to stringResource without asking.
- `ButtonConfig` JSON: new fields must have defaults for backward compat
  (old profiles decode fine). `StickResponseMode` must stay `@Serializable`.
- `DrawGamepad` signature keeps legacy `stickResponseMode` param (default) as
  fallback when a stick config is missing; per-stick `config.responseMode` wins.
- Tests: unit `src/test` (CustomProfileStorage, GyroAssign, NetworkDiagnostics,
  MainActivityTest) + instrumented `src/androidTest/e2e` (Connection,
  GamepadInput, Navigation, Settings) with `TestGamepadServer` fake socket.
  Settings E2E only clicks Reset/Save/Cancel — safe under settings changes.

## 5. Active / pending work (update when done)
- [done] Arabic default + settings toggle; gamepad LTR-isolated.
- [done] Orientation: portrait everywhere except gamepad/customization.
- [done] Theme pickers removed from settings (fixed defaults in MainActivity).
- [done] QR scanner hardened (`QRCodeScanner.kt`: orientation unlocked,
  torch on, 30s timeout; `CAMERA` permission in manifest).
- [todo] Verify: `./gradlew :app:assembleDebug :app:testDebugUnitTest`.
