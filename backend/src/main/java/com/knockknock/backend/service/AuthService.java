package com.knockknock.backend.service;

import com.knockknock.backend.dto.*;

public interface AuthService {
    Object registerVisitor(RegisterVisitorRequest request);
    Object registerCondoAdmin(RegisterCondoAdminRequest request);
    Object login(LoginRequest request);
}