package org.eclipse.birt.spring.core;

import org.eclipse.birt.report.engine.api.IReportEngine;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import javax.sql.DataSource;

/**
 * Simple {@link org.springframework.web.servlet.ViewResolver} that translates the URL of the report into a report name
 * and then resolves it using the appropriate {@link AbstractSingleFormatBirtView Birt view implementation}.
 *
 * @author Josh Long
 * @author Jason Weathersby
 */
public class BirtViewResolver extends UrlBasedViewResolver {
	

	
    private IReportEngine birtEngine;
    private DataSource dataSource;
    private int taskType;
    

    public BirtViewResolver(){
        setViewClass(AbstractSingleFormatBirtView.class);
        setSuffix(".rptdesign");
    }

    public void setBirtEngine(IReportEngine birtEngine) {
        this.birtEngine = birtEngine;
    }

    public void setDataSource(DataSource dataSource){
        this.dataSource = dataSource;
    }

    public void setTaskType(int taskType){
        this.taskType = taskType;
    }

    @Override
    protected AbstractUrlBasedView buildView(String viewName) throws Exception {
        AbstractSingleFormatBirtView view = (AbstractSingleFormatBirtView) super.buildView(viewName);
        view.setDataSource( this.dataSource);
        view.setBirtEngine(this.birtEngine);
        view.setTaskType(this.taskType);
        return view;
    }


    @Override
    protected Class requiredViewClass() {
        return AbstractSingleFormatBirtView.class;
    }


}
