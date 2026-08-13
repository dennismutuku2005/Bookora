package com.dennis.bookora.models

data class ClaimRequest(
    val id: String = "",
    val bookId: String = "",
    val bookTitle: String = "",
    val claimerId: String = "",
    val claimerName: String = "",
    val claimerEmail: String = "",
    val claimerPhone: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val status: ClaimStatus = ClaimStatus.PENDING,
    val timestamp: Long = 0L,
    val confirmedByClaimer: Boolean = false,
    val confirmedByOwner: Boolean = false
)

enum class ClaimStatus {
    PENDING,
    ACCEPTED,
    CONFIRMED_CLAIMER,
    CONFIRMED_OWNER,
    COMPLETED,
    REJECTED
}
