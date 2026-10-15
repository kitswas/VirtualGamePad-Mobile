# Releasing

Draft from git history. Confirm each step before relying on it.

## Before tagging

1. `mise run check` passes (build, lint, unit tests).
2. Play-test a release candidate with a real game.
3. Bump `versionCode` and `versionName` in `app/build.gradle.kts`.
4. Update Fastlane metadata in `fastlane/metadata/android/en-US` if needed (CI validates it).
5. Update the `VGP_Data_Exchange` submodule only if the protocol changed.

## Signing

Release builds read `signing.properties` (copy `signing.properties.template`).
It needs `STORE_FILE`, `STORE_PASSWORD`, `KEY_ALIAS` and `KEY_PASSWORD`.
Without it the release build is unsigned. Never commit this file or the keystore.

## Build and tag

1. Build the release APK with `./gradlew assembleRelease`.
2. Tag: `git tag -a vX.Y.Z -m "Release vX.Y.Z"` and `git push origin vX.Y.Z`.
3. TODO: how the APK reaches F-Droid or Play. Not recorded in this repo.

## Upgrading Gradle

Get the version and its sha256 from <https://gradle.org/releases/>, then:

```
mise run wrapper-upgrade 9.5.0 <sha256>
```

## Troubleshooting

- Builds hang or files stay locked: `mise run gradle-stop`.
- Gradle configuration cache problems: run with `--no-configuration-cache` before disabling it in `gradle.properties`.
