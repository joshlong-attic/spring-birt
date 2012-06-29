package org.eclipse.birt.spring.core;

import org.apache.commons.io.IOUtils;
import org.eclipse.birt.report.engine.api.*;
import org.eclipse.birt.report.model.api.IModuleOption;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.util.*;


/**
 * Base class for BIRT view hierarchy
 *
 * @author Jason Weathersby
 * @author Josh Long
 */
abstract public class AbstractSingleFormatBirtView extends AbstractView implements InitializingBean {

    private IReportEngine birtEngine;

    private String reportNameRequestParameter = "reportName";

    private String imagesDirectory = "images";

    private String reportsDirectory = "reports";

    private String resourceDirectory = "resources";

    private String isNullParameterName = "__isnull";

    private IRenderOption renderOption;

    private String reportFormatRequestParameter = "reportFormat";

    protected BirtViewResourcePathCallback birtViewResourcePathCallback;

    protected IHTMLActionHandler actionHandler;

    private String requestEncoding = "UTF-8";

    public void setNullParameterName(String nullParameterName) {
        isNullParameterName = nullParameterName;
    }

    public void setBirtViewResourcePathCallback(BirtViewResourcePathCallback birtViewResourcePathCallback) {
        this.birtViewResourcePathCallback = birtViewResourcePathCallback;
    }

    /**
     * Perform common validation on the state of this object
     *
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(this.requestEncoding, "the 'requestEncoding' must be set");
        Assert.hasText(this.reportFormatRequestParameter, "the 'reportFormatRequestParameter' must not be null");
        Assert.hasText(this.reportNameRequestParameter, "the 'reportNameRequestParameter' must not be null");

        if (null == this.renderOption)
            this.renderOption = new RenderOption();

        if (null == this.actionHandler)
            this.actionHandler = new SpringActionHandler(this.reportNameRequestParameter, this.reportFormatRequestParameter);

        if (null == birtViewResourcePathCallback)
            this.birtViewResourcePathCallback = new SimpleBirtViewResourcePathPathCallback(this.reportsDirectory, this.imagesDirectory, this.resourceDirectory);
    }

    public void setRequestEncoding(String r) {
        this.requestEncoding = r;
    }

    public void setBirtEngine(IReportEngine birtEngine) {
        this.birtEngine = birtEngine;
    }

    public void setResourceDirectory(String resourceDirectory) {
        this.resourceDirectory = resourceDirectory;
    }

    public void setReportsDirectory(String reportsDirectory) {
        this.reportsDirectory = reportsDirectory;
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

    public static interface BirtViewResourcePathCallback {

        String baseImageUrl(ServletContext sc, HttpServletRequest r, String reportName) throws Throwable;

        String baseUrl(ServletContext sc, HttpServletRequest r, String reportName) throws Throwable;

        String pathForReport(ServletContext servletContext, HttpServletRequest r, String reportName) throws Throwable;

        String imageDirectory(ServletContext sc, HttpServletRequest request, String reportName);

        String resourceDirectory(ServletContext sc, HttpServletRequest request, String reportName);

    }

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


    public void setHtmlActionHandler(IHTMLActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }


    abstract protected RenderOption renderReport(Map map, HttpServletRequest request, HttpServletResponse response,
                                                 BirtViewResourcePathCallback resourcePathCallback, Map<String, Object> appContextValuesMap,
                                                 String reportName, String format, IRenderOption options) throws Throwable;

    public void setRenderOption(IRenderOption renderOption) {
        this.renderOption = renderOption;
    }

    @SuppressWarnings("unchecked")
    protected void renderMergedOutputModel(Map map, HttpServletRequest request, HttpServletResponse response) throws Exception {
        FileInputStream fis = null;
        try {
            String reportName = request.getParameter(this.reportNameRequestParameter);
            String format = request.getParameter(this.reportFormatRequestParameter);
            ServletContext sc = request.getServletContext(); /// avoid creating an HTTP session if possible.
            if (format == null) {
                format = "html";
            }

            Map<String, Object> mapOfOptions = new HashMap<String, Object>();
            mapOfOptions.put(IModuleOption.RESOURCE_FOLDER_KEY, birtViewResourcePathCallback.resourceDirectory(sc, request, reportName));
            mapOfOptions.put(IModuleOption.PARSER_SEMANTIC_CHECK_KEY, Boolean.FALSE);

            fis = new FileInputStream(birtViewResourcePathCallback.pathForReport(sc, request, reportName));
            IReportRunnable runnable = birtEngine.openReportDesign(reportName, fis, mapOfOptions);
            IRunAndRenderTask runAndRenderTask = birtEngine.createRunAndRenderTask(runnable);
            runAndRenderTask.setParameterValues(discoverAndSetParameters(runnable, request));

            // set content type
            String contentType = birtEngine.getMIMEType(format);
            response.setContentType(contentType);
            setContentType(contentType);

            Map<String, Object> appContextValuesToPutInApplicationContextDuringRunAndRender = new HashMap<String, Object>();
            appContextValuesToPutInApplicationContextDuringRunAndRender.put(EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST, request);

            IRenderOption options = null == this.renderOption ? new RenderOption() : this.renderOption;
            options.setActionHandler(actionHandler);

            IRenderOption returnedRenderOptions = renderReport(map, request, response, this.birtViewResourcePathCallback,
                    appContextValuesToPutInApplicationContextDuringRunAndRender, reportName, format, options);

            runAndRenderTask.setRenderOption(returnedRenderOptions);

            for (String k : appContextValuesToPutInApplicationContextDuringRunAndRender.keySet())
                runAndRenderTask.getAppContext().put(k, appContextValuesToPutInApplicationContextDuringRunAndRender.get(k));

            runAndRenderTask.run();
            runAndRenderTask.close();


            /* if (format.equalsIgnoreCase("html")) {
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
                String rptDesignSuffix = ".RPTDESIGN";
                if (uReportName.endsWith(rptDesignSuffix)) {
                    att = uReportName.replace(rptDesignSuffix, "." + format);
                }
                // Create file
                FileWriter fstream = new FileWriter("c:/test/out.txt");
                BufferedWriter out = new BufferedWriter(fstream);
                out.write("Hello Java " + format + "--" + birtEngine.getMIMEType(format));
                out.close();

                response.setHeader("Content-Disposition", "attachment; filename=\"" + att + "\"");
                options.setOutputStream(response.getOutputStream());
                options.setOutputFormat(format);
                runAndRenderTask.setRenderOption(options);
            }*/


        } catch (Throwable th) {
            throw new RuntimeException(th); // nothing useful to do here
        } finally {
            if (null != fis)
                IOUtils.closeQuietly(fis);
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
