package com.laitsneo.mipPay.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laitsneo.mipPay.dto.Admin.ResponseDto;
import com.laitsneo.mipPay.util.HelperComponent;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class JWTAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    HelperComponent helperComponent;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        int status = response.getStatus();
        System.out.println(status);
        String requestUri = String.valueOf(request.getRequestURI());
        System.out.println("requestUri: "+requestUri);
        //////////
        Set<String> uris = this.helperComponent.scanURIs();
        /////////
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // Set content type to application/json
        response.setContentType("application/json");
        ResponseDto responseDto = ResponseDto.builder().response("Access denied !!" + authException.getMessage()).status("UNAUTHORIZED").statusCode(401).build();
        if(status == 404 || (status == 200 && !uris.contains(requestUri))){
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            responseDto = ResponseDto.builder().response("Request Endpoint is incorrect").status("NOT_FOUND").statusCode(404).build();
        }
        // Convert the map to JSON string
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(responseDto);

        // Write JSON string to response
        response.getWriter().write(json);
    }
}
