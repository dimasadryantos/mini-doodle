package com.dimas.minidoodle.api;

import com.dimas.minidoodle.service.ConflictException;
import com.dimas.minidoodle.service.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({ConflictException.class, ObjectOptimisticLockingFailureException.class})
    ProblemDetail conflict(RuntimeException ex) {
        String detail = ex instanceof ConflictException ? ex.getMessage()
                : "The resource was modified concurrently, please retry";
        return problem(HttpStatus.CONFLICT, detail);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail integrity(DataIntegrityViolationException ex) {
        String root = ex.getMostSpecificCause().getMessage();
        if (root != null && root.contains("excl_slot_overlap")) {
            return problem(HttpStatus.CONFLICT, "Slot overlaps an existing slot in this calendar");
        }
        return problem(HttpStatus.CONFLICT, "Request violates a data constraint");
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class,
            ConstraintViolationException.class})
    ProblemDetail badRequest(Exception ex) {
        String detail = ex instanceof MethodArgumentNotValidException manv
                ? manv.getBindingResult().getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                    .reduce((a, b) -> a + "; " + b).orElse("Validation failed")
                : ex.getMessage();
        return problem(HttpStatus.BAD_REQUEST, detail);
    }

    private static ProblemDetail problem(HttpStatus status, String detail) {
        return ProblemDetail.forStatusAndDetail(status, detail);
    }
}