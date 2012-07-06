package org.eclipse.birt.spring.example;

import java.util.HashMap;

import org.eclipse.birt.spring.core.AbstractSingleFormatBirtView;
import org.eclipse.birt.spring.core.BirtEngineFactory;
import org.eclipse.birt.spring.core.BirtViewResolver;
import org.eclipse.birt.spring.core.HtmlSingleFormatBirtView;
import org.eclipse.birt.spring.core.MultiFormatBirtView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
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


    @Inject
    private BirtDataServiceConfiguration birtDataServiceConfiguration;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/TopNPercent");
        registry.addViewController("/SampleSpring");
        registry.addViewController("/DashBoard");
        registry.addViewController("/masterreport");
        registry.addViewController("/ProductLines");
        registry.addViewController("/SubReports");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("images/*").addResourceLocations("/images/");
    }

    /*
        HashMap<String, Object> parms = new HashMap<String, Object>();
        parms.put("TopCount", "7");
        parms.put("TopPercentage", "15");
        bvr.setReportParameters(parms);
    */

    @Bean      // this should always be #2 in the resolution chain!
    public BirtViewResolver birtViewResolver() throws Exception {
        BirtViewResolver bvr = new BirtViewResolver();
        bvr.setBirtEngine(this.engine().getObject());
        bvr.setViewClass(HtmlSingleFormatBirtView.class);
        bvr.setDataSource(this.birtDataServiceConfiguration.dataSource());
        bvr.setReportsDirectory("Reports");
        bvr.setOrder(2);
        return bvr;
    }

    @Bean(name = "orderDetails")
    public AbstractSingleFormatBirtView orderDetailsView() throws Throwable {
        HtmlSingleFormatBirtView abstractSingleFormatBirtView = new HtmlSingleFormatBirtView();
        abstractSingleFormatBirtView.setDataSource(birtDataServiceConfiguration.dataSource());
        abstractSingleFormatBirtView.setBirtEngine(engine().getObject());
        abstractSingleFormatBirtView.setReportName("detail.rptdesign");
        abstractSingleFormatBirtView.setReportsDirectory("Reports");
        return abstractSingleFormatBirtView;
    }

    @Bean
    public BeanNameViewResolver beanNameResolver() {
        BeanNameViewResolver bnvr = new BeanNameViewResolver();
        bnvr.setOrder(1);
        return bnvr;
    }

    @Bean
    public BirtEngineFactory engine() {
        return new BirtEngineFactory();
    }

/*
    // todo extricate me to a more logical place
    @Controller
    public static class BirtController {
        @RequestMapping(value = "/DashBoard")
        public void populateView(){
            System.out.println("retrieving DashBoard report...");
        }
    }*/

} 