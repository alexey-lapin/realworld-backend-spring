package com.github.al.realworld.rest.support;

import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;

public class ServerPortCustomizer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        String port = System.getProperty("server.port");
        if (port != null) {
            factory.setPort(Integer.parseInt(port));
        }
    }

}
