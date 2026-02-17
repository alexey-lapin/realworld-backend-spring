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
package com.github.al.realworld.infrastructure.config;

import com.github.al.realworld.ds.CountingDriverManagerDataSource;
import com.github.al.realworld.ds.MeteredCountingDriverManagerDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.health.registry.HealthContributorRegistry;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Fallback;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH");
    }

    @Bean
    public Object registryCustomizer(HealthContributorRegistry registry) {
        return new SmartInitializingSingleton() {
            @Override
            public void afterSingletonsInstantiated() {
                System.out.println(registry);
                registry.unregisterContributor("secondaryDataSource");
            }
        };
    }

    @Bean
    public DataSource primaryDataSource(DataSourceProperties properties, MeterRegistry meterRegistry) {
        return createMeteredDataSource(properties, meterRegistry, "primary");
    }

    @Fallback
    @Bean
    public DataSource secondaryDataSource(DataSourceProperties properties, MeterRegistry meterRegistry) {
        return createMeteredDataSource(properties, meterRegistry, "secondary");
    }

    private DataSource createMeteredDataSource(
            DataSourceProperties properties,
            MeterRegistry meterRegistry,
            String dataSourceName
    ) {
        var dataSource = properties.initializeDataSourceBuilder()
                .type(CountingDriverManagerDataSource.class)
                .build();
        return new MeteredCountingDriverManagerDataSource(dataSource, meterRegistry, dataSourceName);
    }

}
