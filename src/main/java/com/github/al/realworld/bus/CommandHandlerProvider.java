package com.github.al.realworld.bus;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;

@RequiredArgsConstructor
class CommandHandlerProvider<H extends CommandHandler<?, ?>> {

    private final ApplicationContext applicationContext;
    private final Class<H> type;

    H get() {
        return applicationContext.getBean(type);
    }
}
