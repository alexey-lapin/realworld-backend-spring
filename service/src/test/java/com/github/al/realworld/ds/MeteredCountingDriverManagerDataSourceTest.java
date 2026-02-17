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

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MeteredCountingDriverManagerDataSourceTest {

    @Test
    void shouldReportOpenedAndActiveConnectionMetrics() throws Exception {
        var meterRegistry = new SimpleMeterRegistry();
        var delegate = new CountingDriverManagerDataSource("jdbc:h2:mem:ds-metrics;DB_CLOSE_DELAY=-1", "sa", "");
        var dataSource = new MeteredCountingDriverManagerDataSource(delegate, meterRegistry, "test");

        assertThat(openedConnections(meterRegistry)).isEqualTo(0.0);
        assertThat(activeConnections(meterRegistry)).isEqualTo(0.0);

        var firstConnection = dataSource.getConnection();
        var secondConnection = dataSource.getConnection();

        assertThat(openedConnections(meterRegistry)).isEqualTo(2.0);
        assertThat(activeConnections(meterRegistry)).isEqualTo(2.0);

        secondConnection.close();
        firstConnection.close();
        firstConnection.close();

        assertThat(openedConnections(meterRegistry)).isEqualTo(2.0);
        assertThat(activeConnections(meterRegistry)).isEqualTo(0.0);
    }

    private static double openedConnections(SimpleMeterRegistry meterRegistry) {
        return meterRegistry.get(MeteredCountingDriverManagerDataSource.OPENED_CONNECTIONS_METER_NAME)
                .tag("datasource", "test")
                .functionCounter()
                .count();
    }

    private static double activeConnections(SimpleMeterRegistry meterRegistry) {
        return meterRegistry.get(MeteredCountingDriverManagerDataSource.ACTIVE_CONNECTIONS_METER_NAME)
                .tag("datasource", "test")
                .gauge()
                .value();
    }

}
