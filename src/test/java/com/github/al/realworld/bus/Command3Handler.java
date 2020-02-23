package com.github.al.realworld.bus;

import org.springframework.stereotype.Component;

@Component
public class Command3Handler implements CommandHandler<String, Command3> {

    @Override
    public String handle(Command3 command) {
        return "command-3-result";
    }
}
