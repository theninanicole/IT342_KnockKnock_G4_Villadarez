package edu.villadarez.knockknock.features.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthModelsTest {

    @Test
    fun loginRequestStoresCredentialsForApiPayload() {
        val request = LoginRequest(
            email = "visitor@example.com",
            password = "Password1"
        )

        assertEquals("visitor@example.com", request.email)
        assertEquals("Password1", request.password)
    }

    @Test
    fun registerVisitorRequestStoresMatchingPasswordFields() {
        val request = RegisterVisitorRequest(
            fullName = "Nina Visitor",
            email = "visitor@example.com",
            contactNumber = "9171234567",
            password = "Password1",
            confirmPassword = "Password1"
        )

        assertEquals("Nina Visitor", request.fullName)
        assertEquals("visitor@example.com", request.email)
        assertEquals("9171234567", request.contactNumber)
        assertEquals(request.password, request.confirmPassword)
    }

    @Test
    fun authResponseAllowsVisitorWithoutCondoAndOptionalMessage() {
        val user = User(
            id = "user-1",
            fullName = "Nina Visitor",
            email = "visitor@example.com",
            role = "VISITOR",
            contactNumber = null
        )
        val response = AuthResponse(user = user, token = "jwt-token")

        assertEquals("jwt-token", response.token)
        assertEquals("VISITOR", response.user.role)
        assertNull(response.user.condo)
        assertNull(response.message)
    }
}
