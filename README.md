# Booty

A simplified, de-Googled fork of [MyExpenses](https://github.com/mtotschnig/MyExpenses).

## What is this?

MyExpenses is an excellent expense tracking app for Android. Booty strips it down to the essentials — removing cloud sync, OCR, banking integrations, licensing checks, and Google dependencies — while keeping the core expense tracking experience intact.

## Building

Requires [Nix](https://nixos.org/download.html):

```bash
nix develop
./gradlew assembleExternRelease
```

The APK will be in `myExpenses/build/outputs/apk/extern/release/`.

## Syncing with upstream

```bash
./sync.sh
```

This fetches the latest upstream MyExpenses, merges it, re-applies our intentional deletions (tracked in `.booty-deletions`), builds, and publishes a release to Codeberg.

## What's removed

See `.booty-deletions` for the full list of removed files and modules.

## Credits

Booty is built on the work of [Michael Totschnig](https://github.com/mtotschnig) and the many contributors to MyExpenses. See the upstream [README](https://github.com/mtotschnig/MyExpenses#readme) for the full credits list.

## License

GPL-3.0, same as upstream.
