package org.eclipse.birt.spring.example;

import org.eclipse.birt.spring.core.BirtEngineFactory;
import org.eclipse.birt.spring.core.HtmlSingleFormatBirtView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.BeanNameViewResolver;

import javax.inject.Inject;

/**
 * @author Jason Weathersby
 * @author Josh Long
 */
@EnableWebMvc
@ComponentScan(basePackageClasses = {BirtWebConfiguration.class})
@Configuration
public class BirtWebConfiguration extends WebMvcConfigurerAdapter {

    static private final String BIRT_HTML_VIEW = "htmlBirt";

    @Inject
    private BirtDataServiceConfiguration birtDataServiceConfiguration;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/reports").setViewName(BIRT_HTML_VIEW);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("images/*").addResourceLocations("/images/");
    }

    @Bean(name = BIRT_HTML_VIEW)
    public HtmlSingleFormatBirtView htmlBirt() throws Exception {
        HtmlSingleFormatBirtView htmlSingleFormatBirtView = new HtmlSingleFormatBirtView();
        htmlSingleFormatBirtView.setBirtEngine(this.engine().getObject());
        htmlSingleFormatBirtView.setDataSource(birtDataServiceConfiguration.dataSource());
        return htmlSingleFormatBirtView;
    }

    @Bean
    public ViewResolver beanNameResolver() {
        return new BeanNameViewResolver();
    }

    @Bean
    public BirtEngineFactory engine() {
        return new BirtEngineFactory();
    }

} 