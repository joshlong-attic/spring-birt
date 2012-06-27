package org.eclipse.birt.spring.example;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;

import org.eclipse.birt.spring.core.BirtEngineFactory;
import org.eclipse.birt.spring.core.BirtView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.BeanNameViewResolver;

@EnableWebMvc 
@ComponentScan( {"org.eclipse.birt.spring.core", "org.eclipse.birt.spring.example"})
@Configuration
public class BirtWebConfiguration  extends WebMvcConfigurerAdapter  {

	//@Autowired private CarService carService ;

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/reports").setViewName("birtView");

	}

	@Bean 
	public BirtView birtView(){ 
		BirtView bv = new BirtView();
		//bv.setReportFormatRequestParameter("ReportFormat");
		//bv.setReportNameRequestParameter("ReportName");
		bv.setBirtEngine( this.engine().getObject() );
		return bv; 
	}


	@Bean public BeanNameViewResolver beanNameResolver(){ 
		BeanNameViewResolver br = new BeanNameViewResolver() ;
		return br; 
	} 

	@Bean
	protected BirtEngineFactory engine(){ 
		BirtEngineFactory factory = new BirtEngineFactory() ;  
		//factory.setLogLevel( Level.FINEST);
		//factory.setLogDirectory ( new File ("c:/logs"));
		//factory.setLogDirectory( new FileSystemResource("/logs"));

		return factory ; 
	}
	

} 