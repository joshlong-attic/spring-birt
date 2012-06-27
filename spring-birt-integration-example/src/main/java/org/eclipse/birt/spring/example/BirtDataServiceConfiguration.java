package org.eclipse.birt.spring.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BirtDataServiceConfiguration {

    @Bean
    public CarService carService() {
        return new CarServiceImpl();
    }

}
