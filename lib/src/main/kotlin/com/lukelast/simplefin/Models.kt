package canifin.backend.simplefin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountSet(
    val errors: List<String>,
    val accounts: List<Account>,
    @SerialName("x-api-message")
    val xApiMessage: List<String>? = null,
)

@Serializable
data class Account(
    val org: Organization,
    val id: String,
    val name: String,
    val currency: String,
    val balance: String,
    @SerialName("available-balance")
    val availableBalance: String? = null,
    @SerialName("balance-date")
    val balanceDate: Long,
    val transactions: List<Transaction>? = null,
    val holdings: List<Holding>? = null,
    val extra: Map<String, String>? = null
)

@Serializable
data class Holding(
    val id: String,
    val created: Long,
    val cost_basis: String,
    val currency: String,
    val description: String,
    val market_value: String,
    val purchase_price: String,
    val shares: String,
    val symbol: String
)

@Serializable
data class Organization(
    val domain: String? = null,
    @SerialName("sfin-url")
    val sfinUrl: String,
    val name: String? = null,
    val url: String? = null,
    val id: String? = null
)

@Serializable
data class Transaction(
    val id: String,
    val posted: Long,
    val amount: String,
    val description: String,
    val payee: String,
    val memo: String,
    @SerialName("transacted_at")
    val transactedAt: Long? = null,
    val pending: Boolean? = null,
    val extra: Map<String, String>? = null
)
