package org.eclipse.birt.spring.core;

import org.apache.commons.io.IOUtils;
import org.eclipse.birt.report.data.oda.jdbc.IConnectionFactory;
import org.eclipse.birt.report.engine.api.*;
import org.eclipse.birt.report.model.api.IModuleOption;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.FileInputStream;
import java.util.*;

/**
 * Base class for BIRT-based {@link org.springframework.web.servlet.View views}.
 */
abstract public class AbstractSingleFormatBirtView extends AbstractUrlBasedView implements InitializingBean {

    public static interface BirtViewResourcePathCallback {

        String baseImageUrl(ServletContext sc, HttpServletRequest r, String reportName) throws Throwable;

        String baseUrl(ServletContext sc, HttpServletRequest r, String reportName) throws Throwable;

        String pathForReport(ServletContext servletContext, HttpServletRequest r, String reportName) throws Throwable;

        String imageDirectory(ServletContext sc, HttpServletRequest request, String reportName);

        String resourceDirectory(ServletContext sc, HttpServletRequest request, String reportName);

        String pathForDocument(ServletContext servletContext, HttpServletRequest r, String documentName) throws Throwable;


    }

    public static final int RUNRENDERTASK = 0;
    public static final int RUNTHENRENDERTASK = 1;


    private DataSource dataSource;
    private String reportName;
    private IReportEngine birtEngine;

    private int taskType = this.RUNRENDERTASK;

    private String reportNameRequestParameter = "reportName";

    private String documentNameRequestParameter = "documentName";

    private String imagesDirectory = "images";

    private String documentsDirectory = "";

    private String documentName = null;

    private String reportsDirectory = "";

    private String resourceDirectory = "resources";

    private String isNullParameterName = "__isnull";

    private IRenderOption renderOption;

    private String reportFormatRequestParameter = "reportFormat";

    protected BirtViewResourcePathCallback birtViewResourcePathCallback;

    protected IHTMLActionHandler actionHandler;

    private String requestEncoding = "UTF-8";

    private String renderRange = null;
    
    private String reportOutputFormat = "html";

    private Map<String, Object> reportParameters = new HashMap<String, Object>();

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public void setReportOutputFormat(String format){
    	this.reportOutputFormat = format;
    }
    /**
     * This method allows you to set the implementation of the Resource callback class for implementing
     * location logic for resource folder, image folder, reports folder, documents folder, baseURL and baseImageURL
     */
    public void setBirtViewResourcePathCallback(BirtViewResourcePathCallback birtViewResourcePathCallback) {
        this.birtViewResourcePathCallback = birtViewResourcePathCallback;
    }

    /**
     * Data source to put in the report's app context.
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * This method allows you to set a report parameter to a null value
     */
    public void setNullParameterName(String nullParameterName) {
        isNullParameterName = nullParameterName;
    }

    /**
     * Method to set report Parameters, defaults form the URL
     */
    public void setReportParameters(Map<String, Object> reportParameters) {
        this.reportParameters = reportParameters;
    }


    /**
     * Method to set encoding for the request
     */
    public void setRequestEncoding(String r) {
        this.requestEncoding = r;
    }

    /**
     * Set the instance of the BIRT Engine
     */
    public void setBirtEngine(IReportEngine birtEngine) {
        this.birtEngine = birtEngine;
    }

    /**
     * Set the resource directory that contains birt libraries, images, style sheets for the reports
     * by default the Resources folder will be checked
     */
    public void setResourceDirectory(String resourceDirectory) {
        this.resourceDirectory = resourceDirectory;
    }

    /**
     * Set the folder within the web app that will contain reports
     */
    public void setReportsDirectory(String reportsDirectory) {
        this.reportsDirectory = reportsDirectory;
    }

    /**
     * by default this parameter is set to reportFomat (eg reportFormat=html), but using this method
     * the request parameter can be changed.
     */
    public void setReportFormatRequestParameter(String rf) {
        this.reportFormatRequestParameter = rf;
    }

    /**
     * by default this parameter is set to reportName (eg reportName=TopNPercent.rptdesign), but using this method
     * the request parameter can be changed.
     */
    public void setReportNameRequestParameter(String rn) {
        this.reportNameRequestParameter = rn;
    }

