package org.eclipse.birt.spring.core;

import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class MultiFormatBirtView extends AbstractSingleFormatBirtView {

    public MultiFormatBirtView() {
        setContentType("application/octet-stream");
    }

    @Override
    protected RenderOption renderReport(Map<String, Object> map, HttpServletRequest request, HttpServletResponse response, BirtViewResourcePathCallback resourcePathCallback, Map<String, Object> appContextValuesMap, String reportName, String format, IRenderOption options) throws Throwable {

        RenderOption rOptions = new RenderOption(options);
        rOptions.setOutputFormat(format);
        rOptions.setOutputStream(response.getOutputStream());


    	String oName = reportName;
    	if( oName.toLowerCase().endsWith(".rptdesign")){
    		oName = oName.replaceAll("(?i).rptdesign", "");
    	}
    	String att = oName  + "." + format;
    	response.setHeader("Content-Disposition", "attachment; filename=\"" + att + "\"");

        return rOptions;
    }

}
