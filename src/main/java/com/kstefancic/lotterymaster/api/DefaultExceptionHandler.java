package com.kstefancic.lotterymaster.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;
import javax.xml.bind.ValidationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice(annotations = RestController.class)
public class DefaultExceptionHandler extends ResponseEntityExceptionHandler {

//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public Map<String, String> handleValidationExceptions(
//            MethodArgumentNotValidException ex) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getAllErrors().forEach((error) -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//        return errors;
//    }

    // 404
    @ExceptionHandler({EntityNotFoundException.class})
    protected ResponseEntity<Object> handleNotFound(final RuntimeException ex, final WebRequest request) {
        final ApiError apiError = new ApiError("not-found", ex.getMessage(), getClass().getSimpleName());
        return handleExceptionInternal(ex, apiError, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    // 400
    @ExceptionHandler({ ValidationException.class, javax.validation.ValidationException.class})
    protected ResponseEntity<Object> handleBadRequest(final RuntimeException ex, final WebRequest request) {
        final ApiError apiError = new ApiError("validation-error", ex.getMessage(), ex.getClass().getSimpleName());
        return handleExceptionInternal(ex, apiError, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    // 400
    @ExceptionHandler({ PaymentException.class})
    protected ResponseEntity<Object> handlePaymentError(final RuntimeException ex, final WebRequest request) {
        final ApiError apiError = new ApiError(((PaymentException) ex).getCode(), ex.getMessage(), ex.getClass().getSimpleName());
        return handleExceptionInternal(ex, apiError, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    // 403
    @ExceptionHandler({AccessDeniedException.class})
    protected ResponseEntity<Object> handleForbiddenRequest(final RuntimeException ex, final WebRequest request) {
        final ApiError apiError = new ApiError("access-denied", ex.getMessage(), ex.getClass().getSimpleName());
        return handleExceptionInternal(ex, apiError, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }
    //500
    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleServerError(final Exception ex, final WebRequest request) {
        final ApiError apiError = new ApiError("unknown-error", ex.getMessage(), ex.getClass().getSimpleName());
        return handleExceptionInternal(ex, apiError, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpHeaders headers,
        HttpStatus status,
        WebRequest request) {
        List<String> errors = new ArrayList<String>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }

        ApiError apiError =
            new ApiError("validation-error", "Something went wrong. Please check your data.", ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage());
        return handleExceptionInternal(
            ex, apiError, headers, HttpStatus.BAD_REQUEST, request);
    }
}
