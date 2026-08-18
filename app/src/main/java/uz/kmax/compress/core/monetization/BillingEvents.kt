package uz.kmax.compress.core.monetization

sealed interface BillingEvents {
    data object PurchaseSuccess : BillingEvents
    data class PurchaseError(val message: String) : BillingEvents
    data object RestoreSuccess : BillingEvents
    data object RestoreEmpty : BillingEvents
}
