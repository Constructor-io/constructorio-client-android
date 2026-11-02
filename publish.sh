#!/usr/bin/env bash
#
# Publishes a new version of the Android client.
#
# Automates "How to version the iOS (Swift) & Android (Kotlin) repos" as a
# sequence of steps. Run all of them, stop after any one with --until, or pick
# up a partly finished release with --from.
#
#   sync     Switch to master and pull it from origin
#   bump     Update library/build.gradle, the README Jitpack link and the
#            `cioand-x.y.z` client strings in the tests
#   test     Run the unit tests
#   docs     Regenerate the docs (./gradlew dokkaHtml)
#   commit   Commit "Version Bump"                          (local)
#   tag      Create the annotated tag vX.Y.Z                (local)
#   push     Push master and the tag to origin              (remote)
#   release  Create the GitHub release. Jitpack builds from the tag.  (remote)
#
# Usage:
#   ./publish.sh <patch|minor|major|X.Y.Z> -m "<release title>" [options]
#   ./publish.sh --from <step> [options]
#
# Examples:
#   ./publish.sh minor -m "..." --until docs   Bump, test and build the docs, then stop
#   ./publish.sh minor -m "..." --until tag    ...and commit and tag locally, without pushing
#   ./publish.sh minor -m "..." --dry-run      ...the same, and print the push / release commands
#   ./publish.sh --from push                   Push the commit and tag created above, then release
#   ./publish.sh --from release                Retry only the GitHub release
#
# Options:
#   --until <step>       Stop after this step. Defaults to release.
#   --from <step>        Start at this step. Defaults to sync. From test onwards, the
#                        version comes from library/build.gradle and the version
#                        argument is optional.
#   --dry-run            Run the local steps (up to tag), but only print the push and
#                        release commands.
#   -m, --message <msg>  Tag message and release title. Required when the run creates
#                        the tag; when resuming after it, the existing tag's message is used.
#   -y, --yes            Don't ask for confirmation before the remote steps.
#   --skip-tests         Skip the test step.
#   --skip-docs          Skip the docs step.
#   --with-integration   Also run the *Integration* tests. They hit the live API and
#                        depend on live index data, so they're skipped by default.
#   -h, --help           Show this help.
#
# Environment:
#   JAVA_HOME_11  JDK used for gradle. Defaults to `/usr/libexec/java_home -v 11`.
#                 KAPT on Kotlin 1.5 crashes on JDK 17, so this never uses your global JAVA_HOME.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$REPO_ROOT"

BUILD_GRADLE="library/build.gradle"
README="README.md"
TEST_DIRS="library/src/test library/src/androidTest"
REMOTE="origin"
BRANCH="master"
STEPS="sync bump test docs commit tag push release"

bold() { printf '\033[1m%s\033[0m\n' "$*"; }
step() { printf '\n\033[1;34m==>\033[0m \033[1m[%s] %s\033[0m\n' "$1" "$2"; }
warn() { printf '\033[1;33mwarning:\033[0m %s\n' "$*" >&2; }
die()  { printf '\033[1;31merror:\033[0m %s\n' "$*" >&2; exit 1; }
usage() { sed -n '3,/^$/s/^# \{0,1\}//p' "$0"; }

confirm() {
  [ "$ASSUME_YES" = true ] && return 0
  local reply
  read -r -p "$1 [y/N] " reply
  case "$reply" in [yY]|[yY][eE][sS]) return 0 ;; *) return 1 ;; esac
}

# Position of a step in $STEPS, or nothing if it isn't one.
step_index() {
  local i=0 s
  for s in $STEPS; do
    [ "$s" = "$1" ] && { echo "$i"; return; }
    i=$((i + 1))
  done
}

step_after() {
  local found=false s
  for s in $STEPS; do
    [ "$found" = true ] && { echo "$s"; return; }
    [ "$s" = "$1" ] && found=true
  done
}

