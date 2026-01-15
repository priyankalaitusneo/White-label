package com.laitsneo.mipPay.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.laitsneo.mipPay.util.JWTHelper;

import java.io.IOException;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private Logger logger = LoggerFactory.getLogger(OncePerRequestFilter.class);

    @Autowired
    private JWTHelper jwtHelper;
    @Autowired
    private UserDetailsService userDetailsService;

//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        String requestHeader = request.getHeader("Authorization");
//        logger.info("Header: {}",requestHeader);
//        String username = null;
//        String token = null;
//
//        if(requestHeader != null && requestHeader.startsWith("Bearer")) {
//            token = requestHeader.substring(7);
//            try{
//
//                username = this.jwtHelper.getUsernameFromToken(token);
//                System.out.println("userName: "+username);
//
//            }catch (IllegalArgumentException exception){
//                logger.info("Illegal argument while fetching the username !!");
//                exception.printStackTrace();
//            }catch (ExpiredJwtException exception){
//                logger.info("Given JWT token is expired !!");
//                exception.printStackTrace();
//            }catch (MalformedJwtException exception){
//                logger.info("Some changes has been done in token. Invalid Token !!");
//                exception.printStackTrace();
//            }catch (Exception exception){
//                logger.info("Something went wrong !!");
//                exception.printStackTrace();
//            }
//        }else{
//            logger.info("Invalid header value !!");
//        }
//
//        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
//
//            //fetch user details from username
//            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
//            //validate token
//            logger.info("Token: {}",token);
//            logger.info("User: {}",userDetails);
//
//            Boolean validateToken = this.jwtHelper.validateToken(token,userDetails);
//            logger.info("validateToken: {}",validateToken);
//
//            if(validateToken){
//
//                //set the authentication
//                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
//                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//            }else {
//                logger.info("validation failed !!");
//            }
//        }
//
//        filterChain.doFilter(request,response);
//    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;
        String username = null;

        // Determine which cookie name to use based on API endpoint
        String requestURI = request.getRequestURI();
        String cookieName = null;

        if (requestURI.startsWith("/payment/admin")) {
            cookieName = "admin-jwt";
        } else if (requestURI.startsWith("/payment/client")) {
            cookieName = "client-jwt";
        }

        // Try to read JWT token from appropriate cookie
        if (cookieName != null && request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie c : request.getCookies()) {
                if (cookieName.equals(c.getName())) {
                    token = c.getValue();
                    break;
                }
            }
        }

        // Fallback to Authorization header
        if (token == null) {
            String requestHeader = request.getHeader("Authorization");
            if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
                token = requestHeader.substring(7);
            }
        }

        // Extract username from JWT
        try {
            if (token != null) {
                username = this.jwtHelper.getUsernameFromToken(token);
            }
        } catch (IllegalArgumentException e) {
            logger.info("Unable to extract username from token");
        } catch (ExpiredJwtException e) {
            logger.info("Token is expired");
        } catch (MalformedJwtException e) {
            logger.info("Token is invalid");
        }

        // Validate token & set authentication context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            Boolean isValid = this.jwtHelper.validateToken(token, userDetails);

            if (isValid) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
