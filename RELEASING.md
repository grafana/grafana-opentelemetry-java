# Releasing

## Publish Release via Github Workflow

### Prepare for Release

Create/switch to a new branch off of `main`.

```sh
git checkout -b Update_for_new_release
```

From the project root, run the following command to update the repo with the new version (ex. 1.0.0)

```sh
./scripts/release.sh "<VERSION>"
gradle build # this will update the version in Java
```

1. Update the repo's CHANGELOG with details about the release.
2. Then commit/push the changes and open a PR.
3. Add the "oats" label to the PR to have all acceptance tests run against the PR.
4. Merge the PR once approved.

```sh
git tag -a v<VERSION> -m "Release v<VERSION>"
git push origin v<VERSION>
```
                          
Copy the CHANGELOG entry to the release description on Github.