# Whether a step falls between --from and --until.
runs() {
  local i
  i="$(step_index "$1")"
  [ "$i" -ge "$FROM_INDEX" ] && [ "$i" -le "$UNTIL_INDEX" ]
}

read_version() {
  sed -n "s/^[[:space:]]*versionName '\([0-9]*\.[0-9]*\.[0-9]*\)'.*/\1/p" "$BUILD_GRADLE"
}

# ---------------------------------------------------------------------------
# Arguments
# ---------------------------------------------------------------------------

BUMP=""
MESSAGE=""
FROM="sync"
UNTIL=""
ASSUME_YES=false
DRY_RUN=false
SKIP_TESTS=false
SKIP_DOCS=false
WITH_INTEGRATION=false

while [ $# -gt 0 ]; do
  case "$1" in
    -m|--message) [ $# -ge 2 ] || die "$1 needs a value"; MESSAGE="$2"; shift 2 ;;
    --from)       [ $# -ge 2 ] || die "$1 needs a step"; FROM="$2"; shift 2 ;;
    --until)      [ $# -ge 2 ] || die "$1 needs a step"; UNTIL="$2"; shift 2 ;;
    -y|--yes)     ASSUME_YES=true; shift ;;
    --dry-run)    DRY_RUN=true; shift ;;
    --skip-tests) SKIP_TESTS=true; shift ;;
    --skip-docs)  SKIP_DOCS=true; shift ;;
    --with-integration) WITH_INTEGRATION=true; shift ;;
    -h|--help)    usage; exit 0 ;;
    -*)           die "unknown option: $1 (see --help)" ;;
    *)            [ -z "$BUMP" ] || die "unexpected argument: $1"; BUMP="$1"; shift ;;
  esac
done

if [ "$DRY_RUN" = true ] && [ -n "$UNTIL" ]; then
  die "--dry-run and --until can't be used together"
fi
UNTIL="${UNTIL:-release}"

FROM_INDEX="$(step_index "$FROM")"
UNTIL_INDEX="$(step_index "$UNTIL")"
[ -n "$FROM_INDEX" ] || die "unknown step for --from: $FROM (steps: $STEPS)"
[ -n "$UNTIL_INDEX" ] || die "unknown step for --until: $UNTIL (steps: $STEPS)"
[ "$FROM_INDEX" -le "$UNTIL_INDEX" ] || die "--from $FROM comes after --until $UNTIL"

if runs bump && [ -z "$BUMP" ]; then
  usage
  exit 1
fi
if runs tag && [ -z "$MESSAGE" ]; then
  die "a release title is required: -m \"<release title>\""
fi

# ---------------------------------------------------------------------------
# Preflight
# ---------------------------------------------------------------------------

step preflight "Checking the environment"

if runs release && [ "$DRY_RUN" = false ]; then
  command -v gh >/dev/null || die "the GitHub CLI (gh) is required: brew install gh"
  gh auth status >/dev/null 2>&1 || die "gh is not authenticated: run 'gh auth login'"
fi

if runs test || runs docs; then
  if [ -z "${JAVA_HOME_11:-}" ]; then
    JAVA_HOME_11="$(/usr/libexec/java_home -v 11 2>/dev/null)" \
      || die "JDK 11 not found. Install it or set JAVA_HOME_11."
  fi
fi
gradle() { JAVA_HOME="$JAVA_HOME_11" ./gradlew "$@"; }

if runs sync; then
  # Untracked files (.DS_Store, .claude/, ...) are fine; they are never staged.
  if [ -n "$(git status --porcelain --untracked-files=no)" ]; then
    git status --short --untracked-files=no
    die "you have uncommitted changes to tracked files. Commit or stash them first."
  fi
  if [ -n "$(git status --porcelain -- docs)" ]; then
    die "docs/ has untracked or modified files, and they would end up in the release commit. Clean it first."
  fi
elif [ "$(git rev-parse --abbrev-ref HEAD)" != "$BRANCH" ]; then
  die "resuming from '$FROM' needs $BRANCH checked out"
