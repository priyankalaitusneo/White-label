package com.laitsneo.mipPay.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.laitsneo.mipPay.dto.Admin.ResponseDto;

import java.net.ProtocolException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomBadCredentialsException.class)
    public ResponseEntity<ResponseDto> handleCustomBadCredentialsException(CustomBadCredentialsException exception){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseDto.builder().response(exception.getMessage()).status("UNAUTHORIZED").statusCode(401).build());
    }

    @ExceptionHandler(CustomBadRequestException.class)
    public ResponseEntity<ResponseDto> handleCustomBadRequestException(CustomBadRequestException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseDto.builder().response(exception.getMessage()).status("BAD_REQUEST").statusCode(400).build());
    }

    @ExceptionHandler(CustomUnauthorizedException.class)
    public ResponseEntity<ResponseDto> handleCustomBadRequestException(CustomUnauthorizedException exception){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseDto.builder().response(exception.getMessage()).status("UNAUTHORIZED").statusCode(401).build());
    }

    @ExceptionHandler(CustomInternalServerErrorException.class)
    public ResponseEntity<ResponseDto> handleCustomInternalServerErrorException(CustomInternalServerErrorException exception){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseDto.builder().response(exception.getMessage()).status("INTERNAL_SERVER_ERROR").statusCode(504).build());
    }

//    @ExceptionHandler(CustomResourceNotFoundException.class)
//    public ResponseEntity<ResponseDto> handleCustomResourceNotFoundException(CustomResourceNotFoundException exception){
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseDto.builder().response(exception.getMessage()).status("BAD_REQUEST").statusCode(400).build());
//    }

    @ExceptionHandler(CustomDuplicateEntryException.class)
    public ResponseEntity<ResponseDto> handleCustomDuplicateEntryException(CustomDuplicateEntryException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseDto.builder().response(exception.getMessage()).status("BAD_REQUEST").statusCode(400).build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception){

        // If there are validation errors, retrieve error messages
        Map<String,String> errorMessages = new HashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            errorMessages.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(ResponseDto.builder().response(errorMessages).status("BAD_REQUEST").statusCode(400).build());

    }

    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<ResponseDto> handleHttpMessageNotReadableException(HttpMessageConversionException exception){

        System.out.println(exception.getMessage());
        return ResponseEntity.badRequest().body(ResponseDto.builder().response("Please provide correct data").status("BAD_REQUEST").statusCode(400).build());

    }


    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponseDto> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseDto.builder().response("Request Method or Endpoint is incorrect").status("NOT_FOUND").statusCode(404).build());
    }

    @ExceptionHandler(CustomMethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto> methodArgumentNotValidException(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseDto.builder().response("Request Method or Endpoint is incorrect").status("NOT_FOUND").statusCode(404).build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(AccessDeniedException e) {
        return "Access denied: " + e.getMessage();
    }

//    @ExceptionHandler(CustomServerDownException.class)
//    public ResponseEntity<ResponseDto> handleCustomServerDownException(CustomServerDownException ex) {
//        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
//                .body(ResponseDto.builder().response(ex.getMessage()).status("SERVICE_UNAVAILABLE").statusCode(503).build());
//    }

//    @ExceptionHandler(CustomHttpClientErrorException.class)
//    public ResponseEntity<?> handleCustomHttpClientErrorException(CustomHttpClientErrorException ex) {
//        int firstColon = ex.getMessage().indexOf(":");
//        int statusCode = Integer.parseInt(ex.getMessage().substring(0,3));
//        String message = ex.getMessage().substring(firstColon+3,ex.getMessage().length()-1);
//        try{
//            JSONObject jsonObject = new JSONObject(message);
//            return ResponseEntity.status(HttpStatus.valueOf(statusCode))
//                    .body(jsonObject.toMap());
//        }catch (JSONException e){
//            return ResponseEntity.status(HttpStatus.valueOf(statusCode))
//                    .body(message);
//        }
//    }

    @ExceptionHandler(ProtocolException.class)
    public ResponseEntity<ResponseDto> handleProtocolException(ProtocolException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ResponseDto.builder().response("PROTOCOL EXCEPTION : "+ex.getMessage()).status("BAD_GATEWAY").statusCode(504).build());
    }
    
    
    
}


