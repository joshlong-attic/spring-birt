package org.eclipse.birt.spring.core;


import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.IAction;
import org.eclipse.birt.report.engine.api.IHTMLActionHandler;
import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.eclipse.birt.report.model.api.util.ParameterValidationUtil;

import com.ibm.icu.math.BigDecimal;

/**
 * Defines a default action handler for HTML output format
 */
public class SpringActionHandler implements IHTMLActionHandler
{
	private String mReportsKey = null;
	private String mFormatKey = null;
	
	
    public SpringActionHandler(String reportNameKey, String formatKey) {
    	

    	this.mReportsKey = reportNameKey;
    	this.mFormatKey = formatKey;
    	
    }

	/**
	 * Get URL of the action.
	 * 
	 * @param actionDefn
	 * @param context
	 * @return URL
	 */

	public String getURL(IAction actionDefn, Object context) {
		if( context instanceof IReportContext){
			return getURL( actionDefn, (IReportContext)context);
		}else{
			return null;
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.engine.api2.IHTMLActionHandler#getURL(org.eclipse.birt.report.engine.api2.IAction,
	 *      java.lang.Object)
	 */
	public String getURL( IAction actionDefn, IReportContext context )
	{
		if ( actionDefn == null )
		{
			return null;
		}
		String url = null;
		switch ( actionDefn.getType( ) )
		{
			case IAction.ACTION_BOOKMARK :
				if ( actionDefn.getActionString( ) != null )
				{
					url = "#" + actionDefn.getActionString( );
				}
				break;
			case IAction.ACTION_HYPERLINK :
				url = actionDefn.getActionString( );
				break;
			case IAction.ACTION_DRILLTHROUGH :
				url = buildDrillAction( actionDefn, context );
				break;
			default :
				return null;
		}
		return url;
	}

	/**
	 * builds URL for drillthrough action
	 * 
	 * @param action
	 *            instance of the IAction instance
	 * @param context
	 *            the context for building the action string
	 * @return a URL
	 */
	protected String buildDrillAction( IAction action, IReportContext context )
	{
		String baseURL = null;
		if ( context != null )
		{
			if ( context instanceof IReportContext )
			{
				baseURL = ( (IReportContext) context ).getRenderOption().getBaseURL( );
			}else{
				baseURL = "";
			}
		}

		if ( baseURL == null )
		{
			baseURL = "";
		}
		StringBuffer link = new StringBuffer( );
		String reportName = action.getReportName( );
	

		if ( reportName != null && !reportName.equals( "" ) ) //$NON-NLS-1$
		{
			link.append( baseURL );
			String format = action.getFormat( );


			try
			{
				link.append("?"+this.mReportsKey+"=" );
				link.append( URLEncoder.encode( reportName, "UTF-8" ) ); //$NON-NLS-1$
			}
			catch ( UnsupportedEncodingException e1 )
			{
				// It should not happen. Does nothing
			}

			// add format support
			if ( format != null && format.length( ) > 0 )
			{
				link.append( "&"+this.mFormatKey+"=" + format ); //$NON-NLS-1$
			}

			// Adds the parameters
			if ( action.getParameterBindings( ) != null )
			{
				Iterator paramsIte = action.getParameterBindings( ).entrySet( )
						.iterator( );
				while ( paramsIte.hasNext( ) )
				{
					Map.Entry entry = (Map.Entry) paramsIte.next( );
					try
					{
						String key = (String) entry.getKey( );
						Object valueObj = entry.getValue( );
						if ( valueObj != null )
						{
							Object[] values;
							if ( valueObj instanceof List )
							{
								valueObj = ( (List) valueObj ).toArray( );
								values = (Object[]) valueObj;
							}
							else
							{
								values = new Object[1];
								values[0] = valueObj;
							}

							for ( int i = 0; i < values.length; i++ )
							{
								String value = getDisplayValue( values[i] );

								if ( value != null )
								{
									link.append( "&"
											+ URLEncoder.encode( key, "UTF-8" )
											+ "="
											+ URLEncoder.encode( value, "UTF-8" ) );
								}
							}
						}
					}
					catch ( UnsupportedEncodingException e )
					{
						// Does nothing
					}
				}
			}

			if (action.getBookmark( ) != null )
			{
				try
				{
					link.append( "#" ); //$NON-NLS-1$
					link.append( URLEncoder.encode( action.getBookmark( ),
							"UTF-8" ) ); //$NON-NLS-1$
				}
				catch ( UnsupportedEncodingException e )
				{
					// Does nothing
				}
			}
		}

		return link.toString( );
	}


	/**
	 * Get display value.
	 * 
	 * @param value
	 * @return
	 */
	String getDisplayValue( Object value )
	{
		if ( value == null )
			return null;

		if ( value instanceof Float || value instanceof Double
				|| value instanceof BigDecimal )
		{
			return value.toString( );
		}
		return ParameterValidationUtil.getDisplayValue( value );
	}




}