    /**
     * by default this parameter is set to nameofreport.rptdocument but using this method
     * the name of the rptdocument can be set
     */
    public void setDocumentName(String dn) {
        this.documentName = dn;
    }

    /**
     * by default this parameter is set to documentName (eg documentName=TopNPercent.rptdocument but using this method
     * the name of the requestor parameter can be change
     */
    public void setDocumentNameRequestParameter(String dn) {
        this.documentNameRequestParameter = dn;
    }

    /**
     * Set the images directory that engine will use to generate temporary images for the reports
     * by default the images directory will be used
     */
    public void setImagesDirectory(String imagesDirectory) {
        this.imagesDirectory = imagesDirectory;
    }

    /**
     * Set the documents directory that engine will use to generate temporary rptdocuments for the reports
     * by default the documents directory will be used
     */
    public void setDocumentsDirectory(String documentDirectory) {
        this.documentsDirectory = documentDirectory;

    }

    /**
     * Sets the engine to either runandrender a report using one task or
     * to generate a rptdocument first with a runtask and then followed by a render task
     * default operation is to use a runandrender task
     */
    public void setTaskType(int taskType) {
        this.taskType = taskType;

    }

    /**
     * Set the page range string for reports that use a run then render task.
     * eg 1-3, 1,3,4
     */
    public void setRenderRange(String renderRange) {
        this.renderRange = renderRange;

    }

    /**
     * sets the Action Handler instance to be used when generating html reports.
     * SimpleRequestParameterActionHandler is used by default.
     */
    public void setHtmlActionHandler(IHTMLActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }

    /**
     * This method allows setting the render options for rendering reports
     *
     * @param renderOption
     */
    public void setRenderOption(IRenderOption renderOption) {
        this.renderOption = renderOption;
    }

