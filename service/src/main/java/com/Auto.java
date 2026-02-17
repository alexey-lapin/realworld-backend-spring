package com;

import com.github.al.realworld.ds.DbqTransactionManager;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Fallback;

import javax.sql.DataSource;

@NullMarked
@AutoConfiguration(after = DataSourceTransactionManagerAutoConfiguration.class)
public class Auto {

    @Fallback
    @Bean
    @ConditionalOnMissingBean
    DbqTransactionManager dbqTransactionManager(DataSource dataSource) {
        return new DbqTransactionManager(dataSource);
    }

}
