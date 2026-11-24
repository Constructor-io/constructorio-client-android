# Maintainers

> Only maintainers with admin access to this repo can publish a release. `master` requires a reviewed pull request, and the script pushes the "Version Bump" commit straight to `master`, which GitHub only lets admins do. Anyone can rehearse a release locally with `--dry-run`, which stops before anything is pushed.

## Publishing a release

Releases are cut locally with `./publish.sh`, run from `master` after the PR is merged. It bumps the version, runs the unit tests, regenerates the docs, commits and tags the release, pushes it and creates the GitHub release. Jitpack builds the library from the tag, and GitHub Pages redeploys the docs from `master`.

```bash
# Pick a version strategy (patch/minor/major, or an exact X.Y.Z) and a release title
./publish.sh minor -m "Add Product Insights Agent tracking"

# Rehearse it: makes the local commit and tag, but only prints the push / release commands
./publish.sh minor -m "Add Product Insights Agent tracking" --dry-run
```

The script runs these steps in order. `--until <step>` stops after any of them, and `--from <step>` picks a release back up from one:

1. `sync` checks out `master` and pulls it. It needs a clean working tree (untracked files are fine) and a clean `docs/`
2. `bump` updates `versionName` in `library/build.gradle`, the Jitpack link in `README.md` and the `cioand-x.y.z` client version the tests assert on. The replacements are anchored, so the README's `// vX.Y.Z+ only` notes are left alone, and the step fails if the old version is still anywhere in the code or tests
3. `test` runs the unit tests. Add `--with-integration` to also run the `*Integration*` tests, which hit the live API
4. `docs` regenerates `docs/` with `./gradlew dokkaHtml`
5. `commit` commits the "Version Bump" locally
6. `tag` creates the annotated tag `vX.Y.Z`, with the release title as its message
7. `push` pushes `master` and the tag. This is also the Jitpack release, since Jitpack builds from tags
8. `release` creates the GitHub release with generated notes

It asks for confirmation once, before the push and release (`--yes` skips this). `--skip-tests` and `--skip-docs` skip those steps. `-m` is required whenever the run creates the tag. See `./publish.sh --help` for every option.

Splitting a release up, or retrying part of one:

```bash
# Commit and tag locally, without pushing
./publish.sh minor -m "..." --until tag

# Push the commit and tag, then create the release
./publish.sh --from push

# Retry only the GitHub release
./publish.sh --from release
```

When resuming, the version comes from `library/build.gradle` and the release title from the existing tag, so neither needs passing again.

### One-time setup

- **GitHub CLI**: `gh auth login`
- **JDK 11**: KAPT on Kotlin 1.5 crashes on JDK 17, so the script runs Gradle on JDK 11 whatever your `JAVA_HOME` is. It finds it with `/usr/libexec/java_home -v 11`; set `JAVA_HOME_11` to use a different one.

The script checks both before it changes anything.

### Before and after you release

- When `--until` stops a run, the script prints how to continue and how to undo it. If a step fails instead, check `git log` and `git ls-remote --tags origin` to see how far it got before re-running with `--from`
- After a dry run, either publish it with `--from push` or discard the local commit and tag (the script prints both commands). A fresh run's `sync` won't start on top of it, since the tag already exists
- The docs deploy through the "pages build and deployment" workflow. If its deploy job fails with a server error, re-running it may fail the same way, so request a fresh build instead: `gh api -X POST repos/Constructor-io/constructorio-client-android/pages/builds`
- Jitpack builds a tag the first time someone requests it. Check its status on [Jitpack](https://jitpack.io/#Constructor-io/constructorio-client-android)
