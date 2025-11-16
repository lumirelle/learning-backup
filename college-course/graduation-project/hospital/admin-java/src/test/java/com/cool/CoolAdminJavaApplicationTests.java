package com.cool;

import cn.hutool.core.date.DateUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
class CoolAdminJavaApplicationTests {

    @Test
    void thisWeek() {
        Date beginOfThisWeek = DateUtil.beginOfWeek(DateUtil.date());
        Date endOfThisWeek = DateUtil.endOfWeek(DateUtil.date());
        System.out.println("Begin of this week: " + beginOfThisWeek);
        System.out.println("End of this week: " + endOfThisWeek);
    }

}
