package com.oddy.annotation.entity.student;

import com.oddy.annotation.entity.teacher.Teacher;
import com.oddy.annotation.event.TestEvent;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import java.util.Date;
import lombok.Data;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.scheduling.annotation.Async;

// 2.6 使用注解注册 bean（常用）
// @Component，简单的实例化并注册 bean，使用注解注册 bean 需要在配置类中使用 @ComponentScan 启用组件扫描
@Data
public class Student implements
    ApplicationEventPublisherAware { // 5. Aware 接口，主要用于注册为 bean 时获取一些相关的 Spring 内置组件

  private String name;

  // 5.1 Aware 接口获得的组件，一般保存在成员变量中
  // 6. 事件
  // 6.1 ApplicationEventPublisher 用于发布事件，这里也可以使用自动装配
  private ApplicationEventPublisher applicationEventPublisher;

  // 2.5 自动装配（常用）
  // 2.5.1 Autowired
  // @Autowired，采用 byType 模式，可以添加到 setter/构造方法、字段、方法参数上
  // @Qualifier("teacher")，在 byType 模式下出现冲突时使用该注解指定 bean 的名称
  // 2.5.2 @Resource
  // @Resource，采用 byName 模式，找不到则采用 byType 模式，可以添加到 setter方法、字段上
  // 其匹配机制更加合理高效，因此 IDEA 更推荐在成员变量上使用 @Resource 注入
  // 当然，Spring 官方更加推荐使用基于 setter/构造方法的 @Autowired 注入
  // 总之，@Autowired 和 @Resource 都可以实现自动装配，日常使用中二者相差不大
  @Resource
  private Teacher teacher;

  // bean 属性：相当于 XML 中的 init-method
  @PostConstruct
  public void init() {
    System.out.println("init");
  }

  // bean 属性：相当于 XML 中的 destroy-method
  @PreDestroy
  public void destroy() {
    System.out.println("destroy");
  }

  // 3.1 同步任务
  public void syncTask() throws InterruptedException {
    System.out.println(Thread.currentThread().getName() + "：同步任务开始执行");
    Thread.sleep(3000);
    System.out.println(Thread.currentThread().getName() + "：同步任务执行完毕");
  }

  // 3.2 异步任务，@Async 要求方法返回值是 void 或 Future 类型
  @Async
  public void asyncTask() throws InterruptedException {
    System.out.println(Thread.currentThread().getName() + "：异步任务开始执行");
    Thread.sleep(3000);
    System.out.println(Thread.currentThread().getName() + "：异步任务执行完毕");
  }

  // 4.1 声明定时任务，不需要主动调用此方法
  // @Scheduled(fixedRate = 2000)
  // @Scheduled(cron = "*/2 * * * * *")
  // 使用 cron 表达式
  public void scheduledTask() {
    System.out.println(Thread.currentThread().getName() + "：定时任务执行！" + new Date());
  }

  // 5.2 该 Aware 接口要求实现的方法，Spring 会自动调用，传入相关的组件，我们一般在此方法中保存得到的组件
  @Override
  public void setApplicationEventPublisher(
      @Nonnull ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  // 6.4 发布事件的方法
  public void publishEvent() {
    System.out.println("发布事件");
    applicationEventPublisher.publishEvent(new TestEvent(this));
  }
}
