package com.knockknock.backend.service;

import com.knockknock.backend.dto.*;

public interface AuthService {

    UserResponse registerVisitor(RegisterVisitorRequest request);

    UserResponse registerCondoAdmin(RegisterCondoAdminRequest request);

    UserResponse login(LoginRequest request);
}