fi

# ---------------------------------------------------------------------------
# sync
# ---------------------------------------------------------------------------

if runs sync; then
  step sync "Syncing $BRANCH with $REMOTE"
  if [ "$(git rev-parse --abbrev-ref HEAD)" != "$BRANCH" ]; then
    git checkout "$BRANCH"
  fi
  git pull --ff-only --tags "$REMOTE" "$BRANCH"
fi

# ---------------------------------------------------------------------------
# Resolve the version and message
# ---------------------------------------------------------------------------

FILE_VERSION="$(read_version)"
[ -n "$FILE_VERSION" ] || die "couldn't find versionName in $BUILD_GRADLE"

if runs bump; then
  CURRENT="$FILE_VERSION"
  IFS=. read -r MAJOR MINOR PATCH <<EOF
$CURRENT
EOF
  case "$BUMP" in
    patch) NEW="$MAJOR.$MINOR.$((PATCH + 1))" ;;
    minor) NEW="$MAJOR.$((MINOR + 1)).0" ;;
    major) NEW="$((MAJOR + 1)).0.0" ;;
    *)     NEW="${BUMP#v}" ;;
  esac
  echo "$NEW" | grep -Eq '^[0-9]+\.[0-9]+\.[0-9]+$' || die "invalid version: $BUMP (expected patch, minor, major or X.Y.Z)"

  IFS=. read -r NEW_MAJOR NEW_MINOR NEW_PATCH <<EOF
$NEW
EOF
  if [ "$NEW_MAJOR" -lt "$MAJOR" ] \
    || { [ "$NEW_MAJOR" -eq "$MAJOR" ] && [ "$NEW_MINOR" -lt "$MINOR" ]; } \
    || { [ "$NEW_MAJOR" -eq "$MAJOR" ] && [ "$NEW_MINOR" -eq "$MINOR" ] && [ "$NEW_PATCH" -le "$PATCH" ]; }; then
    die "new version $NEW must be greater than the current version $CURRENT"
  fi
else
  # Resuming: the bump has already been applied.
  NEW="$FILE_VERSION"
  CURRENT=""
  if [ -n "$BUMP" ] && [ "${BUMP#v}" != "$NEW" ]; then
    die "$BUILD_GRADLE is at $NEW, not $BUMP. Drop the version argument, or start from 'bump'."
  fi
fi

TAG="v$NEW"
TAG_EXISTS=false
git rev-parse -q --verify "refs/tags/$TAG" >/dev/null && TAG_EXISTS=true

if runs bump || runs commit || runs tag; then
  [ "$TAG_EXISTS" = false ] || die "tag $TAG already exists locally"
  [ -z "$(git ls-remote --tags "$REMOTE" "refs/tags/$TAG")" ] || die "tag $TAG already exists on $REMOTE"
fi

# Resuming after the tag step: reuse the tag's message as the release title.
if [ -z "$MESSAGE" ] && runs release && [ "$TAG_EXISTS" = true ]; then
  MESSAGE="$(git tag -l --format='%(contents:subject)' "$TAG")"
fi
if runs release && [ -z "$MESSAGE" ]; then
  die "couldn't work out a release title; pass one with -m"
fi

[ -n "$CURRENT" ] && bold "Current version: $CURRENT"
bold "New version:     $NEW"
[ -n "$MESSAGE" ] && bold "Tag message:     $MESSAGE"
bold "Steps:           $FROM -> $UNTIL"
[ "$DRY_RUN" = true ] && bold "Dry run:         push and release are only printed"

# ---------------------------------------------------------------------------
# bump
# ---------------------------------------------------------------------------

