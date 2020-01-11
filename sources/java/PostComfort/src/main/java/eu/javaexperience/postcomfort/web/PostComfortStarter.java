package eu.javaexperience.postcomfort.web;

import static eu.javaexperience.electronic.uartbus.rpc.UartbusCliTools.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;

import eu.javaexperience.cli.CliEntry;
import eu.javaexperience.cli.CliTools;
import eu.javaexperience.log.JavaExperienceLoggingFacility;
import eu.javaexperience.log.Loggable;
import eu.javaexperience.log.Logger;
import eu.javaexperience.teasite.TeasiteBundle;
import eu.javaexperience.web.template.WebsiteTemplate;
import eu.javaexperience.web.template.WebsiteTemplate.WebsiteTemplateTemplateBuilder;

import static eu.javaexperience.postcomfort.web.PostComfortBackendTools.*;

public class PostComfortStarter
{
	protected static final Logger LOG = JavaExperienceLoggingFacility.getLogger(new Loggable("PostComfortStarter"));
	
	protected static final CliEntry[] PROG_CLI_ENTRIES =
	{
		RPC_HOST,
		RPC_PORT,
		WEB_PORT,
		WEB_APP
	};
	
	public static void printHelpAndExit(int exit)
	{
		System.err.println("Usage of PostComfortStarter:\n");
		System.err.println(CliTools.renderListAllOption(PROG_CLI_ENTRIES));
		System.exit(1);
	}

	public static Server createServer(WebsiteTemplate wt, int port) throws Exception
	{
		Server server = new Server();
		ServerConnector connector = new ServerConnector(server, 1, 1);
		connector.setPort(port);
		server.addConnector(connector);

		server.setHandler(new AbstractHandler()
		{
			@Override
			public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
			{
				wt.service(request, response);
			}
		});
		return server;
	}
	
	public static TeasiteBundle createBundle()
	{
		return new TeasiteBundle
		(
			"",
			PostComfortBackendTools.isSystemInDevelMode(),
			PostComfortBackendTools.resourceFile
			(
				PostComfortBackendTools.isSystemInDevelMode()?
					"src/main/resources/web_resources"
				:
					"web_resources"
			),
			"/tmp/postcomfort",
			PostComfortBackendTools.resourceFile((PostComfortBackendTools.isSystemInDevelMode()?"src/main/resources/":"")+"web_resources/site/postcomfort_webapp.js"),
			PostComfortBackendTools.getWebappClass(),
			null,
			PostComfortFacilities.getRpcNodes()
		);
	}
	
	public static void main(String[] args) throws Throwable
	{
		System.out.println("isDevel: "+PostComfortBackendTools.isSystemInDevelMode());
		JavaExperienceLoggingFacility.addStdOut();
		Map<String, List<String>> pa = CliTools.storeCliOptions(true, args);
		String un = CliTools.getFirstUnknownParam(pa, PROG_CLI_ENTRIES);
		if(null != un)
		{
			printHelpAndExit(1);
		}
		
		PostComfortFacilities.touch();
		
		TeasiteBundle bundle = createBundle();
		
		WebsiteTemplateTemplateBuilder b = WebsiteTemplateTemplateBuilder.createDefaults();
		b.handleApp = bundle.asPathDispatch();
		b.sessionCookieName = "jvx-postcomfort-session";
		WebsiteTemplate wt = new WebsiteTemplate(b);
		
		Server server = createServer(wt, WEB_PORT.tryParseOrDefault(pa, 2100));
		server.start();
	}
}