    /**
     * Perform common validation on the state of this object
     *
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(this.requestEncoding, "the 'requestEncoding' must be set");
        Assert.hasText(this.reportFormatRequestParameter, "the 'reportFormatRequestParameter' must not be null");
        Assert.isTrue(StringUtils.hasText(this.reportName) || StringUtils.hasText(this.reportNameRequestParameter), "the 'reportName' or the 'reportNameRequestParameter' must not be null");
        Assert.hasText(this.documentNameRequestParameter, "the 'documentNameRequestParameter' must not be null");

        if (null == this.renderOption)
            this.renderOption = new RenderOption();

        if (null == this.actionHandler)
            this.actionHandler = new SimpleRequestParameterActionHandler(this.reportNameRequestParameter, this.reportFormatRequestParameter);

        if (null == birtViewResourcePathCallback)
            this.birtViewResourcePathCallback = new SimpleBirtViewResourcePathPathCallback(this.reportsDirectory, this.imagesDirectory, this.resourceDirectory, this.documentsDirectory);
    }

    abstract protected RenderOption renderReport(Map<String, Object> map, HttpServletRequest request, HttpServletResponse response,
                                                 BirtViewResourcePathCallback resourcePathCallback, Map<String, Object> appContextValuesMap,
                                                 String reportName, String format, IRenderOption options) throws Throwable;

    private String canonicalizeName(String reportName) {
        ///Assert.hasText(reportName);
        if (!StringUtils.hasText(reportName))
            return null;

        return !reportName.toLowerCase().endsWith(".rptdesign") ? reportName + ".rptdesign" : reportName;
    }
    private String canonicalizeDocName(String docName) {
        ///Assert.hasText(reportName);
        if (!StringUtils.hasText(docName))
            return null;

        return !docName.toLowerCase().endsWith(".rptdocument") ? docName + ".rptdocument" : docName;
    }
    @SuppressWarnings("unchecked")
    protected void renderMergedOutputModel(Map<String, Object> modelData, HttpServletRequest request, HttpServletResponse response) throws Exception {
        FileInputStream fis = null;
        IReportRunnable runnable = null;
        IReportDocument document = null;
        try {

            if (this.reportParameters == null)
                this.reportParameters = new HashMap<String, Object>();

            for (String k : modelData.keySet())
                this.reportParameters.put(k, modelData.get(k));

            // 2) reportName property
            // 1) report name parameter is available, use that

            String reportName;
            reportName = StringUtils.hasText(this.reportName) ? this.reportName : request.getParameter(this.reportNameRequestParameter);    // 'cat'
            String fullReportName = canonicalizeName(reportName);

            String documentName;
            documentName = StringUtils.hasText(this.documentName) ? this.documentName : request.getParameter(this.documentNameRequestParameter);    // 'cat'
            String fullDocumentName = canonicalizeDocName(documentName);
            if( documentName == null){
            	fullDocumentName = reportName.replaceAll(".rptdesign", ".rptdocument");
            }
            
            
            String format;
            if( this.reportOutputFormat != null){
            	format = this.reportOutputFormat;
            }else{
            	format= request.getParameter(this.reportFormatRequestParameter);
            }
            ServletContext sc = request.getServletContext(); /// avoid creating an HTTP session if possible.

            if (format == null) {
                format = "html";
            }

            Map<String, Object> mapOfOptions = new HashMap<String, Object>();
            mapOfOptions.put(IModuleOption.RESOURCE_FOLDER_KEY, birtViewResourcePathCallback.resourceDirectory(sc, request, reportName));
            mapOfOptions.put(IModuleOption.PARSER_SEMANTIC_CHECK_KEY, Boolean.FALSE);

            // set content type
            String contentType = birtEngine.getMIMEType(format);
            response.setContentType(contentType);
            setContentType(contentType);

            Map<String, Object> appContextMap = new HashMap<String, Object>();
            appContextMap.put(EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST, request);

            if (this.dataSource != null) {
                appContextMap.put(IConnectionFactory.PASS_IN_CONNECTION, this.dataSource.getConnection());

                if (this.closeDataSourceConnection)
                    appContextMap.put(IConnectionFactory.CLOSE_PASS_IN_CONNECTION, Boolean.TRUE);
            }


            IEngineTask task = null;
            String pathForReport = birtViewResourcePathCallback.pathForReport(sc, request, fullReportName);
            fis = new FileInputStream(pathForReport);
            runnable = birtEngine.openReportDesign(fullReportName, fis, mapOfOptions);

            if (runnable != null && this.taskType == AbstractSingleFormatBirtView.RUNRENDERTASK) {
                task = birtEngine.createRunAndRenderTask(runnable);
                task.setParameterValues(discoverAndSetParameters(runnable, request));
                IRunAndRenderTask runAndRenderTask = (IRunAndRenderTask) task;
                IRenderOption options = null == this.renderOption ? new RenderOption() : this.renderOption;
                options.setActionHandler(actionHandler);
                IRenderOption returnedRenderOptions = renderReport(modelData, request, response, this.birtViewResourcePathCallback,
                        appContextMap, reportName, format, options);
                for (String k : appContextMap.keySet())
                    runAndRenderTask.getAppContext().put(k, appContextMap.get(k));
                runAndRenderTask.setRenderOption(returnedRenderOptions);
                runAndRenderTask.run();
                runAndRenderTask.close();
            } else {

                //Run then Render
                if (runnable != null) {
                    task = birtEngine.createRunTask(runnable);
                    task.setParameterValues(discoverAndSetParameters(runnable, request));
                    IRunTask runTask = (IRunTask) task;
                    for (String k : appContextMap.keySet())
                        runTask.getAppContext().put(k, appContextMap.get(k));
                    String pathForDocument = birtViewResourcePathCallback.pathForDocument(sc, request, fullDocumentName);
                    runTask.run(pathForDocument);
                    runTask.close();
                    document = birtEngine.openReportDocument(fullDocumentName, pathForDocument, mapOfOptions);
                    task = birtEngine.createRenderTask(document);
                    IRenderTask renderTask = (IRenderTask) task;
                    IRenderOption options = null == this.renderOption ? new RenderOption() : this.renderOption;
                    options.setActionHandler(actionHandler);
                    IRenderOption returnedRenderOptions = renderReport(modelData, request, response, this.birtViewResourcePathCallback,
                            appContextMap, reportName, format, options);
                    for (String k : appContextMap.keySet())
                        renderTask.getAppContext().put(k, appContextMap.get(k));
                    if (renderRange != null) {
                    	
                        renderTask.setPageRange(renderRange);
                    }
                    renderTask.setRenderOption(returnedRenderOptions);
                    renderTask.render();
                    renderTask.close();
                    document.close();
                }
            }


        } catch (Throwable th) {
            throw new RuntimeException(th); // nothing useful to do here
        } finally {
            if (null != fis)
                IOUtils.closeQuietly(fis);
            if (null != document)
                document.close();

        }
    }


    private boolean closeDataSourceConnection = true;// IConnectionFactory.CLOSE_PASS_IN_CONNECTION

    public void setCloseDataSourceConnection(boolean b) {
        this.closeDataSourceConnection = b;
    }

    private Map<String, Object> discoverAndSetParameters(IReportRunnable report, HttpServletRequest request) throws Throwable {

        Map<String, Object> parms = new HashMap<String, Object>();
        IGetParameterDefinitionTask task = birtEngine.createGetParameterDefinitionTask(report);
        @SuppressWarnings("unchecked")
        Collection<IParameterDefnBase> params = task.getParameterDefns(true);
        for (IParameterDefnBase param : params) {
            Assert.isInstanceOf(IScalarParameterDefn.class, param, "the parameter must be assignable to " + IScalarParameterDefn.class.getName());
            IScalarParameterDefn scalar = (IScalarParameterDefn) param;
            if (this.reportParameters != null && this.reportParameters.get(param.getName()) != null) {
                String format = scalar.getDisplayFormat();
                // todo will this step on the Spring MVC converters?
                ReportParameterConverter converter = new ReportParameterConverter(format, request.getLocale());


                Object value = this.reportParameters.get(param.getName());
                parms.put(param.getName(), value);

//                       /* converter.parse(*/  this.reportParameters.get(param.getName()), scalar.getDataType()/*)*/);
            } else if (StringUtils.hasText(getParameter(request, param.getName()))) {
                parms.put(param.getName(), getParamValueObject(request, scalar));
            }
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