if runs bump; then
  step bump "Bumping $CURRENT -> $NEW"

  # Each replacement is anchored to its context. A bare find/replace of the version
  # would also hit README entries like "// v2.18.4+ only" and unrelated numbers.

  NEW="$NEW" perl -pi -e "s/^(\s*versionName ')\d+\.\d+\.\d+(')/\${1}\$ENV{NEW}\${2}/" "$BUILD_GRADLE"
  grep -q "versionName '$NEW'" "$BUILD_GRADLE" || die "failed to update versionName in $BUILD_GRADLE"
  echo "  $BUILD_GRADLE"

  NEW="$NEW" perl -pi -e 's{(jitpack\.io/#Constructor-io/constructorio-client-android/v)\d+\.\d+\.\d+}{${1}$ENV{NEW}}g' "$README"
  grep -q "constructorio-client-android/v$NEW" "$README" || die "failed to update the Jitpack link in $README"
  echo "  $README"

  # Tests assert on the `c=cioand-x.y.z` client param. Replace every occurrence,
  # including ones a previous bump missed.
  # shellcheck disable=SC2086
  TEST_FILES="$(git grep -l -E 'cioand-[0-9]+\.[0-9]+\.[0-9]+' -- $TEST_DIRS || true)"
  for f in $TEST_FILES; do
    NEW="$NEW" perl -pi -e 's/cioand-\d+\.\d+\.\d+/cioand-$ENV{NEW}/g' "$f"
    echo "  $f"
  done

  # Check the bump didn't miss anything. docs/ is regenerated below, and the README
  # keeps "vX.Y.Z+ only" notes for older versions on purpose.
  # shellcheck disable=SC2086
  STALE="$(git grep -n -E 'cioand-([0-9]+\.[0-9]+\.[0-9]+|[&"])' -- $TEST_DIRS | grep -v -e "cioand-$NEW" -e 'startsWith("cioand-")' || true)"
  [ -z "$STALE" ] || { echo "$STALE"; die "some client version strings weren't updated"; }

  LEFTOVER="$(git grep -n -F "$CURRENT" -- . ':!docs' ":!$README" || true)"
  [ -z "$LEFTOVER" ] || { echo "$LEFTOVER"; die "the old version $CURRENT still appears in the files above. Fix them, or reset with: git checkout -- ."; }
fi

# ---------------------------------------------------------------------------
# test
# ---------------------------------------------------------------------------

if runs test; then
  if [ "$SKIP_TESTS" = true ]; then
    warn "skipping the test step"
  else
    TEST_ARGS=""
    if [ "$WITH_INTEGRATION" = true ]; then
      step test "Running unit and integration tests"
    else
      step test "Running unit tests (skipping *Integration* tests)"
      # Gradle 7 has no CLI flag for excluding tests, so pass the filter in an init script.
      INIT_SCRIPT="$(mktemp)"
      trap 'rm -f "$INIT_SCRIPT"' EXIT
      echo "allprojects { tasks.withType(Test).configureEach { filter { excludeTestsMatching '*Integration*' } } }" > "$INIT_SCRIPT"
      TEST_ARGS="--init-script $INIT_SCRIPT"
    fi
    # shellcheck disable=SC2086
    gradle $TEST_ARGS :library:testDebugUnitTest \
      || die "tests failed. Fix them and continue with: ./publish.sh --from test"
  fi
fi

# ---------------------------------------------------------------------------
# docs
# ---------------------------------------------------------------------------

if runs docs; then
  if [ "$SKIP_DOCS" = true ]; then
    warn "skipping the docs step"
  else
    step docs "Generating docs (dokkaHtml)"
    gradle dokkaHtml
  fi
fi

# ---------------------------------------------------------------------------
# commit
# ---------------------------------------------------------------------------

stage_changes() {
  # Only stage tracked files we touched, plus whatever dokka regenerated.
  git add -u -- "$BUILD_GRADLE" "$README" library/src
  git add -A -- docs
}

if runs commit; then
  step commit "Committing \"Version Bump\""
  stage_changes
  git diff --cached --quiet -- "$BUILD_GRADLE" \
    && die "$BUILD_GRADLE has no version change to commit. Run the bump step first."
  git diff --cached --stat | tail -n 25
  git commit -q -m "Version Bump"
  git log -1 --oneline
