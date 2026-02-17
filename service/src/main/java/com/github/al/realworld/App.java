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
package com.github.al.realworld;

import com.github.al.realworld.ds.ConnectionAwareTransactionDefinition;
import com.github.al.realworld.ds.DbqTransactionManager;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;

@SpringBootApplication
public class App implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(App.class);
        application.setApplicationStartup(new BufferingApplicationStartup(1000));
        application.run(args);
    }

    @Autowired
    private DataSource dataSource;

    @Autowired
    private A tran;

    @Autowired
    private DbqTransactionManager dbqTransactionManager;

    @Override
    public void run(String... args) throws Exception {
        var connection = dataSource.getConnection();
        var transaction = dbqTransactionManager.getTransaction(new ConnectionAwareTransactionDefinition(connection) {
            @Override
            public @Nullable String getName() {
                return "dbq";
            }
        });
        try {
            tran.run();
            dbqTransactionManager.commit(transaction);
        } catch (Throwable t) {
            dbqTransactionManager.rollback(transaction);
            throw t;
        } finally {
            connection.close();
        }
    }

    @Configuration
    static class Test {

        @Bean
        A tran(B b) {
            return new A(b);
        }

        @Bean
        B b() {
            return new B();
        }

    }

    @RequiredArgsConstructor
    static class A {

        private final B b;

        @Transactional(transactionManager = "dbqTransactionManager")
        public void run() {
            System.out.println("In transaction: " + TransactionSynchronizationManager.isActualTransactionActive());
            b.run();
        }

    }

    static class B {

        @Transactional(transactionManager = "dbqTransactionManager", propagation = Propagation.REQUIRES_NEW)
        public void run() {
            System.out.println("In transaction: " + TransactionSynchronizationManager.isActualTransactionActive());
        }

    }

}
