# SimpleFIN Kotlin

A Kotlin library for accessing financial data through the SimpleFIN API.

## Installation

You can download the library from JitPack.

https://jitpack.io/#lukelast/simplefin-kotlin

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
// Create client and fetch access token
val client = SimplefinClient()
val setupToken = "your_setup_token_here"
val accessToken: AccessTokenUrl = client.fetchAccessUrl(setupToken)

// Fetch accounts with the access token
val accounts = client.accounts(
    token = accessToken,
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

