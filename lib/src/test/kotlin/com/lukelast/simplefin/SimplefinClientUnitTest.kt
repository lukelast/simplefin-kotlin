package com.lukelast.simplefin

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

/**
 * Unit tests for SimplefinClient using mocked HTTP client. These tests verify error handling,
 * parameter construction, and edge cases.
 */
class SimplefinClientUnitTest {

    // ========================================
    // Error Scenario Tests
    // ========================================

    @Test
    fun `accounts throws SimplefinApiException on 401 Unauthorized`() {
        runBlocking {
            val mockEngine = MockEngine { request ->
                respond(
                    content = ByteReadChannel("""{"error": "Unauthorized"}"""),
                    status = HttpStatusCode.Unauthorized,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))

            val exception =
                assertThrows<SimplefinApiException> { client.accounts(token = demoAccessUrl) }

            assertEquals(HttpStatusCode.Unauthorized, exception.status)
            assertTrue(exception.message!!.contains("401"))
            assertTrue(exception.responseBody.contains("Unauthorized"))
            client.close()
        }
    }

    @Test
    fun `accounts throws SimplefinApiException on 403 Forbidden`() {
        runBlocking {
            val mockEngine = MockEngine { request ->
                respond(
                    content = ByteReadChannel("""{"error": "Forbidden"}"""),
                    status = HttpStatusCode.Forbidden,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))

            val exception =
                assertThrows<SimplefinApiException> { client.accounts(token = demoAccessUrl) }

            assertEquals(HttpStatusCode.Forbidden, exception.status)
            assertTrue(exception.message!!.contains("403"))
            client.close()
        }
    }

    @Test
    fun `accounts throws SimplefinApiException on 404 Not Found`() {
        runBlocking {
            val mockEngine = MockEngine { request ->
                respond(
                    content = ByteReadChannel("""{"error": "Not Found"}"""),
                    status = HttpStatusCode.NotFound,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))

            val exception =
                assertThrows<SimplefinApiException> { client.accounts(token = demoAccessUrl) }

            assertEquals(HttpStatusCode.NotFound, exception.status)
            assertTrue(exception.message!!.contains("404"))
            client.close()
        }
    }

    @Test
    fun `accounts throws SimplefinApiException on 500 Internal Server Error`() {
        runBlocking {
            val mockEngine = MockEngine { request ->
                respond(
                    content = ByteReadChannel("""{"error": "Internal Server Error"}"""),
                    status = HttpStatusCode.InternalServerError,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))

            val exception =
                assertThrows<SimplefinApiException> { client.accounts(token = demoAccessUrl) }

            assertEquals(HttpStatusCode.InternalServerError, exception.status)
            assertTrue(exception.message!!.contains("500"))
            client.close()
        }
    }

    @Test
    fun `accounts throws SimplefinApiException on 503 Service Unavailable`() {
        runBlocking {
            val mockEngine = MockEngine { request ->
                respond(
                    content = ByteReadChannel("""{"error": "Service Unavailable"}"""),
                    status = HttpStatusCode.ServiceUnavailable,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))

            val exception =
                assertThrows<SimplefinApiException> { client.accounts(token = demoAccessUrl) }

            assertEquals(HttpStatusCode.ServiceUnavailable, exception.status)
            assertTrue(exception.message!!.contains("503"))
            client.close()
        }
    }

    @Test
    fun `fetchAccessUrl throws SetupTokenUsedException on 403 Forbidden`() {
        runBlocking {
            val mockEngine = MockEngine { request ->
                respond(
                    content = ByteReadChannel(""),
                    status = HttpStatusCode.Forbidden,
                    headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))

            assertThrows<SetupTokenUsedException> {
                // Using a valid base64 encoded URL for testing
                val setupToken =
                    "aHR0cHM6Ly9kZW1vOmRlbW9AYmV0YS1icmlkZ2Uuc2ltcGxlZmluLm9yZy9zaW1wbGVmaW4="
                client.fetchAccessUrl(setupToken)
            }

            client.close()
        }
    }

