package edu.villadarez.knockknock.features.visit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VisitModelsTest {

    @Test
    fun myVisitsResponseStoresVisitSummaries() {
        val visit = VisitSummary(
            id = 1L,
            referenceNumber = "KK-SUN-2026-000001",
            condoName = "Sunrise Towers",
            unitNumber = "12A",
            visitDate = "2026-05-20",
            status = "SCHEDULED"
        )

        val response = MyVisitsResponse(visits = listOf(visit))

        assertEquals(1, response.visits.size)
        assertEquals("KK-SUN-2026-000001", response.visits.first().referenceNumber)
        assertEquals("SCHEDULED", response.visits.first().status)
    }

    @Test
    fun notificationsResponseStoresReadAndUnreadItems() {
        val unread = NotificationItem(
            id = 1L,
            title = "Visit confirmed",
            message = "Your visit is confirmed.",
            createdAt = "2026-05-05T08:00:00Z",
            isRead = false
        )
        val read = unread.copy(id = 2L, isRead = true)

        val response = NotificationsResponse(notifications = listOf(unread, read))

        assertFalse(response.notifications.first().isRead)
        assertTrue(response.notifications.last().isRead)
    }
}
