package org.eclipse.birt.spring.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Driver;

/**
 * @author Jason Weathersby
 * @author Josh Long
 */
@EnableTransactionManagement
@Configuration
@PropertySource("/config.properties")
public class BirtDataServiceConfiguration {

    @Inject
    private Environment environment;

    @Bean
    public PlatformTransactionManager transactionManager() throws Exception {
        return new DataSourceTransactionManager(this.dataSource());
    }

    @Bean
    @SuppressWarnings("unchecked")
    public DataSource dataSource() throws Exception {
        /*       return new EmbeddedDatabaseBuilder()
        .setName("crm")
        .setType(EmbeddedDatabaseType.H2)
        .build();*/

        // here's how you would do this if you were connecting to a
        // regular DS (perhaps a real PostgreSQL instance or a non in-memory H2 instance)
        SimpleDriverDataSource simpleDriverDataSource = new SimpleDriverDataSource();

        Class<? extends Driver> d = (Class<? extends Driver>) Class.forName(environment.getProperty("ds.driverClass"));

        String user = environment.getProperty("ds.user"),
                pw = environment.getProperty("ds.password"),
                url = environment.getProperty("ds.url");

        simpleDriverDataSource.setDriverClass(d);
        simpleDriverDataSource.setUsername(user);
        simpleDriverDataSource.setPassword(pw);
        simpleDriverDataSource.setUrl(url);
        return simpleDriverDataSource;

    }

    @Bean
    public CarService carService() {
        return new CarServiceImpl();
    }

}
