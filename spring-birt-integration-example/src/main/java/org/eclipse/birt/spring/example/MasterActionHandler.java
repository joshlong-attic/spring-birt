package org.eclipse.birt.spring.example;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.eclipse.birt.report.engine.api.IAction;
import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.eclipse.birt.spring.core.SimpleRequestParameterActionHandler;

public class MasterActionHandler extends SimpleRequestParameterActionHandler {

	public MasterActionHandler(String reportNameKey, String formatKey) {
		super(reportNameKey, formatKey);

	}
	public MasterActionHandler() {
		super(null, null);

	}

	@Override
	public String getURL(IAction actionDefn, IReportContext context) {

		if( actionDefn.getType() == IAction.ACTION_DRILLTHROUGH ){

			//"/orders/{orderId}.html"
			StringBuilder link = new StringBuilder();

			String baseURL = null;
			if (context != null) {
				baseURL = context.getRenderOption().getBaseURL();
			}
			if (baseURL == null) {
				baseURL = "";
			}
			link.append( baseURL );
			link.append("/orders/");
			Object ordern = actionDefn.getParameterBindings().get("order");


			if (ordern != null) {
				Object[] values;
				if (ordern instanceof List) {
					ordern = ((List) ordern).toArray();
					values = (Object[]) ordern;
				} else {
					values = new Object[1];
					values[0] = ordern;
				}

				if( values[0] != null )link.append( values[0]);
				link.append(".html");
			}

			return link.toString();			
		}else{
			return super.getURL(actionDefn, context);
		}
	}
}
