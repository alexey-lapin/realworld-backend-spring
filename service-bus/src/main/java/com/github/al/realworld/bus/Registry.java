/*
 * MIT License
 *
 * Copyright (c) 2020 - present Alexey Lapin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
