package org.eclipse.birt.spring.core;

import org.eclipse.birt.report.engine.api.*;
import org.springframework.util.Assert;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * @author Jason Weathersby
 * @author Josh Long
 */
public class BirtView extends AbstractView {


    public static final String PARAM_ISNULL = "__isnull";
    public static final String UTF_8_ENCODE = "UTF-8";

    private IReportEngine birtEngine;
    private String reportNameRequestParameter = "ReportName";
    private String imagesDirectory = "images" ;
    private String reportFormatRequestParameter = "ReportFormat";
    private IRenderOption renderOptions;

    public void setRenderOptions(IRenderOption ro) {
        this.renderOptions = ro;
    }

    public void setReportFormatRequestParameter(String rf) {
        Assert.hasText(rf, "the report format parameter must not be null");
        this.reportFormatRequestParameter = rf;
    }

    public void setReportNameRequestParameter(String rn) {
        Assert.hasText(rn, "the reportNameRequestParameter must not be null");
        this.reportNameRequestParameter = rn;
    }

    public void setImagesDirectory(String imagesDirectory) {
        this.imagesDirectory = imagesDirectory;
    }

    protected void renderMergedOutputModel( Map map, HttpServletRequest request, HttpServletResponse response) throws Exception {

        String reportName = request.getParameter(this.reportNameRequestParameter);
        String format = request.getParameter(this.reportFormatRequestParameter);
        ServletContext sc = request.getSession().getServletContext();
        if (format == null) {
            format = "html";
        }

        IReportRunnable runnable = null;
        runnable = birtEngine.openReportDesign(sc.getRealPath("/Reports") + "/" + reportName);
        IRunAndRenderTask runAndRenderTask = birtEngine.createRunAndRenderTask(runnable);
        runAndRenderTask.setParameterValues(discoverAndSetParameters(runnable, request));

        response.setContentType(birtEngine.getMIMEType(format));
        IRenderOption options = null == this.renderOptions ? new RenderOption() : this.renderOptions;
        if (format.equalsIgnoreCase("html")) {
            HTMLRenderOption htmlOptions = new HTMLRenderOption(options);
            htmlOptions.setOutputFormat("html");
            htmlOptions.setOutputStream(response.getOutputStream());
            htmlOptions.setImageHandler(new HTMLServerImageHandler());
            htmlOptions.setBaseImageURL(request.getContextPath() + "/"+ imagesDirectory);
            htmlOptions.setImageDirectory(sc.getRealPath("/"+ imagesDirectory));
            runAndRenderTask.setRenderOption(htmlOptions);

        } else if (format.equalsIgnoreCase("pdf")) {
            PDFRenderOption pdfOptions = new PDFRenderOption(options);
            pdfOptions.setOutputFormat("pdf");
            pdfOptions.setOption(IPDFRenderOption.PAGE_OVERFLOW, IPDFRenderOption.FIT_TO_PAGE_SIZE);
            pdfOptions.setOutputStream(response.getOutputStream());
            runAndRenderTask.setRenderOption(pdfOptions);
        } else {

            String att = "download." + format;
            String uReportName = reportName.toUpperCase();
            if (uReportName.endsWith(".RPTDESIGN")) {
                att = uReportName.replace(".RPTDESIGN", "." + format);
            }

            try {
                // Create file
                FileWriter fstream = new FileWriter("c:/test/out.txt");
                BufferedWriter out = new BufferedWriter(fstream);
                out.write("Hello Java " + format + "--" + birtEngine.getMIMEType(format));
                //Close the output stream
                out.close();
            } catch (Exception e) {//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }

            response.setHeader("Content-Disposition", "attachment; filename=\"" + att + "\"");
            options.setOutputStream(response.getOutputStream());
            options.setOutputFormat(format);
            runAndRenderTask.setRenderOption(options);
        }
        runAndRenderTask.getAppContext().put(EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST, request);
        runAndRenderTask.run();
        runAndRenderTask.close();

    }

    protected HashMap discoverAndSetParameters(IReportRunnable report, HttpServletRequest request) throws Exception {

        HashMap<String, Object> parms = new HashMap<String, Object>();
        IGetParameterDefinitionTask task = birtEngine.createGetParameterDefinitionTask(report);

        @SuppressWarnings("unchecked")
        Collection<IParameterDefnBase> params = task.getParameterDefns(true);
        Iterator<IParameterDefnBase> iter = params.iterator();
        while (iter.hasNext()) {
            IParameterDefnBase param = (IParameterDefnBase) iter.next();

            IScalarParameterDefn scalar = (IScalarParameterDefn) param;
            if (request.getParameter(param.getName()) != null) {
                parms.put(param.getName(), getParamValueObject(request, scalar));
            }
        }
        task.close();
        return parms;
    }

    protected Object getParamValueObject(HttpServletRequest request,
                                         IScalarParameterDefn parameterObj) throws Exception {
        String paramName = parameterObj.getName();
        String format = parameterObj.getDisplayFormat();
        if (doesReportParameterExist(request, paramName)) {
            ReportParameterConverter converter = new ReportParameterConverter(
                    format, request.getLocale());
            // Get value from http request
            String paramValue = getReportParameter(request,
                    paramName, null);
            return converter.parse(paramValue, parameterObj.getDataType());
        }
        return null;
    }

    public static String getReportParameter(HttpServletRequest request,
                                            String name, String defaultValue) {
        assert request != null && name != null;

        String value = getParameter(request, name);
        if (value == null || value.length() <= 0)
         // Treat
        // it as blank value.
        {
            value = ""; //$NON-NLS-1$
        }

        Map paramMap = request.getParameterMap();
        if (paramMap == null || !paramMap.containsKey(name)) {
            value = defaultValue;
        }

        Set nullParams = getParameterValues(request, PARAM_ISNULL);

        if (nullParams != null && nullParams.contains(name)) {
            value = null;
        }

        return value;
    }

    public static boolean doesReportParameterExist(HttpServletRequest request,
                                                   String name) {
        assert request != null && name != null;

        boolean isExist = false;

        Map paramMap = request.getParameterMap();
        if (paramMap != null) {
            isExist = (paramMap.containsKey(name));
        }
        Set nullParams = getParameterValues(request, PARAM_ISNULL);
        if (nullParams != null && nullParams.contains(name)) {
            isExist = true;
        }

        return isExist;
    }

    public static String getParameter(HttpServletRequest request,
                                      String parameterName) {

        if (request.getCharacterEncoding() == null) {
            try {
                request.setCharacterEncoding(UTF_8_ENCODE);
            } catch (UnsupportedEncodingException e) {
            }
        }
        return request.getParameter(parameterName);
    }

    //allows setting parameter values to null using __isnull
    public static Set getParameterValues(HttpServletRequest request,
                                         String parameterName) {
        Set<String> parameterValues = null;
        String[] parameterValuesArray = request.getParameterValues(parameterName);

        if (parameterValuesArray != null) {
            parameterValues = new LinkedHashSet<String>();

            for (int i = 0; i < parameterValuesArray.length; i++) {
                parameterValues.add(parameterValuesArray[i]);
            }
        }

        return parameterValues;
    }

    public void setBirtEngine(IReportEngine birtEngine) {
        this.birtEngine = birtEngine;
    }

}