### Context
<!--- Why do you believe many users will benefit from this change? -->
<!--- Link to relevant issues or forum discussions here -->

### Contributor Checklist
- [ ] [Review Contribution Guidelines](https://github.com/gradle/gradle/blob/master/CONTRIBUTING.md)
- [ ] Make sure that all commits are [signed off](https://git-scm.com/docs/git-commit#Documentation/git-commit.txt---signoff) to indicate that you agree to the terms of [Developer Certificate of Origin](https://developercertificate.org/).
- [ ] Make sure all contributed code can be distributed under the terms of the [Apache License 2.0](https://github.com/gradle/gradle/blob/master/LICENSE), e.g. the code was written by yourself or the original code is licensed under [a license compatible to Apache License 2.0](https://apache.org/legal/resolved.html).
- [ ] Provide tests (under `/src/test`, `/src/integrationTest` and `/src/functionalTest`) to verify logic
- [ ] Update documentation and Javadoc for public-facing changes
- [ ] Ensure that tests pass locally: `./gradlew test integrationTest functionalTest`

### Reviewing cheatsheet

Before merging the PR, comments starting with
- ❌ ❓**must** be fixed
- 🤔 💅 **should** be fixed
- 💭 **may** be fixed
