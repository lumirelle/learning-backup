package com.oddy.annotation.event;

import org.springframework.context.ApplicationEvent;

// 6.2 自定义事件
public class TestEvent extends ApplicationEvent {

  public TestEvent(Object source) {
    super(source);
  }
}
