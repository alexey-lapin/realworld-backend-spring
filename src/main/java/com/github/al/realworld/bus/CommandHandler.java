package com.github.al.realworld.bus;

public interface CommandHandler<R, C extends Command<R>> {

    R handle(C command);

}
