# Contributing

We welcome all kinds of feedback and contributions: in the form of ideas, issues or PRs! :)

How can you reach us and take part in the project?

* through our [community forum](https://softwaremill.community/c/open-source/11)
* by creating an [issue](https://github.com/softwaremill/jox/issues)
* or by writing some code and creating a pull request!

## Releasing

Jox has two independently versioned lines:

* core: the parent POM, `channels`, `channels-fray-tests` and `bench`, versioned by the `revision` property in the root
  `pom.xml`; tags are `vX.Y.Z-channels`
* flows: `structured`, `flows`, `kafka` and `json`, with literal versions in their POMs; tags are `vX.Y.Z-flows`

To release the flows line:

1. Replace the old version with the new one in `structured/pom.xml`, `flows/pom.xml`, `kafka/pom.xml` and
   `json/pom.xml`: each module's own `<version>` and its dependencies on `structured` and `flows`. Grep for the old
   version to find all places.
2. Update the versions in `docs/structured.md`, `docs/flows.md`, `docs/kafka.md` and `docs/json.md`.
3. Commit as `Release X.Y.Z`.
4. Publish to Maven Central: `mvn -Pdeploy -pl structured,flows,kafka,json deploy`. This needs a `central` server
   entry in `~/.m2/settings.xml` and a GPG key. Then publish the validated deployment in the Central portal.
5. Tag the commit `vX.Y.Z-flows` and push the commit and the tag. CI publishes the release notes on GitHub.

To release the core line, change `<revision>` in the root `pom.xml` and the version in `docs/channels.md`, commit as
`Release X.Y.Z of channels`, publish with `mvn -Pdeploy -pl .,channels deploy`, and tag `vX.Y.Z-channels`.
