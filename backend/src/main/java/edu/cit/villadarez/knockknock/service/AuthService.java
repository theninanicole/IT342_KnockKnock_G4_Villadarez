package edu.cit.villadarez.knockknock.service;

import edu.cit.villadarez.knockknock.dto.*;

public interface AuthService {
    Object registerVisitor(RegisterVisitorRequest request);
    Object registerVisitorWithGoogle(GoogleTokenRequest request);
    Object registerCondoAdmin(RegisterCondoAdminRequest request);
    Object registerCondoAdminWithGoogle(RegisterCondoAdminGoogleRequest request);
    Object login(LoginRequest request);
    Object loginWithGoogle(GoogleTokenRequest request);
    Object getCurrentUser(String token);
}