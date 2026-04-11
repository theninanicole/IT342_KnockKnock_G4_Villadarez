package edu.cit.villadarez.knockknock.models

data class MyVisitsResponse(
    val visits: List<VisitSummary>
)

data class VisitSummary(
    val id: Long,
    val referenceNumber: String,
    val condoName: String,
    val unitNumber: String,
    val visitDate: String,
    val status: String // e.g., "PENDING", "APPROVED"
)

data class NotificationsResponse(
    val notifications: List<NotificationItem>
)

data class NotificationItem(
    val id: Long,
    val title: String,
    val message: String,
    val createdAt: String,
    val isRead: Boolean
)
