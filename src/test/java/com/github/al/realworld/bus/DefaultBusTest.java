package com.github.al.realworld.bus;

import com.github.al.realworld.command.Command1;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DefaultBusTest {

    @Autowired
    private Bus bus;

    @Test
    void name() {
        String result = bus.executeCommand(new Command3());
        System.out.println(result);
    }

    static class TestCommand1 implements Command<String> {
    }

    @Component
    public static class TestCommand1Handler implements CommandHandler<String, TestCommand1> {
        @Override
        public String handle(TestCommand1 command) {
            return "handle-result-1";
        }
    }
}