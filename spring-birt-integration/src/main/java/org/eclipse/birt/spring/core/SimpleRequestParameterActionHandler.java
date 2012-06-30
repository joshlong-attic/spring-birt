package org.eclipse.birt.spring.core;


import com.ibm.icu.math.BigDecimal;
import org.eclipse.birt.report.engine.api.IAction;
import org.eclipse.birt.report.engine.api.IHTMLActionHandler;
import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.eclipse.birt.report.model.api.util.ParameterValidationUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Defines a default action handler for HTML output format
 *
 * @author Jason Weathersby
 * @author Josh Long
 */
public class SimpleRequestParameterActionHandler implements IHTMLActionHandler {
    private String mReportsKey = null;
    private String mFormatKey = null;
    private Map<Integer, ActionUrlBuilder> urlBuilderMap = new ConcurrentHashMap<Integer, ActionUrlBuilder>();

    public SimpleRequestParameterActionHandler(String reportNameKey, String formatKey) {
        this.mReportsKey = reportNameKey;
        this.mFormatKey = formatKey;

        // lookup table for types of actions
        urlBuilderMap.put(IAction.ACTION_BOOKMARK, new ActionUrlBuilder() {
            public String urlForAction(int actionType, IAction actionDefn, IReportContext ctx) throws Exception {
                if (actionDefn.getActionString() != null)
                    return "#" + actionDefn.getActionString();
                return null;
            }
        });
        urlBuilderMap.put(IAction.ACTION_HYPERLINK, new ActionUrlBuilder() {
            public String urlForAction(int actionType, IAction actionDefn, IReportContext ctx) throws Exception {
                return actionDefn.getActionString();
            }
        });
        urlBuilderMap.put(IAction.ACTION_DRILLTHROUGH, new ActionUrlBuilder() {
            public String urlForAction(int actionType, IAction actionDefn, IReportContext ctx) throws Exception {
                return buildDrillAction(actionDefn, ctx);
            }
        });


    }

    public String getURL(IAction actionDefn, Object context) {
        if (context instanceof IReportContext) {
            return getURL(actionDefn, (IReportContext) context);
        } else {
            return null;
        }
    }

    public String getURL(IAction actionDefn, IReportContext context) {
        try {
            return (actionDefn != null && urlBuilderMap.containsKey(actionDefn.getType())) ?
                    urlBuilderMap.get(actionDefn.getType()).urlForAction(actionDefn.getType(), actionDefn, context) :
                    null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static private interface ActionUrlBuilder {
        String urlForAction(int actionType, IAction actionDefn, IReportContext ctx) throws Exception;
    }

    /**
     * builds URL for drillthrough action
     *
     * @param action  instance of the IAction instance
     * @param context the context for building the action string
     * @return a URL
     */
    protected String buildDrillAction(IAction action, IReportContext context) {
        String baseURL = null;
        if (context != null) {
            baseURL = context.getRenderOption().getBaseURL();
        }

        if (baseURL == null) {
            baseURL = "";
        }
        StringBuilder link = new StringBuilder();
        String reportName = action.getReportName();


        if (reportName != null && !reportName.equals("")) {
            link.append(baseURL);
            String format = action.getFormat();

            String reportAction = reportName.replaceAll("(?i).rptdesign", "");

            try {
                //link.append("?" + this.mReportsKey + "=");
                link.append("/");
                link.append(URLEncoder.encode(reportAction, "UTF-8"));
            } catch (UnsupportedEncodingException e1) {
                // It should not happen. Does nothing
            }

            // add format support
            boolean quesChar = false;
            if (format != null && format.length() > 0) {
                link.append("?").append(this.mFormatKey).append("=").append(format);
                quesChar = true;
            }

            // Adds the parameters
            if (action.getParameterBindings() != null) {
                for (Object o : action.getParameterBindings().entrySet()) {
                    Map.Entry entry = (Map.Entry) o;
                    try {
                        String key = (String) entry.getKey();
                        Object valueObj = entry.getValue();
                        if (valueObj != null) {
                            Object[] values;
                            if (valueObj instanceof List) {
                                valueObj = ((List) valueObj).toArray();
                                values = (Object[]) valueObj;
                            } else {
                                values = new Object[1];
                                values[0] = valueObj;
                            }
                            for (Object objectValue : values) {
                                String value = getDisplayValue(objectValue);
                                if (value != null) {
                                    if (quesChar) {
                                        link.append("&").append(URLEncoder.encode(key, "UTF-8")).append("=").append(URLEncoder.encode(value, "UTF-8"));
                                    } else {
                                        link.append("?").append(URLEncoder.encode(key, "UTF-8")).append("=").append(URLEncoder.encode(value, "UTF-8"));
                                        quesChar = true;
                                    }
                                }
                            }
                        }
                    } catch (UnsupportedEncodingException e) {
                        // Does nothing
                    }
                }
            }

            if (action.getBookmark() != null) {
                try {
                    link.append("#");
                    link.append(URLEncoder.encode(action.getBookmark(), "UTF-8"));

                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return link.toString();
    }

    private String getDisplayValue(Object value) {
        if (value == null)
            return null;

        if (value instanceof Float || value instanceof Double
                || value instanceof BigDecimal) {
            return value.toString();
        }
        return ParameterValidationUtil.getDisplayValue(value);
    }


}