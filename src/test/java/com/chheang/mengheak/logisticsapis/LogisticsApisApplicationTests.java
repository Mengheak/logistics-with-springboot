package com.chheang.mengheak.logisticsapis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/** Guards the wiring: every mapper, service impl, filter and controller has to resolve. */
@SpringBootTest
class LogisticsApisApplicationTests {

    @Autowired
    private WebApplicationContext context;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
        assertThat(context.getBeanNamesForType(Object.class)).isNotEmpty();
    }
}
