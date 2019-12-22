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
import eu.javaexperience.exceptions.OperationSuccessfullyEnded;
import eu.javaexperience.log.JavaExperienceLoggingFacility;
import eu.javaexperience.log.LogLevel;
import eu.javaexperience.log.Loggable;
import eu.javaexperience.log.Logger;
import eu.javaexperience.log.LoggingTools;
import eu.javaexperience.reflect.Mirror;

import eu.javaexperience.web.RequestContext;
import eu.javaexperience.web.facility.SiteFacilityTools;
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

	public static Server createServer(int port) throws Exception
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
				RequestContext ctx = new RequestContext(request, response);
				try
				{
					SiteFacilityTools.setCurrentContext(ctx);
					PostComfortFacilities.DDS.getChains().dispatch(ctx);
				}
				catch(OperationSuccessfullyEnded s)
				{
					return;
				}
				catch(Throwable e)
				{
					LoggingTools.tryLogFormatException(LOG, LogLevel.WARNING, e, "Exception while handling http request ");
					Mirror.propagateAnyway(e);
				}
				finally
				{
					response.flushBuffer();
					SiteFacilityTools.setCurrentContext(null);
				}
			}
		});
		return server;
	}
	
	public static void main(String[] args) throws Throwable
	{
		JavaExperienceLoggingFacility.addStdOut();
		Map<String, List<String>> pa = CliTools.storeCliOptions(true, args);
		String un = CliTools.getFirstUnknownParam(pa, PROG_CLI_ENTRIES);
		if(null != un)
		{
			printHelpAndExit(1);
		}
		
		PostComfortFacilities.touch();
		
		Server server = createServer(WEB_PORT.tryParseOrDefault(pa, 2100));
		server.start();
	}
}
