# Booty Keeper, finance tracker

A simplified fork of [MyExpenses](https://github.com/mtotschnig/MyExpenses).

## What is this?

MyExpenses is an excellent expense tracking app for Android. Booty Keeper strips it down to the essentials — removing cloud sync, OCR, banking integrations, budgets, licensing checks, and Google dependencies — while keeping the core expense tracking experience intact. All features are free, no strings attached.

## Features

- Transaction tracking with splits, transfers, and templates
- Multiple accounts with unlimited currencies
- Category tree management
- Distribution charts and history
- PDF/CSV export and printing
- Planned/recurring transactions
- Automatic exchange rate downloads

## Removed from upstream

- Cloud synchronization (WebDAV, Google Drive, etc.)
- OCR receipt scanning
- FinTS/banking integration
- Budget tracking
- Web UI
- All licensing, billing, ads, and in-app purchases

See `.booty-deletions` for the full list of removed files and modules.

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

## Credits

Booty Keeper is built on the work of [Michael Totschnig](https://github.com/mtotschnig) and the many contributors to MyExpenses.

## License

[AGPL-3.0](Licence.md)
