package com.odorok.OdorokApplication.commons.handler;

import com.odorok.OdorokApplication.commons.exception.*;
import com.odorok.OdorokApplication.commons.response.ResponseRoot;
import com.odorok.OdorokApplication.course.exception.ScheduledDateOverlappingException;
import com.odorok.OdorokApplication.region.exception.InvalidSidoCodeException;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Access;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.nio.file.AccessDeniedException;

import static com.odorok.OdorokApplication.commons.response.CommonResponseBuilder.fail;

@Hidden
//@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

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

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ResponseRoot<Object>> handleUpload(FileUploadException e) {
        log.error("파일 업로드 중 문제발생 - {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(fail("파일 업로드 실패: "));
    }

    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<ResponseRoot<Void>> handleS3(S3Exception e) {
        log.error("S3업로드 실패 - {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(fail("파일 업로드 실패: "));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseRoot<Void>> noPermission(AccessDeniedException e){
        log.error("잘못된 권한에서의 요청 - {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(fail("사용자 권한이 없습니다"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseRoot<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("데이터 제약 조건 위반 - {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(fail("글 작성에 실패했습니다."));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ResponseRoot<Void>> handleEntityNotFound(EntityNotFoundException e) {
        log.warn("엔티티를 찾을 수 없음 - {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(fail("글 작성에 실패했습니다."));
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ResponseRoot<Void>> handleTransaction(TransactionSystemException e) {
        log.error("트랜잭션 오류 - {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(fail("글 작성에 실패했습니다."));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseRoot<Void>> handleRuntime(RuntimeException e) {
        log.error("Unexpected runtime error - {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(fail("런타임 오류"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseRoot<Void>> handleGeneric(Exception e) {
        log.error("예상치 못한 예외 - {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(fail("알 수 없는 오류가 발생했습니다."));
    }

    @ExceptionHandler(InvalidSidoCodeException.class)
    public ResponseEntity<ResponseRoot<Void>> handleInvalidSidoCode(InvalidSidoCodeException e) {
        log.error("시도코드 에러 - {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(fail(e.getMessage()));
    }

    @ExceptionHandler(ScheduledDateOverlappingException.class)
    public ResponseEntity<ResponseRoot<Void>> handleOverlappedSchedule(ScheduledDateOverlappingException e) {
        log.error("중복 방문 코스 예정 에러 - {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(fail(e.getMessage()));
    }

    @ExceptionHandler(AlreadyCheckedInException.class)
    public ResponseEntity<ResponseRoot<Void>> handleOverlappedAttendance(AlreadyCheckedInException e) {
        log.error("이미 출석체크가 완료되었음 - {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(fail(e.getMessage())); 
    }

}
