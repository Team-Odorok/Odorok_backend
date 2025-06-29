package com.odorok.OdorokApplication.commons.handler;

import com.odorok.OdorokApplication.commons.exception.BadRequestException;
import com.odorok.OdorokApplication.commons.exception.GptCommunicationException;
import com.odorok.OdorokApplication.commons.exception.NotFoundException;
import com.odorok.OdorokApplication.s3.exception.FileUploadException;
import com.odorok.OdorokApplication.s3.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.odorok.OdorokApplication.commons.response.CommonResponseBuilder.fail;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<String> handleUpload(FileUploadException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("파일 업로드 실패: " + e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorizedException(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("런타임 오류: " + e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequest(BadRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(fail(e.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(fail(e.getMessage()));
    }

    @ExceptionHandler(GptCommunicationException.class)
    public ResponseEntity<?> handleGptCommunication(GptCommunicationException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(fail(e.getMessage()));

    }
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(fail(e.getMessage()));
    }
}