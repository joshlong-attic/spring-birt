package org.eclipse.birt.spring.core;

import org.eclipse.birt.report.engine.api.*;
import org.eclipse.birt.report.model.api.IModuleOption;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.*;

/**
 * @author Jason Weathersby
 * @author Josh Long
 */
public class BirtView extends AbstractView implements InitializingBean {

    private IReportEngine birtEngine;
    private String reportNameRequestParameter = "reportName";
    private String imagesDirectory = "images";
    private String reportsDirectory = "reports";
    private String resourceDirectory = "resources";
    private String isNullParameterName = "__isnull";
    private String reportFormatRequestParameter = "reportFormat";
    private IRenderOption renderOptions;
    private String requestEncoding = "UTF-8";

    /**
     * Perform common validation on the state of this object
     *
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(this.requestEncoding, "the requestEncoding must be set");
        Assert.hasText(this.reportFormatRequestParameter, "the 'reportFormatRequestParameter' must not be null");
        Assert.hasText(this.reportNameRequestParameter, "the 'reportNameRequestParameter' must not be null");



        if (birtViewResourcePathCallback == null)
            birtViewResourcePathCallback = new SimpleBirtViewResourcePathPathCallback(reportsDirectory, imagesDirectory, resourceDirectory);

    }

    public void setRequestEncoding(String r) {
        this.requestEncoding = r;
    }
    public void setResourceDirectory(String resourceDirectory) {
        this.resourceDirectory = resourceDirectory;
    }
    public void setReportsDirectory(String reportsDirectory) {
        this.reportsDirectory = reportsDirectory;
    }

    public void setRenderOptions(IRenderOption ro) {
        this.renderOptions = ro;
    }

    public void setReportFormatRequestParameter(String rf) {
        this.reportFormatRequestParameter = rf;
    }

    public void setReportNameRequestParameter(String rn) {
        this.reportNameRequestParameter = rn;
    }

    public void setImagesDirectory(String imagesDirectory) {
        this.imagesDirectory = imagesDirectory;
    }


    /**
     * callback interface.
     */
    public static interface BirtViewResourcePathCallback {
        String baseImageUrl(ServletContext sc, HttpServletRequest r, String reportName) throws Throwable;

        String baseUrl(ServletContext sc, HttpServletRequest r, String reportName) throws Throwable;
        
        String pathForReport(ServletContext servletContext, HttpServletRequest r, String reportName) throws Throwable;

        String imageDirectory(ServletContext sc, HttpServletRequest request, String reportName);

        String resourceDirectory(ServletContext sc, HttpServletRequest request, String reportName);
        
        
    }

    /**
     * Default implementation of the {@link BirtViewResourcePathCallback }
     */
    public static class SimpleBirtViewResourcePathPathCallback implements BirtViewResourcePathCallback {

        private String reportFolder, imagesFolder, resourceFolder;

        public String baseImageUrl(ServletContext sc, HttpServletRequest request, String reportName) throws Throwable {
            return request.getContextPath() + "/" + imagesFolder;
        }
        
        public String baseUrl(ServletContext sc, HttpServletRequest request, String reportName) throws Throwable {
            return request.getRequestURI();
        }

        public String pathForReport(ServletContext sc, HttpServletRequest request, String reportName) throws Throwable {
            return sc.getRealPath("/" + reportFolder) + "/" + reportName;
        }

        public String imageDirectory(ServletContext sc, HttpServletRequest request, String reportName) {
            return sc.getRealPath("/" + imagesFolder);
        }
        
        public String resourceDirectory(ServletContext sc, HttpServletRequest request, String reportName) {
            return sc.getRealPath("/" + resourceFolder);
        }

        public SimpleBirtViewResourcePathPathCallback(String f, String i, String k) {
            this.reportFolder = f;
            this.imagesFolder = i;
            this.resourceFolder = k;
            Assert.hasText(this.reportFolder, "you must provide a valid report folder value");
            Assert.hasText(this.imagesFolder, "you must provide a valid images folder value");
            Assert.hasText(this.resourceFolder, "you must provide a valid resource folder value");
        }
    }

    // important object
    private BirtViewResourcePathCallback birtViewResourcePathCallback;

