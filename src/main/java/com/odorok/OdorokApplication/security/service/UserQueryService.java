package com.odorok.OdorokApplication.security.service;


import com.odorok.OdorokApplication.domain.User;

public interface UserQueryService {
    User queryUserByEmail(String email);
    boolean existsByEmail(String email);
}
