package com.oddy.annotation.listener;

import com.oddy.annotation.event.TestEvent;
import jakarta.annotation.Nonnull;
import org.springframework.context.ApplicationListener;

// 6.3 自定义事件监听器
public class TestListener implements ApplicationListener<TestEvent> {

  @Override
  public void onApplicationEvent(@Nonnull TestEvent event) {
    System.out.println("TestListener 接收到了 TestEvent 的事件");
  }
}
