package edu.cit.villadarez.knockknock.models

// For POST /auth/login
data class LoginRequest(
    val email: String,
    val password: String
)

// For POST /auth/register/visitor
data class RegisterVisitorRequest(
    val fullName: String,
    val email: String,
    val contactNumber: String,
    val password: String,
    val confirmPassword: String
)

data class RegisterCondoAdminRequest(
    val fullName: String,
    val email: String,
    val contactNumber: String,
    val password: String,
    val confirmPassword: String,

    // Condominium Details
    val condoName: String,
    val condoAddress: String
)

// The User object inside the response
data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val role: String,
    val contactNumber: String?,
    val condo: Condo? = null // Include this for Admin roles
)

data class Condo(
    val id: String,
    val name: String,
    val code: String,
    val address: String
)

// The main response object for Login and Register
data class AuthResponse(
    val user: User,
    val token: String,
    val message: String? = null
)
