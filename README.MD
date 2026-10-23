# VirtualGamePad Mobile App (The Client)

![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/kitswas/VirtualGamePad-Mobile/total) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=kitswas_VirtualGamePad-Mobile&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=kitswas_VirtualGamePad-Mobile)

This is a mobile app that allows you to use your Android phone as a gamepad for your computer.

It is a part of [the VirtualGamePad project](https://kitswas.github.io/VirtualGamePad/).

> [!NOTE]  
> I am not an experienced mobile developer. Help is appreciated.

[![Get it on F-Droid](https://f-droid.org/badge/get-it-on.png)](https://f-droid.org/packages/io.github.kitswas.virtualgamepadmobile/)

## For Developers

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
