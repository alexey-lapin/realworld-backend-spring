package com.github.al.realworld.bus;

public interface Bus {

    <R, C extends Command<R>> R executeCommand(C command);


}