elif [ "$UNTIL_INDEX" -lt "$(step_index commit)" ]; then
  # Stopping before the commit: show what it would contain, without staging it.
  stage_changes
  echo
  bold "Changes so far:"
  git diff --cached --stat | tail -n 25
  git reset -q
fi

# ---------------------------------------------------------------------------
# tag
# ---------------------------------------------------------------------------

if runs tag; then
  step tag "Tagging $TAG"
  [ "$(git log -1 --format=%s)" = "Version Bump" ] \
    || die "HEAD isn't a \"Version Bump\" commit. Run the commit step first."
  git tag -a "$TAG" -m "$MESSAGE"
  echo "  $TAG -> $(git rev-parse --short HEAD) \"$MESSAGE\""
  TAG_EXISTS=true
fi

# ---------------------------------------------------------------------------
# push, release (remote)
# ---------------------------------------------------------------------------

if runs push || runs release; then
  [ "$TAG_EXISTS" = true ] || die "tag $TAG doesn't exist locally. Run the tag step first."
fi

if [ "$DRY_RUN" = true ]; then
  if runs push || runs release; then
    step dry-run "Skipping the remote steps. They would run:"
    runs push && echo "  git push $REMOTE $BRANCH --follow-tags"
    runs release && printf '  gh release create %s --title %q --generate-notes --verify-tag\n' "$TAG" "$MESSAGE"
  fi
  echo
  bold "Dry run: nothing was pushed."
  if runs push || runs release; then
    NEXT="$FROM"; runs tag && NEXT="push"
    echo "Publish it with: ./publish.sh --from $NEXT"
  fi
  if runs tag; then
    echo "Or discard:      git tag -d $TAG && git reset --hard $REMOTE/$BRANCH"
  fi
  exit 0
fi

if runs push || runs release; then
  ACTIONS=""
  runs push && ACTIONS="push $BRANCH and $TAG to $REMOTE"
  runs release && ACTIONS="${ACTIONS:+$ACTIONS, then }create the GitHub release \"$MESSAGE\""
  echo
  if ! confirm "About to $ACTIONS. Continue?"; then
    echo "Stopped. Nothing was pushed. Continue later with: ./publish.sh --from $FROM"
    exit 1
  fi
fi

if runs push; then
  step push "Pushing $BRANCH and $TAG to $REMOTE"
  [ "$(git rev-parse "$TAG^{commit}")" = "$(git rev-parse HEAD)" ] \
    || die "$TAG doesn't point at HEAD. Check 'git log' before pushing."
  git push "$REMOTE" "$BRANCH" --follow-tags
fi

if runs release; then
  step release "Creating GitHub release $TAG"
  [ -n "$(git ls-remote --tags "$REMOTE" "refs/tags/$TAG")" ] \
    || die "$TAG isn't on $REMOTE yet. Run the push step first."
  gh release create "$TAG" --title "$MESSAGE" --generate-notes --verify-tag
fi

# ---------------------------------------------------------------------------
# Summary
# ---------------------------------------------------------------------------

echo
if [ "$UNTIL" = release ]; then
  bold "Published $TAG"
  echo "  Release: $(gh release view "$TAG" --json url --jq .url)"
  echo "  Jitpack: https://jitpack.io/#Constructor-io/constructorio-client-android/$TAG"
  exit 0
fi

bold "Stopped after '$UNTIL'."
echo "Continue with:  ./publish.sh --from $(step_after "$UNTIL")"
case "$UNTIL" in
  sync) ;;
  bump|test|docs)
    echo "Or discard:     git checkout -- . && git clean -fd docs" ;;
  commit)
    echo "Or discard:     git reset --hard HEAD~1" ;;
  tag)
    echo "Or discard:     git tag -d $TAG && git reset --hard HEAD~1" ;;
  push)
    echo "$BRANCH and $TAG are on $REMOTE; only the GitHub release is left." ;;
esac