    @SuppressWarnings("unchecked")
    protected void renderMergedOutputModel(Map map, HttpServletRequest request, HttpServletResponse response) throws Exception {

        try {
            String reportName = request.getParameter(this.reportNameRequestParameter);
            String format = request.getParameter(this.reportFormatRequestParameter);
            ServletContext sc = request.getSession().getServletContext();
            if (format == null) {
                format = "html";
            }
            
    		Map moptions = new HashMap( );
    		moptions.put( IModuleOption.RESOURCE_FOLDER_KEY, birtViewResourcePathCallback.resourceDirectory(sc, request, reportName) );
    		moptions.put( IModuleOption.PARSER_SEMANTIC_CHECK_KEY, Boolean.FALSE );
    		FileInputStream fis = new FileInputStream(birtViewResourcePathCallback.pathForReport(sc, request, reportName));
            IReportRunnable runnable = birtEngine.openReportDesign(reportName,fis, moptions);
            IRunAndRenderTask runAndRenderTask = birtEngine.createRunAndRenderTask(runnable);
            runAndRenderTask.setParameterValues(discoverAndSetParameters(runnable, request));

            response.setContentType(birtEngine.getMIMEType(format));
            IRenderOption options = null == this.renderOptions ? new RenderOption() : this.renderOptions;
            SpringActionHandler sAH = new SpringActionHandler(this.reportNameRequestParameter,this.reportFormatRequestParameter );
            options.setActionHandler(sAH);
            
            if (format.equalsIgnoreCase("html")) {
                HTMLRenderOption htmlOptions = new HTMLRenderOption(options);
                htmlOptions.setOutputFormat("html");
                htmlOptions.setOutputStream(response.getOutputStream());
                htmlOptions.setImageHandler(new HTMLServerImageHandler());
                htmlOptions.setBaseImageURL(birtViewResourcePathCallback.baseImageUrl(sc, request, reportName));
                htmlOptions.setImageDirectory(birtViewResourcePathCallback.imageDirectory(sc, request, reportName));
                htmlOptions.setBaseURL(birtViewResourcePathCallback.baseUrl(sc, request, reportName));
                
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
            //runAndRenderTask.getAppContext().put("birt.viewer.resource.path", birtViewResourcePathCallback.resourceDirectory(sc, request, reportName));
            runAndRenderTask.run();
            runAndRenderTask.close();
           
            fis.close();
            

        } catch (Throwable th) {
            throw new RuntimeException(th); // nothing useful to do here
        }
    }

    private Map<String, Object> discoverAndSetParameters(IReportRunnable report, HttpServletRequest request) throws Throwable {
        HashMap<String, Object> parms = new HashMap<String, Object>();
        IGetParameterDefinitionTask task = birtEngine.createGetParameterDefinitionTask(report);
        @SuppressWarnings("unchecked")
        Collection<IParameterDefnBase> params = task.getParameterDefns(true);
        for (IParameterDefnBase param : params) {
            Assert.isInstanceOf(IScalarParameterDefn.class, param, "the parameter must be assignable to " + IScalarParameterDefn.class.getName());
            IScalarParameterDefn scalar = (IScalarParameterDefn) param;
            if (StringUtils.hasText(getParameter(request, param.getName())))
                parms.put(param.getName(), getParamValueObject(request, scalar));
        }
        task.close();
        return parms;
    }

    private Object getParamValueObject(HttpServletRequest request, IScalarParameterDefn parameterObj) throws Throwable {
        String paramName = parameterObj.getName();
        String format = parameterObj.getDisplayFormat();
        if (doesReportParameterExist(request, paramName)) {
            ReportParameterConverter converter = new ReportParameterConverter(format, request.getLocale());
            String paramValue = getReportParameter(request, paramName, null);
            return converter.parse(paramValue, parameterObj.getDataType());
        }
        return null;
    }

    private String getReportParameter(HttpServletRequest request, String name, String defaultValue) throws Throwable {
        Assert.notNull(request, "the HttpServletRequest must be non-null");
        Assert.hasText(name, "the parameter name must be specified");
        String value = getParameter(request, name);
        Map<String, String[]> paramMap = request.getParameterMap();
        if (paramMap == null || !paramMap.containsKey(name)) {
            value = defaultValue;
        }
        Set<String> nullParams = getParameterValues(request, isNullParameterName);
        if (nullParams != null && nullParams.contains(name)) {
            value = null;
        }
        return value;
    }

    private boolean doesReportParameterExist(HttpServletRequest request, String name) throws Throwable {
        Assert.notNull(request, "the HttpServletRequest must not be null");
        Assert.hasText(name, "the name of the parameter must not be null");
        Map<String, String[]> paramMap = request.getParameterMap();
        Set<String> nullParams = getParameterValues(request, isNullParameterName);
        boolean exists = false;

        if (paramMap != null)
            exists = paramMap.containsKey(name);

        if (nullParams.contains(name))
            exists = true;

        return exists;
    }

    public void setBirtEngine(IReportEngine birtEngine) {
        this.birtEngine = birtEngine;
    }

    private Set<String> getParameterValues(HttpServletRequest request, String parameterName) throws Throwable {
        handleEncodingInRequest(request);
        String[] parameterValuesArray = request.getParameterValues(parameterName);
        Set<String> parameterValues = new LinkedHashSet<String>();
        Collections.addAll(parameterValues, null == parameterValuesArray ? new String[0] : parameterValuesArray);
        return parameterValues;
    }

    private void handleEncodingInRequest(HttpServletRequest request) throws Throwable {
        if (!StringUtils.hasText(request.getCharacterEncoding())) {
            request.setCharacterEncoding(this.requestEncoding);
        }
    }

    private String getParameter(HttpServletRequest request, String parameterName) throws Throwable {
        handleEncodingInRequest(request);
        String result = request.getParameter(parameterName);
        return StringUtils.hasText(result) ? result : "";
    }
}