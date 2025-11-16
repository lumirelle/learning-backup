package com.oddy.demossm.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ErrorController {

  @ExceptionHandler(Exception.class)
  public String error(Exception e, Model model) {
    log.error("Exception: ", e);
    model.addAttribute("exception", e);
    return "error";
  }

}
