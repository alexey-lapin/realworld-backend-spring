package com.github.al.realworld.application.command;

import com.github.al.realworld.bus.CommandHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;

import java.util.Arrays;
import java.util.Map;

@SpringBootTest
public class BeansTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void name() {
        Map<String, CommandHandler> beansOfType = applicationContext.getBeansOfType(CommandHandler.class);
        beansOfType.values().forEach(commandHandler -> {
            Class<?>[] aClass = GenericTypeResolver.resolveTypeArguments(commandHandler.getClass(), CommandHandler.class);
            System.out.println(Arrays.toString(aClass));
        });
//        System.out.println(beansOfType);
    }
}
