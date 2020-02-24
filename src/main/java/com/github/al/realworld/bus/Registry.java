package com.github.al.realworld.bus;

import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
@Component
public class Registry {

    private final Map<Class<? extends Command>, CommandHandlerProvider> commandHandlerProviders = new HashMap<>();
    private final Map<Class<? extends Query>, QueryHandlerProvider> queryHandlerProviders = new HashMap<>();

    public Registry(ApplicationContext applicationContext) {
        Map<String, CommandHandler> commandHandlers = applicationContext.getBeansOfType(CommandHandler.class);
        commandHandlers.values().forEach(h -> registerCommandHandler(applicationContext, h));

        Map<String, QueryHandler> queryHandlers = applicationContext.getBeansOfType(QueryHandler.class);
        queryHandlers.values().forEach(h -> registerQueryHandler(applicationContext, h));
    }

    private void registerCommandHandler(ApplicationContext applicationContext, CommandHandler commandHandler) {
        Class<?>[] handlerTypes = GenericTypeResolver.resolveTypeArguments(commandHandler.getClass(), CommandHandler.class);
        Class<? extends Command> commandType = (Class<? extends Command>) handlerTypes[1];
        commandHandlerProviders.put(commandType, new CommandHandlerProvider(applicationContext, commandHandler.getClass()));
    }

    private void registerQueryHandler(ApplicationContext applicationContext, QueryHandler queryHandler) {
        Class<?>[] handlerTypes = GenericTypeResolver.resolveTypeArguments(queryHandler.getClass(), QueryHandler.class);
        Class<? extends Query> queryType = (Class<? extends Query>) handlerTypes[1];
        queryHandlerProviders.put(queryType, new QueryHandlerProvider(applicationContext, queryHandler.getClass()));
    }

    <R, C extends Command<R>> CommandHandler<R, C> getCommandHandler(Class<C> commandType) {
        return commandHandlerProviders.get(commandType).get();
    }

    <R, Q extends Query<R>> QueryHandler<R, Q> getQueryHandler(Class<Q> queryType) {
        return queryHandlerProviders.get(queryType).get();
    }

}
