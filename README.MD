# VirtualGamePad Mobile App (The Client)

![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/kitswas/VirtualGamePad-Mobile/total) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=kitswas_VirtualGamePad-Mobile&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=kitswas_VirtualGamePad-Mobile)

This is a mobile app that allows you to use your Android phone as a gamepad for your computer.

It is a part of [the VirtualGamePad project](https://kitswas.github.io/VirtualGamePad/).

> [!NOTE]  
> I am not an experienced mobile developer. Help is appreciated.

[![Get it on F-Droid](https://f-droid.org/badge/get-it-on.png)](https://f-droid.org/packages/io.github.kitswas.virtualgamepadmobile/)

## For Developers

Building needs a JDK 21 and an Android SDK. [mise](https://mise.jdx.dev/) pins Java and runs the common commands:

```bash
mise run doctor # Check prerequisites
mise run build  # Debug APK
mise tasks ls   # List all tasks
```

`mise run build` (and `test`, `lint`, `check`, `launch`, `connected-test`) reuses a system Android SDK from `local.properties` or `ANDROID_HOME`/`ANDROID_SDK_ROOT`. Only when none is found does it install `cmdline-tools` via `mise`, accept the licenses and write `sdk.dir` to `local.properties`; the tool is declared in a gitignored `mise.local.toml`, so the SDK tools are on PATH in this checkout and Gradle downloads the platform and build-tools on first build.

Check formatting/lint with the [ktlint](https://pinterest.github.io/ktlint/) Gradle plugin:

```bash
gradlew ktlintCheck # Report violations
gradlew ktlintFormat # Fix what can be fixed automatically
```

Git hooks enforce this and run a full build after merges:

```bash
git config core.hooksPath .githooks
```

- `pre-push`: `ktlintCheck` - blocks the push (`git push --no-verify` to bypass)
- `post-commit`: `ktlintFormat` on Kotlin files, leaving fixes as unstaged changes to review
- `post-merge`: full `gradlew build` (including tests) after merges/pulls (advisory only)

Disable with `git config --unset core.hooksPath`.