    @Test
    fun `fetchAccessUrl throws SimplefinApiException on other errors`() {
        runBlocking {
            val mockEngine = MockEngine { request ->
                respond(
                    content = ByteReadChannel("""{"error": "Bad Request"}"""),
                    status = HttpStatusCode.BadRequest,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))

            val exception =
                assertThrows<SimplefinApiException> {
                    val setupToken =
                        "aHR0cHM6Ly9kZW1vOmRlbW9AYmV0YS1icmlkZ2Uuc2ltcGxlZmluLm9yZy9zaW1wbGVmaW4="
                    client.fetchAccessUrl(setupToken)
                }

            assertEquals(HttpStatusCode.BadRequest, exception.status)
            client.close()
        }
    }

    // ========================================
    // Mocked Response Tests
    // ========================================

    @Test
    fun `accounts handles minimal valid response`() {
        runBlocking {
            val mockEngine = MockEngine { request ->
                respond(
                    content =
                        ByteReadChannel(
                            """
                            {
                                "errors": [],
                                "accounts": [
                                    {
                                        "org": {"sfin-url": "https://example.com"},
                                        "id": "acc1",
                                        "name": "Test Account",
                                        "currency": "USD",
                                        "balance": "1000.00",
                                        "balance-date": 1700000000
                                    }
                                ]
                            }
                            """
                                .trimIndent()
                        ),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))
            val result = client.accounts(token = demoAccessUrl)

            assertEquals(0, result.errors.size)
            assertEquals(1, result.accounts.size)
            assertEquals("Test Account", result.accounts[0].name)
            assertEquals("USD", result.accounts[0].currency)
            assertEquals("1000.00", result.accounts[0].balance)
            client.close()
        }
    }

    @Test
    fun `accounts handles response with all optional fields populated`() {
        runBlocking {
            val mockEngine = MockEngine { request ->
                respond(
                    content =
                        ByteReadChannel(
                            """
                            {
                                "errors": [],
                                "accounts": [
                                    {
                                        "org": {
                                            "sfin-url": "https://example.com",
                                            "domain": "example.com",
                                            "name": "Example Bank",
                                            "url": "https://example.com",
                                            "id": "bank1"
                                        },
                                        "id": "acc1",
                                        "name": "Test Account",
                                        "currency": "USD",
                                        "balance": "1000.00",
                                        "available-balance": "950.00",
                                        "balance-date": 1700000000,
                                        "transactions": [
                                            {
                                                "id": "tx1",
                                                "posted": 1699000000,
                                                "amount": "-50.00",
                                                "description": "Test purchase",
                                                "payee": "Test Store",
                                                "memo": "Test memo",
                                                "transacted_at": 1699000000,
                                                "pending": false,
                                                "extra": {"category": "shopping"}
                                            }
                                        ],
                                        "holdings": [
                                            {
                                                "id": "hold1",
                                                "created": 1690000000,
                                                "cost_basis": "1000.00",
                                                "currency": "USD",
                                                "description": "Test Stock",
                                                "market_value": "1200.00",
                                                "purchase_price": "100.00",
                                                "shares": "10",
                                                "symbol": "TEST"
                                            }
                                        ],
                                        "extra": {"account_type": "checking"}
                                    }
                                ],
                                "x-api-message": ["Test message"]
                            }
                            """
                                .trimIndent()
                        ),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))
            val result = client.accounts(token = demoAccessUrl)

            // Verify all optional fields are populated
            assertEquals(1, result.accounts.size)
            val account = result.accounts[0]

            assertNotNull(account.availableBalance)
            assertEquals("950.00", account.availableBalance)

            assertNotNull(account.transactions)
            assertEquals(1, account.transactions!!.size)
            val tx = account.transactions!![0]
            assertEquals("tx1", tx.id)
            assertEquals(false, tx.pending)
            assertNotNull(tx.extra)

            assertNotNull(account.holdings)
            assertEquals(1, account.holdings!!.size)
            val holding = account.holdings!![0]
            assertEquals("TEST", holding.symbol)

            assertNotNull(account.extra)
            assertEquals("checking", account.extra!!["account_type"])

            assertNotNull(result.xApiMessage)
            assertEquals(1, result.xApiMessage!!.size)

            client.close()
        }
    }

    @Test
    fun `accounts handles empty accounts list`() {
        runBlocking {
            val mockEngine = MockEngine { request ->
                respond(
                    content =
                        ByteReadChannel(
                            """
                            {
                                "errors": [],
                                "accounts": []
                            }
                            """
                                .trimIndent()
                        ),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))
            val result = client.accounts(token = demoAccessUrl)

            assertEquals(0, result.errors.size)
            assertEquals(0, result.accounts.size)
            client.close()
        }
    }

