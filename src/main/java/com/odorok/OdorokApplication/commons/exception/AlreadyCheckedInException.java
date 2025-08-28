package com.odorok.OdorokApplication.commons.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AlreadyCheckedInException extends RuntimeException {
    public AlreadyCheckedInException() {
        super("이미 오늘은 출석체크가 완료되었습니다.");
    }
}