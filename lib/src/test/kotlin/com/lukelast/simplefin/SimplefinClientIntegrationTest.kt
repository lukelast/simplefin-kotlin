package com.lukelast.simplefin

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

/**
 * Integration tests for SimplefinClient that hit the real demo API. These tests verify actual API
 * responses, data structure, and business logic.
 */
class SimplefinClientIntegrationTest {

    private lateinit var client: SimplefinClient

    @BeforeEach
    fun setUp() {
        client = SimplefinClient()
    }

    @AfterEach
    fun tearDown() {
        client.close()
    }

    // ========================================
    // Parameter Variations Tests
    // ========================================

    @Test
    fun `accounts with all default parameters`() {
        runBlocking {
            val result = client.accounts(token = demoAccessUrl)

            // Structure validation
            assertNotNull(result)
            assertNotNull(result.errors)
            assertNotNull(result.accounts)
            assertTrue(result.errors.isEmpty(), "Should have no errors")
            assertTrue(result.accounts.isNotEmpty(), "Should have at least one account")

            // Verify accounts have required fields
            result.accounts.forEach { account ->
                assertNotNull(account.id)
                assertNotNull(account.name)
                assertNotNull(account.currency)
                assertNotNull(account.balance)
                assertNotNull(account.org)
                assertTrue(account.balanceDate > 0, "Balance date should be a valid timestamp")
            }
        }
    }

    @Test
    fun `accounts with 30 day date range`() {
        runBlocking {
            val endDate = Instant.now()
            val startDate = endDate.minus(30.days.toJavaDuration())

            val result =
                client.accounts(token = demoAccessUrl, startDate = startDate, endDate = endDate)

            // Structure validation
            assertTrue(result.errors.isEmpty())
            assertTrue(result.accounts.isNotEmpty())

            // Data value validation - verify most transactions are within date range
            // Note: Demo API may have some transactions slightly outside range due to timezone/sync
            result.accounts.forEach { account ->
                account.transactions?.forEach { transaction ->
                    val txDate = Instant.ofEpochSecond(transaction.posted)
                    // Allow 1 day buffer for timezone/sync issues
                    val rangeStart = startDate.minus(1.days.toJavaDuration())
                    val rangeEnd = endDate.plus(1.days.toJavaDuration())
                    assertTrue(
                        !txDate.isBefore(rangeStart) && !txDate.isAfter(rangeEnd),
                        "Transaction date $txDate should be roughly between $startDate and $endDate (with 1 day buffer)",
                    )
                }
            }
        }
    }

    @Test
    fun `accounts with 90 day date range`() {
        runBlocking {
            val endDate = Instant.now()
            val startDate = endDate.minus(90.days.toJavaDuration())

            val result =
                client.accounts(token = demoAccessUrl, startDate = startDate, endDate = endDate)

            assertTrue(result.errors.isEmpty())
            assertTrue(result.accounts.isNotEmpty())

            // Verify we got transactions
            val hasTransactions = result.accounts.any { it.transactions?.isNotEmpty() == true }
            assertTrue(hasTransactions, "Should have some transactions in 90 day range")
        }
    }

    @Test
    fun `accounts with 1 year date range`() {
        runBlocking {
            val endDate = Instant.now()
            val startDate = endDate.minus(365.days.toJavaDuration())

            val result =
                client.accounts(token = demoAccessUrl, startDate = startDate, endDate = endDate)

            assertTrue(result.errors.isEmpty())
            assertTrue(result.accounts.isNotEmpty())
        }
    }

    @Test
    fun `accounts with only startDate`() {
        runBlocking {
            val startDate = Instant.now().minus(30.days.toJavaDuration())

            val result =
                client.accounts(token = demoAccessUrl, startDate = startDate, endDate = null)

            assertTrue(result.errors.isEmpty())
            assertTrue(result.accounts.isNotEmpty())

            // Verify transactions are after startDate (with small buffer for demo API)
            result.accounts.forEach { account ->
                account.transactions?.forEach { transaction ->
                    val txDate = Instant.ofEpochSecond(transaction.posted)
                    val rangeStart = startDate.minus(1.days.toJavaDuration())
                    assertTrue(
                        !txDate.isBefore(rangeStart),
                        "Transaction date should be roughly after $startDate (with 1 day buffer)",
                    )
                }
            }
        }
    }

