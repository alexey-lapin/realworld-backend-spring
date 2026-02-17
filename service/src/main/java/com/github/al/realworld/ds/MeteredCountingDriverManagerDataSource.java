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
package com.github.al.realworld.ds;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.jdbc.datasource.DelegatingDataSource;

/**
 * {@link DelegatingDataSource} that publishes connection counters from
 * {@link CountingDriverManagerDataSource} to Micrometer.
 */
public class MeteredCountingDriverManagerDataSource extends DelegatingDataSource {

    /**
     * Meter name for the cumulative opened connection count.
     */
    static final String OPENED_CONNECTIONS_METER_NAME = "realworld.datasource.connections.opened";

    /**
     * Meter name for the current active connection count.
     */
    static final String ACTIVE_CONNECTIONS_METER_NAME = "realworld.datasource.connections.active";

    /**
     * Creates and registers Micrometer meters for connection counters.
     *
     * @param dataSource     counting data source that provides counter values
     * @param meterRegistry  target meter registry
     * @param dataSourceName value for the {@code datasource} meter tag
     */
    public MeteredCountingDriverManagerDataSource(
            CountingDriverManagerDataSource dataSource,
            MeterRegistry meterRegistry,
            String dataSourceName
    ) {
        super(dataSource);

        var tags = Tags.of("datasource", dataSourceName);

        FunctionCounter.builder(
                        OPENED_CONNECTIONS_METER_NAME,
                        dataSource,
                        CountingDriverManagerDataSource::getOpenedConnectionCount
                )
                .description("Total number of opened JDBC connections")
                .baseUnit("connections")
                .tags(tags)
                .register(meterRegistry);

        Gauge.builder(
                        ACTIVE_CONNECTIONS_METER_NAME,
                        dataSource,
                        CountingDriverManagerDataSource::getActiveConnectionCount
                )
                .description("Current number of active JDBC connections")
                .baseUnit("connections")
                .tags(tags)
                .register(meterRegistry);
    }

}
