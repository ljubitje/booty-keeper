#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

REPO_OWNER="ljubitje"
REPO_NAME="booty"
CODEBERG_API="https://codeberg.org/api/v1"
DELETIONS_FILE=".booty-deletions"

# ── Helpers ──────────────────────────────────────────────────

die()  { echo "ERROR: $*" >&2; exit 1; }
info() { echo "── $*"; }
warn() { echo "⚠  $*" >&2; }

# ── Resolve Codeberg token ───────────────────────────────────

CODEBERG_TOKEN="${CODEBERG_TOKEN:-}"
[[ -n "$CODEBERG_TOKEN" ]] || die "No Codeberg token found. Set CODEBERG_TOKEN env var."
PUSH_URL="https://${REPO_OWNER}:${CODEBERG_TOKEN}@codeberg.org/${REPO_OWNER}/${REPO_NAME}.git"

# ── Load signing properties ──────────────────────────────────

PROPS_FILE="local.properties"
GRADLE_SIGN_FLAGS=""
if [[ -f "$PROPS_FILE" ]]; then
    RELEASE_STORE_FILE=$(grep -oP '^RELEASE_STORE_FILE=\K.*' "$PROPS_FILE" || true)
    RELEASE_STORE_PASSWORD=$(grep -oP '^RELEASE_STORE_PASSWORD=\K.*' "$PROPS_FILE" || true)
    RELEASE_KEY_ALIAS=$(grep -oP '^RELEASE_KEY_ALIAS=\K.*' "$PROPS_FILE" || true)
    RELEASE_KEY_PASSWORD=$(grep -oP '^RELEASE_KEY_PASSWORD=\K.*' "$PROPS_FILE" || true)
    RELEASE_STORE_FILE="${RELEASE_STORE_FILE/#\~/$HOME}"

    if [[ -n "${RELEASE_STORE_FILE:-}" ]]; then
        # Pass signing config via properties file to avoid exposing passwords in ps output
        GRADLE_SIGN_PROPS=$(mktemp)
        trap 'rm -f "$GRADLE_SIGN_PROPS"' EXIT
        cat > "$GRADLE_SIGN_PROPS" << SIGNEOF
RELEASE_STORE_FILE=$RELEASE_STORE_FILE
RELEASE_STORE_PASSWORD=$RELEASE_STORE_PASSWORD
RELEASE_KEY_ALIAS=$RELEASE_KEY_ALIAS
RELEASE_KEY_PASSWORD=$RELEASE_KEY_PASSWORD
SIGNEOF
        chmod 600 "$GRADLE_SIGN_PROPS"
        GRADLE_SIGN_FLAGS="-Dsigning.properties=$GRADLE_SIGN_PROPS"
    fi
else
    warn "No local.properties found — signing will use defaults"
fi

# ── Pre-flight checks ───────────────────────────────────────

git diff --quiet && git diff --cached --quiet || die "Working tree is dirty — commit or stash first"

# ── 1. Fetch upstream ───────────────────────────────────────

info "Fetching upstream..."
git remote add upstream https://github.com/mtotschnig/MyExpenses.git 2>/dev/null || true
git fetch upstream master --tags

UPSTREAM_HEAD=$(git rev-parse upstream/master)
UPSTREAM_SHORT=$(git rev-parse --short upstream/master)
UPSTREAM_DESC=$(git describe --tags upstream/master 2>/dev/null || echo "$UPSTREAM_SHORT")

# ── 2. Check for new commits ────────────────────────────────

LAST_SYNC=$(git log --all --oneline --grep="Upstream commit: $UPSTREAM_HEAD" | head -1 || true)
if [[ -n "$LAST_SYNC" ]]; then
    echo "Already synced to upstream $UPSTREAM_DESC ($UPSTREAM_SHORT)"
    echo "Nothing to do."
    exit 0
fi

info "New upstream: $UPSTREAM_DESC ($UPSTREAM_SHORT)"

# ── 3. Load deletions manifest ───────────────────────────────

DELETED_PATHS=()
if [[ -f "$DELETIONS_FILE" ]]; then
    while IFS= read -r line; do
        line=$(echo "$line" | sed 's/#.*//' | xargs)
        [[ -n "$line" ]] && DELETED_PATHS+=("$line")
    done < "$DELETIONS_FILE"
fi

info "Tracking ${#DELETED_PATHS[@]} intentional deletions"

# ── 4. Detect conflicts with our deletions ───────────────────

MERGE_BASE=$(git merge-base HEAD upstream/master 2>/dev/null || echo "")
REPORT_FILE="sync-report.md"
HAS_CONFLICTS=false

