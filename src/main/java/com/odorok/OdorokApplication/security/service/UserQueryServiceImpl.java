package com.odorok.OdorokApplication.security.service;

import com.odorok.OdorokApplication.domain.User;
import com.odorok.OdorokApplication.security.exception.EmailNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.odorok.OdorokApplication.security.repository.UserRepository;
@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService{
    private final UserRepository userRepository;

    @Override
    public User queryUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(()->new EmailNotFoundException("전달된 이메일을 찾을 수 없습니다. (이메일 : "+email+")"));
    }
}
