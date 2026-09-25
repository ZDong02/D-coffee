package com.dcoffee.config;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.Assert.assertNotNull;

public class RootConfigTest {
    @Test
    public void rootContextStarts() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(RootConfig.class)) {
            assertNotNull(context.getBeanFactory());
        }
    }
}
