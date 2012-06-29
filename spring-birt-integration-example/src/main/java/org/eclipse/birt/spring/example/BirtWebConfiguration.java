package org.eclipse.birt.spring.example;

import java.io.File;
import java.util.logging.Level;

import org.eclipse.birt.spring.core.BirtEngineFactory;
import org.eclipse.birt.spring.core.BirtView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.BeanNameViewResolver;

/**
 * @author Jason Weathersby
 * @author Josh Long
 */
@EnableWebMvc
@ComponentScan(basePackageClasses = {BirtWebConfiguration.class})
@Configuration
public class BirtWebConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/reports").setViewName("birtView");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("images/*").addResourceLocations("/images/");
    }

    @Bean
    public BirtView birtView() {
        BirtView view = new BirtView();
        
        view.setBirtEngine(this.engine().getObject());
        
        return view;
    }

    @Bean
    public ViewResolver beanNameResolver() {
        return new BeanNameViewResolver();
        //BirtViewResolver birtViewResolver = new BirtViewResolver();
//        return birtViewResolver ;
    }

    @Bean
    public BirtEngineFactory engine() {
        return new BirtEngineFactory();
    }


} 