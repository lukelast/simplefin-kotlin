# SimpleFIN Kotlin

A Kotlin library for accessing financial data through the SimpleFIN API.

## Installation

### Gradle (Kotlin DSL)

```kotlin
repositories { maven { url = uri("https://jitpack.io") } }

dependencies { implementation("com.github.lukelast:simplefin-kotlin:VERSION") }

```

### Gradle (Groovy)

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.lukelast:simplefin-kotlin:VERSION'
}
```

## Usage

```kotlin

SimpleAuthClient().use

val setupToken = "your_setup_token_here"
val accessToken: AccessTokenUrl = SimplefinAuthClient().use { it.fetchAccessUrl(setupToken) }

// Create client and fetch accounts
val client = SimplefinClient(accessToken)
val accounts = client.accounts(
    startDate = Instant.now().minus(Duration.ofDays(30)),
    endDate = Instant.now()
)
println("Accounts: $accounts")
client.close()
```

## Development

### Building

* `./gradlew test`
* `./gradlew clean test publishToMavenLocal`

