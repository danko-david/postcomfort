package eu.javaexperience.postcomfort.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import eu.javaexperience.cli.CliEntry;
import eu.javaexperience.cli.CliTools;
import eu.javaexperience.file.AbstractFile;
import eu.javaexperience.file.FileSystemTools;
import eu.javaexperience.io.IOTools;
import eu.javaexperience.postcomfort.webapp.PostComfortWebapp;
import eu.javaexperience.reflect.Mirror;
import eu.javaexperience.url.UrlTools;
import eu.javaexperience.web.Context;
import eu.javaexperience.web.facility.SiteFacilityTools;

public class PostComfortBackendTools
{
	public static final CliEntry<Integer> WEB_PORT = CliEntry.createFirstArgParserEntry
	(
		(e)->Integer.parseInt(e),
		"Web port. Default: 2100",
		"w", "-web-port"
	);

	public static final CliEntry<String> WEB_APP = CliEntry.createFirstArgParserEntry
	(
		e->e,
		"Web application class. (Default: eu.javaexperience.postcomfort.webapp.PostComfortWebapp)",
		"a", "-web-app-class"
	);
	
	
	public static Class<?> getWebappClass()
	{
		try
		{
			return Class.forName
			(
				WEB_APP.tryParseOrDefault(CliTools.getStoredCliOptions(), PostComfortWebapp.class.getCanonicalName()),
				false,
				Thread.currentThread().getContextClassLoader());
		}
		catch (ClassNotFoundException e)
		{
			Mirror.propagateAnyway(e);
			return null;
		}
	}

	public static boolean isSystemInDevelMode()
	{
		return true;
	}

	//TODO or use zip filesystem
	public static AbstractFile resourceFile(String file)
	{
		return FileSystemTools.DEFAULT_FILESYSTEM.fromUri(file);
	}
	
	public static void renderBootPage(Context ctx, List<String> css, List<String> preJs, List<String> postJs, String page)
	{
		StringBuilder sb = new StringBuilder();
		renderBootPage(sb, css, preJs, postJs, page);
		SiteFacilityTools.finishWithElementSend(ctx, sb.toString());
	}
	
	public static void renderBootPage(StringBuilder sb, List<String> css, List<String> preJs, List<String> postJs, String page)
	{
		sb.append("<html>\n");
		sb.append("\t<head>\n");
		sb.append("\t\t<meta charset=\"utf-8\">\n");
		
		if(null != css)
		{
			for(String c:css)
			{
				sb.append("\t\t<link rel=\"stylesheet\" type=\"text/css\" href=\"");
				sb.append(c);
				sb.append("\" />\n");
			}
		}
		
		if(null != preJs)
		{
			for(String c:preJs)
			{
				sb.append("\t\t<script type=\"text/javascript\" src=\"");
				sb.append(c);
				sb.append("\"></script>\n");
			}
		}
		sb.append("\t</head>\n");
		
		sb.append("\t<body>\n");
		
		if(null != page)
		{
			sb.append("\t\t<div class=\"page_identifier_data\" data-page_id=\"");
			sb.append(page);
			sb.append("\"></div>\n");
		}
		
		sb.append("");
		
		if(null != postJs)
		{
			for(String c:postJs)
			{
				sb.append("\t\t<script type=\"text/javascript\" src=\"");
				sb.append(c);
				sb.append("\"></script>\n");
			}
		}
		
		sb.append("\t\t<script type=\"text/javascript\">main();</script>\n");
		sb.append("\t</body>\n");
		sb.append("</html>");
	}

	public static void acceptPostRequests(Context ctx)
	{
		try
		{
			HttpServletRequest request = ctx.getRequest();
			InputStream is = request.getInputStream();
			
			int n = request.getContentLength();
			int ava = is.available();
			if(n <= 0 && ava > 0)
				n = is.available();
			
			byte[] data = null;
			if(n > 0)
			{
				data = new byte[n];
				IOTools.readFull(is, data);
			}
			else
			{
				data = IOTools.loadAllAvailableFromInputStream(is);
			}
			try
			{
				//UrlTools.processArgsRequest(new String(data), reqParams);
			}
			catch(Exception e){}
			request.setAttribute("data", data);
		}
		catch(Exception e)
		{
			Mirror.propagateAnyway(e);
		}
	}
}
