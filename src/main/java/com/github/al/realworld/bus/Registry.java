package com.github.al.realworld.bus;

import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
@Component
public class Registry {

    private final Map<Class<? extends Command>, CommandHandlerProvider> map = new HashMap<>();

    public Registry(ApplicationContext applicationContext) {
        Map<String, CommandHandler> commandHandlers = applicationContext.getBeansOfType(CommandHandler.class);
        commandHandlers.values().forEach(h -> registerCommandHandler(applicationContext, h));
    }

    private void registerCommandHandler(ApplicationContext applicationContext, CommandHandler commandHandler) {
        Class<?>[] handlerTypes = GenericTypeResolver.resolveTypeArguments(commandHandler.getClass(), CommandHandler.class);
        Class<? extends Command> commandType = (Class<? extends Command>) handlerTypes[1];
        map.put(commandType, new CommandHandlerProvider(applicationContext, commandHandler.getClass()));
    }

    <R, C extends Command<R>> CommandHandler<R, C> getCommandHandler(Class<C> commandType) {
        return map.get(commandType).get();
    }

}
