package edu.villadarez.knockknock.features.visit

data class MyVisitsResponse(
    val visits: List<VisitSummary>
)

data class VisitSummary(
    val id: String,  
    val referenceNumber: String,
    val condoName: String,
    val unitNumber: String,
    val visitDate: String,
    val status: String, // e.g., "SCHEDULED", "CHECKED-IN", "CHECKED-OUT", etc.
    val purpose: String? = null,
    val qrImageUrl: String? = null
)

data class CreateVisitResponse(
    val visitId: String? = null,
    val referenceNumber: String? = null
)

data class VisitFileDTO(
    val fileId: String,
    val fileName: String,
    val fileUrl: String,
    val filePath: String? = null
)

data class FileUploadRequest(
    val visitId: String,
    val filePath: String,
    val fileUrl: String,
    val fileName: String,
    val fileType: String
)

data class GenerateQrResponse(
    val qrImageUrl: String? = null
)

data class UpdateVisitRequest(
    val unitNumber: String,
    val purpose: String,
    val visitDate: String
)

data class VisitFilesResponse(
    val files: List<VisitFileDTO>
)

data class NotificationsResponse(
    val notifications: List<NotificationItem>
)

data class NotificationItem(
    val notifId: String,
    val type: String? = null,
    val title: String,
    val message: String,
    val createdAt: String,
    val isRead: Boolean
)

data class Condominium(
    val condoId: String,
    val name: String,
    val code: String? = null,
    val address: String? = null,
    val status: String? = null
)

data class CondominiumsResponse(
    val condominiums: List<Condominium>
)

data class AdminVisitsResponse(
    val visits: List<AdminVisit>
)

data class AdminVisit(
    val visitId: String,
    val referenceNumber: String,
    val visitDate: String,
    val status: String,
    val visitor: AdminVisitor? = null,
    val unitNumber: String? = null,
    val purpose: String? = null
)

data class AdminVisitor(
    val visitorId: String? = null,
    val id: String? = null,
    val fullName: String? = null,
    val email: String? = null
)

data class AdminHistoryResponse(
    val history: List<AdminHistoryItem>
)

data class AdminHistoryItem(
    val visitorName: String? = null,
    val referenceNumber: String? = null,
    val transition: String? = null,
    val timestamp: String? = null,
    val modifiedBy: String? = null
)
