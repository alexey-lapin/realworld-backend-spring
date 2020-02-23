package com.github.al.realworld.command;

import com.github.al.realworld.bus.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class Command1Handler implements CommandHandler<String, Command1> {

    @Override
    public String handle(Command1 command) {
        return null;
    }
}
