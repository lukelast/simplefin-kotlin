# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin library for the SimpleFIN API, providing a client to access financial account data. The project is structured as a Gradle multi-module build with the main library code in the `lib/` module.

## Build Commands

- **Run tests**: `./gradlew test`
- **Clean and test**: `./gradlew clean test`
- **Build and publish locally**: `./gradlew clean test publishToMavenLocal`
- **Run specific test class**: `./gradlew test --tests "SimplefinClientTest"`
- **Build the library**: `./gradlew build`

## Project Structure

```
lib/src/main/kotlin/com/lukelast/simplefin/
├── SimplefinClient.kt      # Main API client with auth and account fetching
├── DefaultHttpClient.kt    # Shared HTTP client configuration
├── Models.kt               # Data models (AccountSet, Account, Transaction, etc.)
├── AccessTokenUrl.kt       # URL handling and credentials management
└── Exceptions.kt           # Custom exception hierarchy
```

## Core Architecture

### Client Classes
- **SimplefinClient**: Unified client handling both authentication (via `fetchAccessUrl()`) and data fetching (via `accounts()`)
- The client is `AutoCloseable` and uses Ktor HTTP client internally
- Accepts an optional `HttpClient` parameter for custom configuration or testing

### Data Models
- **AccountSet**: Top-level response containing accounts and errors
- **Account**: Bank account with balance, transactions, and holdings
- **Transaction**: Individual financial transaction with amount, description, payee
- **Holding**: Investment holding with shares, market value, cost basis
- **Organization**: Financial institution information

### Authentication Flow
1. Create a `SimplefinClient` instance
2. Call `fetchAccessUrl()` with a setup token (Base64 encoded URL)
3. Receive an `AccessTokenUrl` with credentials (throws `SetupTokenUsedException` if token already used)
4. Call `accounts()` method passing the `AccessTokenUrl` and optional filtering parameters (startDate, endDate, pending, accounts list, balancesOnly)

### AccessTokenUrl
The `AccessTokenUrl` class handles URL parsing and credential management:
- Constructor accepts either a full URL string or user/pass credentials
- Validates HTTPS protocol and "simplefin" in host (throws `InvalidAccessUrlException` on validation failure)
- Provides `accountsUrl()` to build the accounts endpoint URL
- Includes a `demoAccessUrl` constant for testing: `https://demo:demo@beta-bridge.simplefin.org/simplefin`

### Exception Hierarchy
All SimpleFIN exceptions extend from the sealed class `SimplefinException`:
- **SimplefinApiException**: API request failed with non-200 status (includes status code, response body, and endpoint name)
- **SetupTokenUsedException**: Setup token has already been used and cannot be reused
- **InvalidAccessUrlException**: Access token URL is invalid or malformed (protocol, host, path, or credentials issues)

## Key Dependencies

- **Ktor**: HTTP client for API communication (CIO engine)
- **Kotlinx Serialization**: JSON serialization for API responses
- **JUnit 5**: Testing framework

### HTTP Client Configuration
The `defaultHttpClient()` function in `DefaultHttpClient.kt` provides a pre-configured Ktor HTTP client with:
- Connection pooling (50 max connections, 50 per route)
- Automatic retry logic (1 retry with exponential backoff on server errors and exceptions)
- Timeouts (60s request, 5s connect, 45s socket)
- Keep-alive connections (60s)
- 2 connect attempts per request

`SimplefinClient` uses this by default but accepts an optional `HttpClient` parameter for custom configuration or testing.

## Testing

The project uses JUnit 5 for testing. Test files are located in `lib/src/test/kotlin/`:
- `SimplefinClientTest.kt`: Unit tests for the main client
- `SimplefinTest.kt`: Contains main functions for manual testing and token fetching

### Demo Access
There's a `demoAccessUrl` constant available for testing against SimpleFIN's demo server.

## Publishing

### Local Maven
The library is configured to publish to Maven with:
- Group ID: `com.github.lukelast`
- Artifact ID: `simplefin-kotlin`
- Version: `1-SNAPSHOT`

Use `publishToMavenLocal` to install locally for testing in other projects.

### JitPack
The library is available via JitPack at: https://jitpack.io/#lukelast/simplefin-kotlin

Add to Gradle (Kotlin DSL):
```kotlin
repositories { maven { url = uri("https://jitpack.io") } }
dependencies { implementation("com.github.lukelast:simplefin-kotlin:VERSION") }
```