if [[ -n "$MERGE_BASE" && ${#DELETED_PATHS[@]} -gt 0 ]]; then
    # Files changed upstream since our last sync
    CHANGED_UPSTREAM=$(git diff --name-only "$MERGE_BASE" upstream/master 2>/dev/null || true)

    {
        echo "# Sync Conflict Report"
        echo ""
        echo "Generated: $(date -Iseconds)"
        echo "Upstream: $UPSTREAM_DESC ($UPSTREAM_HEAD)"
        echo ""

        for del_path in "${DELETED_PATHS[@]}"; do
            # Check if upstream changed files inside our deleted paths
            AFFECTED=$(echo "$CHANGED_UPSTREAM" | grep "^${del_path}" || true)
            if [[ -n "$AFFECTED" ]]; then
                HAS_CONFLICTS=true
                echo "## Changes in deleted path: \`$del_path\`"
                echo ""
                echo "Upstream modified these files which we intentionally deleted:"
                echo ""
                echo "$AFFECTED" | while read -r f; do
                    echo "- \`$f\`"
                done
                echo ""
                echo "Review these changes to decide if any are relevant to kept code."
                echo ""
            fi
        done

    } > "$REPORT_FILE"

    if [[ "$HAS_CONFLICTS" == true ]]; then
        echo ""
        warn "Upstream changed files in paths we deleted — report: $REPORT_FILE"
        cat "$REPORT_FILE"
        echo ""
        echo "These are informational — upstream changes in deleted modules usually don't affect us."
        read -rp "Continue with sync? [Y/n] " answer
        [[ "${answer:-Y}" =~ ^[Yy]$ ]] || die "Aborted by user."
    fi
    rm -f "$REPORT_FILE"
fi

# ── 5. Merge upstream ───────────────────────────────────────

info "Merging upstream..."

# Try merge, handle conflicts
if ! git merge upstream/master --no-edit -m "Sync with upstream MyExpenses $UPSTREAM_DESC

Upstream commit: $UPSTREAM_HEAD"; then
    echo ""
    warn "Merge conflicts detected. Resolve them, then re-run sync."
    echo ""
    echo "Tip: Conflicts in deleted modules can usually be resolved with:"
    echo "  git rm <conflicting-file>"
    echo ""
    echo "After resolving, commit and re-run sync to continue."
    die "Merge failed — manual resolution needed."
fi

# ── 6. Re-apply deletions ───────────────────────────────────

if [[ ${#DELETED_PATHS[@]} -gt 0 ]]; then
    info "Re-applying intentional deletions..."
    DELETED_SOMETHING=false
    for del_path in "${DELETED_PATHS[@]}"; do
        if [[ "$del_path" == */ ]]; then
            # Directory
            if [[ -d "$del_path" ]]; then
                git rm -rf "$del_path" 2>/dev/null || true
                DELETED_SOMETHING=true
                echo "  Removed: $del_path"
            fi
        else
            # File
            if [[ -f "$del_path" ]]; then
                git rm -f "$del_path" 2>/dev/null || true
                DELETED_SOMETHING=true
                echo "  Removed: $del_path"
            fi
        fi
    done

    if [[ "$DELETED_SOMETHING" == true ]]; then
        git commit --amend --no-edit 2>/dev/null || true
    fi
fi

# ── 7. Tag ───────────────────────────────────────────────────

VERSION=$(grep 'versionName' build.gradle 2>/dev/null | head -1 | grep -oP "'[^']+'" | tr -d "'" || echo "unknown")
TAG="v${VERSION}"
if git tag -l "$TAG" | grep -q .; then
    TAG="${TAG}-$(date +%Y%m%d)"
fi
info "Tagging as $TAG"
git tag "$TAG"

# ── 8. Build APK ────────────────────────────────────────────

info "Building APK..."
GRADLE_CMD="./gradlew assembleExternRelease $GRADLE_SIGN_FLAGS"
if command -v nix &>/dev/null; then
    nix develop --command booty-build -c "$GRADLE_CMD"
else
    eval "$GRADLE_CMD"
fi

APK=$(find myExpenses/build/outputs/apk/extern/release -name '*.apk' -type f 2>/dev/null | head -1)
if [[ -z "$APK" ]]; then
    warn "Release APK not found, trying debug..."
    GRADLE_CMD="./gradlew assembleExternDebug"
    if command -v nix &>/dev/null; then
        nix develop --command booty-build -c "$GRADLE_CMD"
    else
        eval "$GRADLE_CMD"
    fi
    APK=$(find myExpenses/build/outputs/apk/extern/debug -name '*.apk' -type f 2>/dev/null | head -1)
fi

[[ -n "$APK" ]] || die "No APK found after build"
info "Built: $APK"

# ── 9. Push ──────────────────────────────────────────────────

info "Pushing to Codeberg..."
git push "$PUSH_URL" master --tags

# ── 10. Create Codeberg release ──────────────────────────────

info "Creating release $TAG on Codeberg..."

RELEASE_RESPONSE=$(curl -sf -X POST "$CODEBERG_API/repos/$REPO_OWNER/$REPO_NAME/releases" \
    -H "Authorization: token $CODEBERG_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"tag_name\": \"$TAG\",
        \"name\": \"$TAG\",
        \"body\": \"Synced with upstream MyExpenses $UPSTREAM_DESC\n\nUpstream commit: \`$UPSTREAM_SHORT\`\"
    }")

RELEASE_ID=$(echo "$RELEASE_RESPONSE" | jq -r '.id')
[[ "$RELEASE_ID" != "null" && -n "$RELEASE_ID" ]] || die "Failed to create release: $RELEASE_RESPONSE"

APK_NAME="Booty-${TAG}.apk"
curl -sf -X POST "$CODEBERG_API/repos/$REPO_OWNER/$REPO_NAME/releases/$RELEASE_ID/assets?name=$APK_NAME" \
    -H "Authorization: token $CODEBERG_TOKEN" \
    -F "attachment=@$APK" \
    > /dev/null

info "Done! Release: https://codeberg.org/$REPO_OWNER/$REPO_NAME/releases/tag/$TAG"