    @Test
    fun `accounts with only endDate`() {
        runBlocking {
            val endDate = Instant.now()

            val result = client.accounts(token = demoAccessUrl, startDate = null, endDate = endDate)

            assertTrue(result.errors.isEmpty())
            assertTrue(result.accounts.isNotEmpty())

            // Verify transactions are before endDate (with small buffer for demo API)
            result.accounts.forEach { account ->
                account.transactions?.forEach { transaction ->
                    val txDate = Instant.ofEpochSecond(transaction.posted)
                    val rangeEnd = endDate.plus(1.days.toJavaDuration())
                    assertTrue(
                        !txDate.isAfter(rangeEnd),
                        "Transaction date should be roughly before $endDate (with 1 day buffer)",
                    )
                }
            }
        }
    }

    @Test
    fun `accounts with specific account filter`() {
        runBlocking {
            val result = client.accounts(token = demoAccessUrl, accounts = listOf("Demo Checking"))

            assertTrue(result.errors.isEmpty())
            assertTrue(result.accounts.isNotEmpty())

            // Verify only the requested account is returned
            result.accounts.forEach { account ->
                assertTrue(
                    account.name.contains("Checking", ignoreCase = true),
                    "Account name should match filter: ${account.name}",
                )
            }
        }
    }

    @Test
    fun `accounts with multiple account filter`() {
        runBlocking {
            val result =
                client.accounts(
                    token = demoAccessUrl,
                    accounts = listOf("Demo Checking", "Demo Savings"),
                )

            assertTrue(result.errors.isEmpty())
            // May return 0-2 accounts depending on what demo has
            result.accounts.forEach { account ->
                val matchesFilter =
                    account.name.contains("Checking", ignoreCase = true) ||
                        account.name.contains("Savings", ignoreCase = true)
                assertTrue(
                    matchesFilter,
                    "Account name should match one of the filters: ${account.name}",
                )
            }
        }
    }

    @Test
    fun `accounts with non-existent account name`() {
        runBlocking {
            val result =
                client.accounts(token = demoAccessUrl, accounts = listOf("NonExistentAccount12345"))

            assertTrue(result.errors.isEmpty())
            // Should return empty accounts list or no matching accounts
            val matchingAccounts = result.accounts.filter { it.name == "NonExistentAccount12345" }
            assertTrue(matchingAccounts.isEmpty(), "Should not find non-existent account")
        }
    }

    @Test
    fun `accounts with pending transactions disabled`() {
        runBlocking {
            val result = client.accounts(token = demoAccessUrl, pending = false)

            assertTrue(result.errors.isEmpty())
            assertTrue(result.accounts.isNotEmpty())
            assertNotNull(result.accounts)
        }
    }

    @Test
    fun `accounts with balancesOnly enabled`() {
        runBlocking {
            val result = client.accounts(token = demoAccessUrl, balancesOnly = true)

            assertTrue(result.errors.isEmpty())
            assertTrue(result.accounts.isNotEmpty())

            // Business logic validation - transactions and holdings should be null or empty
            result.accounts.forEach { account ->
                val noTransactions = account.transactions == null || account.transactions.isEmpty()
                val noHoldings = account.holdings == null || account.holdings.isEmpty()

                assertTrue(
                    noTransactions,
                    "balancesOnly should not return transactions for ${account.name}",
                )
                assertTrue(
                    noHoldings,
                    "balancesOnly should not return holdings for ${account.name}",
                )

                // Should still have balance information
                assertNotNull(account.balance)
                assertNotNull(account.balanceDate)
            }
        }
    }

    @Test
    fun `accounts with balancesOnly disabled returns full data`() {
        runBlocking {
            val result =
                client.accounts(
                    token = demoAccessUrl,
                    balancesOnly = false,
                    startDate = Instant.now().minus(30.days.toJavaDuration()),
                )

            assertTrue(result.errors.isEmpty())
            assertTrue(result.accounts.isNotEmpty())

            // Should have some accounts with transactions
            val hasTransactions =
                result.accounts.any { it.transactions != null && it.transactions.isNotEmpty() }
            assertTrue(hasTransactions, "Should have transaction data when balancesOnly=false")
        }
    }

    @Test
    fun `accounts with all parameters combined`() {
        runBlocking {
            val result =
                client.accounts(
                    token = demoAccessUrl,
                    startDate = Instant.now().minus(50.days.toJavaDuration()),
                    endDate = Instant.now(),
                    pending = true,
                    accounts = listOf("Demo Checking"),
                    balancesOnly = false,
                )

            assertTrue(result.errors.isEmpty())
            assertNotNull(result.accounts)
        }
    }

    // ========================================
    // Edge Cases Tests
    // ========================================

