package org.eclipse.birt.spring.core;

import org.eclipse.birt.report.engine.api.IReportEngine;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Simple {@link org.springframework.web.servlet.ViewResolver} that translates the URL of the report into a report name
 * and then resolves it using the appropriate {@link AbstractSingleFormatBirtView Birt view implementation}.
 *
 * @author Josh Long
 * @author Jason Weathersby
 */
public class BirtViewResolver extends UrlBasedViewResolver {


    private String reportsDirectory = "";

    private IReportEngine birtEngine;
    private DataSource dataSource;
    private int taskType;
    private Map reportParameters = null;

    public void setReportsDirectory(String reportsDirectory) {
        this.reportsDirectory = reportsDirectory;
    }


    public BirtViewResolver() {
        setViewClass(AbstractSingleFormatBirtView.class);
        setSuffix(".rptdesign");
    }

    public void setBirtEngine(IReportEngine birtEngine) {
        this.birtEngine = birtEngine;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    public void setReportParameters(Map reportParameters) {
        this.reportParameters = reportParameters;
    }

    @Override
    protected AbstractUrlBasedView buildView(String viewName) throws Exception {
        AbstractSingleFormatBirtView view = (AbstractSingleFormatBirtView) super.buildView(viewName);
        view.setDataSource(this.dataSource);
        view.setBirtEngine(this.birtEngine);
        view.setReportParameters(this.reportParameters);
        view.setReportName(viewName);
        view.setReportsDirectory(this.reportsDirectory);
        return view;
    }


    @Override
    protected Class requiredViewClass() {
        return AbstractSingleFormatBirtView.class;
    }


}