    @Test
    fun `accounts handles response with errors field populated`() {
        runBlocking {
            val mockEngine = MockEngine { request ->
                respond(
                    content =
                        ByteReadChannel(
                            """
                            {
                                "errors": ["Error 1", "Error 2"],
                                "accounts": []
                            }
                            """
                                .trimIndent()
                        ),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))
            val result = client.accounts(token = demoAccessUrl)

            assertEquals(2, result.errors.size)
            assertTrue(result.errors.contains("Error 1"))
            assertTrue(result.errors.contains("Error 2"))
            client.close()
        }
    }

    @Test
    fun `accounts handles null optional fields`() {
        runBlocking {
            val mockEngine = MockEngine { request ->
                respond(
                    content =
                        ByteReadChannel(
                            """
                            {
                                "errors": [],
                                "accounts": [
                                    {
                                        "org": {"sfin-url": "https://example.com"},
                                        "id": "acc1",
                                        "name": "Test Account",
                                        "currency": "USD",
                                        "balance": "1000.00",
                                        "balance-date": 1700000000
                                    }
                                ]
                            }
                            """
                                .trimIndent()
                        ),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))
            val result = client.accounts(token = demoAccessUrl)

            val account = result.accounts[0]
            assertNull(account.availableBalance)
            assertNull(account.transactions)
            assertNull(account.holdings)
            assertNull(account.extra)
            assertNull(result.xApiMessage)

            client.close()
        }
    }

    @Test
    fun `accounts handles large transaction list`() {
        runBlocking {
            val transactions =
                (1..100).joinToString(",") { i ->
                    """
                {
                    "id": "tx$i",
                    "posted": ${1700000000 - i * 86400},
                    "amount": "${if (i % 2 == 0) "-" else ""}${i * 10}.00",
                    "description": "Transaction $i",
                    "payee": "Payee $i",
                    "memo": "Memo $i"
                }
                """
                        .trimIndent()
                }

            val mockEngine = MockEngine { request ->
                respond(
                    content =
                        ByteReadChannel(
                            """
                        {
                            "errors": [],
                            "accounts": [
                                {
                                    "org": {"sfin-url": "https://example.com"},
                                    "id": "acc1",
                                    "name": "Test Account",
                                    "currency": "USD",
                                    "balance": "1000.00",
                                    "balance-date": 1700000000,
                                    "transactions": [$transactions]
                                }
                            ]
                        }
                    """
                                .trimIndent()
                        ),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))
            val result = client.accounts(token = demoAccessUrl)

            assertEquals(1, result.accounts.size)
            assertEquals(100, result.accounts[0].transactions!!.size)

            // Verify first and last transactions
            val txList = result.accounts[0].transactions!!
            assertEquals("tx1", txList[0].id)
            assertEquals("tx100", txList[99].id)

            client.close()
        }
    }

    @Test
    fun `accounts handles special characters in descriptions`() {
        runBlocking {
            val mockEngine = MockEngine { request ->
                respond(
                    content =
                        ByteReadChannel(
                            """
                            {
                                "errors": [],
                                "accounts": [
                                    {
                                        "org": {"sfin-url": "https://example.com"},
                                        "id": "acc1",
                                        "name": "Test Account™",
                                        "currency": "USD",
                                        "balance": "1000.00",
                                        "balance-date": 1700000000,
                                        "transactions": [
                                            {
                                                "id": "tx1",
                                                "posted": 1699000000,
                                                "amount": "-50.00",
                                                "description": "Café & Restaurant™",
                                                "payee": "José's Store™",
                                                "memo": "Test™ memo™"
                                            }
                                        ]
                                    }
                                ]
                            }
                            """
                                .trimIndent()
                        ),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))
            val result = client.accounts(token = demoAccessUrl)

            assertEquals(1, result.accounts.size)
            val account = result.accounts[0]
            assertEquals("Test Account™", account.name)

            val tx = account.transactions!![0]
            assertEquals("Café & Restaurant™", tx.description)
            assertEquals("José's Store™", tx.payee)
            assertEquals("Test™ memo™", tx.memo)

            client.close()
        }
    }

    // ========================================
    // Parameter Construction Tests
    // ========================================