    @Test
    fun `accounts with very old startDate`() {
        runBlocking {
            val startDate = Instant.now().minus(1825.days.toJavaDuration()) // 5 years

            val result = client.accounts(token = demoAccessUrl, startDate = startDate)

            // Demo API should handle old dates gracefully, though may return errors if date is too
            // old
            // The important thing is it doesn't throw an exception
            assertNotNull(result)
            assertNotNull(result.accounts)
            // May have errors if the date range is not supported
            // May or may not have transactions depending on demo data
        }
    }

    @Test
    fun `accounts with future endDate`() {
        runBlocking {
            val futureDate = Instant.now().plus(30.days.toJavaDuration())

            val result = client.accounts(token = demoAccessUrl, endDate = futureDate)

            // Should succeed, just return current data
            assertTrue(result.errors.isEmpty())
            assertNotNull(result.accounts)
        }
    }

    @Test
    fun `accounts with same startDate and endDate`() {
        runBlocking {
            val date = Instant.now()

            val result = client.accounts(token = demoAccessUrl, startDate = date, endDate = date)

            assertTrue(result.errors.isEmpty())
            assertNotNull(result.accounts)

            // Transactions should be empty or only from that specific day
            result.accounts.forEach { account ->
                account.transactions?.forEach { transaction ->
                    val txDate = Instant.ofEpochSecond(transaction.posted)
                    // Allow same day (within 24 hours)
                    val dayStart = date.minus(1.days.toJavaDuration())
                    val dayEnd = date.plus(1.days.toJavaDuration())
                    assertTrue(
                        !txDate.isBefore(dayStart) && !txDate.isAfter(dayEnd),
                        "Transaction should be on the same day",
                    )
                }
            }
        }
    }

    @Test
    fun `accounts with inverted date range returns data anyway`() {
        runBlocking {
            val startDate = Instant.now()
            val endDate = Instant.now().minus(30.days.toJavaDuration())

            // API may handle this gracefully or return error
            val result =
                client.accounts(token = demoAccessUrl, startDate = startDate, endDate = endDate)

            // Should not throw exception, though may return errors or empty data
            assertNotNull(result)
            assertNotNull(result.accounts)
            // Inverted ranges may produce errors or empty results - that's acceptable
        }
    }

    @Test
    fun `accounts with empty accounts list returns all accounts`() {
        runBlocking {
            val result = client.accounts(token = demoAccessUrl, accounts = listOf())

            assertTrue(result.errors.isEmpty())
            assertTrue(result.accounts.isNotEmpty(), "Empty filter should return all accounts")
        }
    }

    // ========================================
    // Response Validation Tests
    // ========================================

    @Test
    fun `accounts response has valid structure`() {
        runBlocking {
            val result =
                client.accounts(
                    token = demoAccessUrl,
                    startDate = Instant.now().minus(30.days.toJavaDuration()),
                )

            // AccountSet structure
            assertNotNull(result.errors)
            assertNotNull(result.accounts)

            // Optional field can be null
            // result.xApiMessage can be null or List<String>

            result.accounts.forEach { account ->
                // Required fields
                assertNotNull(account.id)
                assertNotNull(account.name)
                assertNotNull(account.currency)
                assertNotNull(account.balance)
                assertNotNull(account.balanceDate)
                assertNotNull(account.org)

                // Organization structure
                assertNotNull(account.org.sfinUrl)

                // Transactions structure (if present)
                account.transactions?.forEach { tx ->
                    assertNotNull(tx.id)
                    assertNotNull(tx.posted)
                    assertNotNull(tx.amount)
                    assertNotNull(tx.description)
                    assertNotNull(tx.payee)
                    assertNotNull(tx.memo)
                }

                // Holdings structure (if present)
                account.holdings?.forEach { holding ->
                    assertNotNull(holding.id)
                    assertNotNull(holding.created)
                    assertNotNull(holding.cost_basis)
                    assertNotNull(holding.currency)
                    assertNotNull(holding.description)
                    assertNotNull(holding.market_value)
                    assertNotNull(holding.purchase_price)
                    assertNotNull(holding.shares)
                    assertNotNull(holding.symbol)
                }
            }
        }
    }

