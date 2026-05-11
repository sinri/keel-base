# Gradle Properties Example for JReleaser Maven Central Publishing

Add the following properties to your `~/.gradle/gradle.properties` file:

```properties
# Sonatype credentials for Maven Central publishing
sonatypeUsername=YOUR_SONATYPE_USERNAME
sonatypePassword=YOUR_SONATYPE_PASSWORD

# GPG signing credentials
signing.gnupg.keyName=YOUR_GPG_KEY_EMAIL_OR_ID
signing.gnupg.passphrase=YOUR_GPG_PASSPHRASE
signing.gnupg.executable=gpg
signing.gnupg.useLegacyGpg=false
```

## Usage

After updating `~/.gradle/gradle.properties`, you can publish to Maven Central with:

```bash
# Build and publish artifacts
./gradlew clean publish

# Deploy to Maven Central with automatic release
./gradlew jreleaserDeploy
```

Or combine both steps:

```bash
./gradlew clean publish jreleaserDeploy
```

## Important Notes

- Make sure your version in
  `gradle.properties` does NOT end with "SNAPSHOT" and does NOT contain pre-release identifiers (like `-rc`,
  `-beta`, etc.) for Maven Central release
- Ensure your GPG key is properly set up and the passphrase is correct
- The first deployment to Maven Central may require manual activation of the namespace in Sonatype