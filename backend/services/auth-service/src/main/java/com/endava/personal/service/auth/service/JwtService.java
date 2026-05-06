package com.endava.personal.service.auth.service;


import com.endava.personal.service.auth.domain.AuthUser;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    public String generateToken(AuthUser authUser) {

        return "dummy-token" + authUser.getEmail();
    }
}
