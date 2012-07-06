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

    static private final String BIRT_MULTI_VIEW = "multiBirt";

    @Inject
    private BirtDataServiceConfiguration birtDataServiceConfiguration;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/TopNPercent");
        registry.addViewController("/SampleSpring");
        registry.addViewController("/DashBoard");
        registry.addViewController("/masterreport");
        registry.addViewController("/detail");
        registry.addViewController("/ProductLines");
        registry.addViewController("/SubReports");                

    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("images/*").addResourceLocations("/images/");
    }

    @Bean
    public BirtViewResolver birtViewResolver () throws Exception {
        BirtViewResolver bvr = new BirtViewResolver();
        bvr.setBirtEngine( this.engine().getObject());
        bvr.setViewClass( HtmlSingleFormatBirtView.class);
        //Supply JDBC Datasource for BIRT Engine to use
        bvr.setDataSource(this.birtDataServiceConfiguration.dataSource());
		//set task type
        //HtmlSingleFormatBirtView.RUNRENDERTASK
        //HtmlSingleFormatBirtView.RUNTHENRENDERTASK
        bvr.setTaskType(HtmlSingleFormatBirtView.RUNRENDERTASK);
        //set report Parameters
        //If none set the URL will be examined, else default parameters will be used.
        HashMap<String, Object> parms = new HashMap<String, Object>();
        parms.put("TopCount", "7");
        parms.put("TopPercentage", "15");
        bvr.setReportParameters(parms);
        bvr.setPrefix("/Reports/");
        return bvr;
    }

  /*  @Bean
    public ViewResolver beanNameResolver() {
        return new BeanNameViewResolver();
    }
*/
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