    @Test
    fun `accounts constructs URL with startDate parameter`() {
        runBlocking {
            var requestUrl: String? = null

            val mockEngine = MockEngine { request ->
                requestUrl = request.url.toString()
                respond(
                    content = ByteReadChannel("""{"errors": [], "accounts": []}"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))
            val startDate = Instant.ofEpochSecond(1700000000)

            client.accounts(token = demoAccessUrl, startDate = startDate)

            assertNotNull(requestUrl)
            assertTrue(requestUrl!!.contains("start-date=1700000000"))
            client.close()
        }
    }

    @Test
    fun `accounts constructs URL with endDate parameter`() {
        runBlocking {
            var requestUrl: String? = null

            val mockEngine = MockEngine { request ->
                requestUrl = request.url.toString()
                respond(
                    content = ByteReadChannel("""{"errors": [], "accounts": []}"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))
            val endDate = Instant.ofEpochSecond(1710000000)

            client.accounts(token = demoAccessUrl, endDate = endDate)

            assertNotNull(requestUrl)
            assertTrue(requestUrl!!.contains("end-date=1710000000"))
            client.close()
        }
    }

    @Test
    fun `accounts constructs URL with pending parameter`() {
        runBlocking {
            var requestUrl: String? = null

            val mockEngine = MockEngine { request ->
                requestUrl = request.url.toString()
                respond(
                    content = ByteReadChannel("""{"errors": [], "accounts": []}"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))

            client.accounts(token = demoAccessUrl, pending = true)

            assertNotNull(requestUrl)
            assertTrue(requestUrl!!.contains("pending=1"))
            client.close()
        }
    }

    @Test
    fun `accounts constructs URL with single account parameter`() {
        runBlocking {
            var requestUrl: String? = null

            val mockEngine = MockEngine { request ->
                requestUrl = request.url.toString()
                respond(
                    content = ByteReadChannel("""{"errors": [], "accounts": []}"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))

            client.accounts(token = demoAccessUrl, accounts = listOf("Demo Checking"))

            assertNotNull(requestUrl)
            println("Actual URL: $requestUrl")
            assertTrue(
                requestUrl!!.contains("account=Demo%20Checking") ||
                    requestUrl.contains("account=Demo+Checking"),
                "URL should contain encoded account parameter. Actual URL: $requestUrl",
            )
            client.close()
        }
    }

    @Test
    fun `accounts constructs URL with multiple account parameters`() {
        runBlocking {
            var requestUrl: String? = null

            val mockEngine = MockEngine { request ->
                requestUrl = request.url.toString()
                respond(
                    content = ByteReadChannel("""{"errors": [], "accounts": []}"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))

            client.accounts(
                token = demoAccessUrl,
                accounts = listOf("Demo Checking", "Demo Savings"),
            )

            assertNotNull(requestUrl)
            println("Actual URL with multiple accounts: $requestUrl")
            assertTrue(
                (requestUrl!!.contains("account=Demo%20Checking") ||
                    requestUrl.contains("account=Demo+Checking")) &&
                    (requestUrl.contains("account=Demo%20Savings") ||
                        requestUrl.contains("account=Demo+Savings")),
                "URL should contain both account parameters. Actual URL: $requestUrl",
            )
            client.close()
        }
    }

    @Test
    fun `accounts constructs URL with balancesOnly parameter`() {
        runBlocking {
            var requestUrl: String? = null

            val mockEngine = MockEngine { request ->
                requestUrl = request.url.toString()
                respond(
                    content = ByteReadChannel("""{"errors": [], "accounts": []}"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))

            client.accounts(token = demoAccessUrl, balancesOnly = true)

            assertNotNull(requestUrl)
            assertTrue(requestUrl!!.contains("balances-only=1"))
            client.close()
        }
    }

    @Test
    fun `accounts constructs URL with all parameters`() {
        runBlocking {
            var requestUrl: String? = null

            val mockEngine = MockEngine { request ->
                requestUrl = request.url.toString()
                respond(
                    content = ByteReadChannel("""{"errors": [], "accounts": []}"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))

            client.accounts(
                token = demoAccessUrl,
                startDate = Instant.ofEpochSecond(1700000000),
                endDate = Instant.ofEpochSecond(1710000000),
                pending = true,
                accounts = listOf("Test Account"),
                balancesOnly = true,
            )

            assertNotNull(requestUrl)
            println("Actual URL with all parameters: $requestUrl")
            assertTrue(requestUrl!!.contains("start-date=1700000000"))
            assertTrue(requestUrl!!.contains("end-date=1710000000"))
            assertTrue(requestUrl!!.contains("pending=1"))
            assertTrue(
                requestUrl.contains("account=Test%20Account") ||
                    requestUrl.contains("account=Test+Account"),
                "URL should contain account parameter. Actual URL: $requestUrl",
            )
            assertTrue(requestUrl!!.contains("balances-only=1"))
            client.close()
        }
    }

    @Test
    fun `accounts sets Basic Auth header correctly`() {
        runBlocking {
            var authHeader: String? = null

            val mockEngine = MockEngine { request ->
                authHeader = request.headers[HttpHeaders.Authorization]
                respond(
                    content = ByteReadChannel("""{"errors": [], "accounts": []}"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))

            client.accounts(token = demoAccessUrl)

            assertNotNull(authHeader)
            assertTrue(authHeader!!.startsWith("Basic "))
            client.close()
        }
    }

    @Test
    fun `accounts sets Accept header correctly`() {
        runBlocking {
            var acceptHeader: String? = null

            val mockEngine = MockEngine { request ->
                acceptHeader = request.headers[HttpHeaders.Accept]
                respond(
                    content = ByteReadChannel("""{"errors": [], "accounts": []}"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))

            client.accounts(token = demoAccessUrl)

            assertNotNull(acceptHeader)
            assertEquals("application/json", acceptHeader)
            client.close()
        }
    }

    // ========================================
    // Business Logic Validation Tests
    // ========================================

    @Test
    fun `accounts parses decimal balances correctly`() {
        runBlocking {
            val mockEngine = MockEngine { request ->
                respond(
                    content =
                        ByteReadChannel(
                            """
                            {
                                "errors": [],
                                "accounts": [
                                    {
                                        "org": {"sfin-url": "https://example.com"},
                                        "id": "acc1",
                                        "name": "Test",
                                        "currency": "USD",
                                        "balance": "1234.56",
                                        "balance-date": 1700000000
                                    }
                                ]
                            }
                            """
                                .trimIndent()
                        ),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))
            val result = client.accounts(token = demoAccessUrl)

            val balance = result.accounts[0].balance.toBigDecimal()
            assertEquals("1234.56", balance.toString())
            client.close()
        }
    }

    @Test
    fun `accounts handles negative transaction amounts`() {
        runBlocking {
            val mockEngine = MockEngine { request ->
                respond(
                    content =
                        ByteReadChannel(
                            """
                            {
                                "errors": [],
                                "accounts": [
                                    {
                                        "org": {"sfin-url": "https://example.com"},
                                        "id": "acc1",
                                        "name": "Test",
                                        "currency": "USD",
                                        "balance": "1000.00",
                                        "balance-date": 1700000000,
                                        "transactions": [
                                            {
                                                "id": "tx1",
                                                "posted": 1699000000,
                                                "amount": "-50.25",
                                                "description": "Expense",
                                                "payee": "Store",
                                                "memo": "Purchase"
                                            }
                                        ]
                                    }
                                ]
                            }
                            """
                                .trimIndent()
                        ),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))
            val result = client.accounts(token = demoAccessUrl)

            val amount = result.accounts[0].transactions!![0].amount.toBigDecimal()
            assertTrue(amount < 0.toBigDecimal())
            assertEquals("-50.25", amount.toString())
            client.close()
        }
    }

    @Test
    fun `accounts validates Unix timestamps are reasonable`() {
        runBlocking {
            val mockEngine = MockEngine { request ->
                respond(
                    content =
                        ByteReadChannel(
                            """
                            {
                                "errors": [],
                                "accounts": [
                                    {
                                        "org": {"sfin-url": "https://example.com"},
                                        "id": "acc1",
                                        "name": "Test",
                                        "currency": "USD",
                                        "balance": "1000.00",
                                        "balance-date": 1700000000
                                    }
                                ]
                            }
                            """
                                .trimIndent()
                        ),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

            val client = SimplefinClient(HttpClient(mockEngine))
            val result = client.accounts(token = demoAccessUrl)

            val balanceDate = result.accounts[0].balanceDate
            val epochYear2000 = 946684800L

            assertTrue(balanceDate > epochYear2000)
            assertTrue(balanceDate < Instant.now().epochSecond + 86400)
            client.close()
        }
    }
}
