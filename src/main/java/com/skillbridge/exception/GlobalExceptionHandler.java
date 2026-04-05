// src/main/java/com/skillbridge/exception/GlobalExceptionHandler.java
package com.skillbridge.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleNotFound(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(HttpStatus.NOT_FOUND);
        mav.addObject("error", ex.getMessage());
        mav.addObject("status", 404);
        return mav;
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ModelAndView handleUnauthorized(UnauthorizedException ex) {
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(HttpStatus.FORBIDDEN);
        mav.addObject("error", ex.getMessage());
        mav.addObject("status", 403);
        return mav;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArg(IllegalArgumentException ex) {
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(HttpStatus.BAD_REQUEST);
        mav.addObject("error", ex.getMessage());
        mav.addObject("status", 400);
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAll(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        mav.addObject("error", "An unexpected error occurred.");
        mav.addObject("status", 500);
        return mav;
    }
}