class SimpleBirtViewResourcePathPathCallback implements AbstractSingleFormatBirtView.BirtViewResourcePathCallback {

    private String reportFolder, imagesFolder, resourceFolder, documentsFolder;

    public String baseImageUrl(ServletContext sc, HttpServletRequest request, String reportName) throws Throwable {
        return request.getContextPath() + "/" + imagesFolder;
    }

    public String baseUrl(ServletContext sc, HttpServletRequest request, String reportName) throws Throwable {
        String baseUrl = request.getRequestURI();
        int trimloc = baseUrl.lastIndexOf("/", baseUrl.length() - 2);
        return baseUrl.substring(0, trimloc);
    }

    public String pathForReport(ServletContext sc, HttpServletRequest request, String reportName) throws Throwable {
        String folder = sc.getRealPath(reportFolder);

        if (!folder.endsWith("/"))
            folder = folder + "/";

        return folder + reportName;
    }

    public String pathForDocument(ServletContext sc, HttpServletRequest request, String documentName) throws Throwable {
        
        String folder = sc.getRealPath(documentsFolder);

        if (!folder.endsWith("/"))
            folder = folder + "/";

        return folder + documentName;
    	
    }


    public String imageDirectory(ServletContext sc, HttpServletRequest request, String reportName) {
        return sc.getRealPath(imagesFolder);
    }

    public String resourceDirectory(ServletContext sc, HttpServletRequest request, String reportName) {
        return sc.getRealPath(resourceFolder);
    }

    public SimpleBirtViewResourcePathPathCallback(String f, String i, String k, String m) {
        this.reportFolder = StringUtils.hasText(f) ? f : "";
        this.imagesFolder = StringUtils.hasText(i) ? i : "";
        this.resourceFolder = StringUtils.hasText(k) ? k : "";
        this.documentsFolder = StringUtils.hasText(m) ? m : "";

    }

}