package com.cool.core.config;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class LogDiscardPolicy implements RejectedExecutionHandler {

    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        log.warn("logTaskExecutor 当前已超过线程池最大队列容量，拒绝策略为丢弃该线程 {}", r.toString());
    }
}
