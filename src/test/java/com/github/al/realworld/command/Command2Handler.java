package com.github.al.realworld.command;

import com.github.al.realworld.bus.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class Command2Handler implements CommandHandler<String, Command2> {

    @Override
    public String handle(Command2 command) {
        return null;
    }
}
