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
├── SimplefinAuthClient.kt  # Authentication client for setup tokens
├── Models.kt              # Data models (AccountSet, Account, Transaction, etc.)
└── AccessTokenUrl.kt      # URL handling and credentials management
```

## Core Architecture

### Client Classes
- **SimplefinAuthClient**: Handles initial authentication using setup tokens to obtain access URLs
- **SimplefinClient**: Main client for fetching account data, transactions, and holdings
- Both clients are `AutoCloseable` and use Ktor HTTP client internally

### Data Models
- **AccountSet**: Top-level response containing accounts and errors
- **Account**: Bank account with balance, transactions, and holdings
- **Transaction**: Individual financial transaction with amount, description, payee
- **Holding**: Investment holding with shares, market value, cost basis
- **Organization**: Financial institution information

### Authentication Flow
1. Use `SimplefinAuthClient.fetchAccessUrl()` with a setup token (Base64 encoded URL)
2. Receive an `AccessTokenUrl` with credentials
3. Create `SimplefinClient` with the access URL for API calls
4. Call `accounts()` method with optional filtering parameters

## Key Dependencies

- **Ktor**: HTTP client for API communication (CIO engine)
- **Kotlinx Serialization**: JSON serialization for API responses
- **JUnit 5**: Testing framework

## Testing

The project uses JUnit 5 for testing. Test files are located in `lib/src/test/kotlin/`:
- `SimplefinClientTest.kt`: Unit tests for the main client
- `SimplefinTest.kt`: Contains main functions for manual testing and token fetching

### Demo Access
There's a `demoAccessUrl` constant available for testing against SimpleFIN's demo server.

## Maven Publishing

The library is configured to publish to Maven with:
- Group ID: `com.github.lukelast` 
- Artifact ID: `simplefin-kotlin`
- Version: `1-SNAPSHOT`

Use `publishToMavenLocal` to install locally for testing in other projects.