package com.oddy.demospringboot.controller;

import jakarta.servlet.ServletException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

// 专门处理异常
@ControllerAdvice
public class ValidationController {

  @ResponseBody
  @ExceptionHandler(ConstraintViolationException.class)
  public String errorConstraint(ValidationException e) {
    return e.getMessage();
  }

  @ResponseBody
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public String errorParameter(ServletException e) {
    return e.getMessage();
  }

}