    @Test
    fun `accounts response has valid data values`() {
        runBlocking {
            val result = client.accounts(token = demoAccessUrl)

            assertTrue(result.accounts.isNotEmpty())

            result.accounts.forEach { account ->
                // Currency should be 3-letter uppercase code
                assertTrue(
                    account.currency.matches(Regex("[A-Z]{3}")),
                    "Currency should be 3-letter code: ${account.currency}",
                )

                // Balance should be a valid decimal number
                assertDoesNotThrow(
                    { account.balance.toBigDecimal() },
                    "Balance should be parseable as decimal: ${account.balance}",
                )

                // Available balance should be parseable if present
                account.availableBalance?.let { availBal ->
                    assertDoesNotThrow(
                        { availBal.toBigDecimal() },
                        "Available balance should be parseable: $availBal",
                    )
                }

                // Balance date should be a reasonable timestamp (after 2000-01-01)
                val epochYear2000 = 946684800L
                assertTrue(
                    account.balanceDate > epochYear2000,
                    "Balance date should be after year 2000: ${account.balanceDate}",
                )

                // Verify transaction amounts are valid decimals
                account.transactions?.forEach { tx ->
                    assertDoesNotThrow(
                        { tx.amount.toBigDecimal() },
                        "Transaction amount should be parseable: ${tx.amount}",
                    )

                    // Posted date should be reasonable
                    assertTrue(
                        tx.posted > epochYear2000,
                        "Transaction date should be after year 2000: ${tx.posted}",
                    )
                }

                // Verify holding values are valid decimals
                account.holdings?.forEach { holding ->
                    assertDoesNotThrow(
                        {
                            holding.shares.toBigDecimal()
                            holding.cost_basis.toBigDecimal()
                            holding.market_value.toBigDecimal()
                            holding.purchase_price.toBigDecimal()
                        },
                        "Holding values should be parseable as decimals",
                    )

                    // Currency should be 3-letter code
                    assertTrue(
                        holding.currency.matches(Regex("[A-Z]{3}")),
                        "Holding currency should be 3-letter code: ${holding.currency}",
                    )
                }
            }
        }
    }

    @Test
    fun `accounts response demonstrates business logic rules`() {
        runBlocking {
            val result =
                client.accounts(
                    token = demoAccessUrl,
                    startDate = Instant.now().minus(30.days.toJavaDuration()),
                )

            result.accounts.forEach { account ->
                // Balance should be a valid number (positive or negative)
                val balance = account.balance.toBigDecimal()
                assertNotNull(balance)

                account.transactions?.forEach { tx ->
                    val amount = tx.amount.toBigDecimal()

                    // Verify amount semantics: negative typically means expense, positive means
                    // income
                    // Just verify it's a valid number
                    assertNotNull(amount)

                    // Verify description and payee are not blank
                    assertTrue(tx.description.isNotBlank(), "Description should not be blank")
                    assertTrue(tx.payee.isNotBlank(), "Payee should not be blank")

                    // If transactedAt is present, it should be a valid timestamp
                    tx.transactedAt?.let { transactedAt ->
                        val epochYear2000 = 946684800L
                        assertTrue(
                            transactedAt > epochYear2000,
                            "TransactedAt should be after year 2000",
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `accounts with Demo Checking account has expected structure`() {
        runBlocking {
            val result =
                client.accounts(
                    token = demoAccessUrl,
                    accounts = listOf("Demo Checking"),
                    startDate = Instant.now().minus(30.days.toJavaDuration()),
                )

            // Verify at least one account is returned
            assertTrue(result.accounts.isNotEmpty())

            val checkingAccount = result.accounts.first()
            assertTrue(
                checkingAccount.name.contains("Checking", ignoreCase = true),
                "Should be a checking account",
            )

            // Verify it has transactions
            assertNotNull(checkingAccount.transactions)

            // Currency should be USD for demo
            assertEquals("USD", checkingAccount.currency)

            // Should have organization info
            assertNotNull(checkingAccount.org)
            assertNotNull(checkingAccount.org.sfinUrl)
        }
    }

    @Test
    fun `accounts response handles optional fields correctly`() {
        runBlocking {
            val result = client.accounts(token = demoAccessUrl)

            result.accounts.forEach { account ->
                // These fields are optional and may be null
                // Just verify they don't cause errors when accessed

                // availableBalance is optional
                account.availableBalance?.let { avail -> assertTrue(avail.isNotBlank()) }

                // Transaction optional fields
                account.transactions?.forEach { tx ->
                    tx.transactedAt?.let { transactedAt -> assertTrue(transactedAt > 0) }
                }

                // Organization optional fields
                account.org.domain?.let { domain -> assertTrue(domain.isNotBlank()) }

                account.org.name?.let { name -> assertTrue(name.isNotBlank()) }

                account.org.url?.let { url -> assertTrue(url.isNotBlank()) }

                account.org.id?.let { id -> assertTrue(id.isNotBlank()) }
            }
        }
    }
}
