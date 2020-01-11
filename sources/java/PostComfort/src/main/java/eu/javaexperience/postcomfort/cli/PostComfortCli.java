package eu.javaexperience.postcomfort.cli;

import eu.javaexperience.electronic.uartbus.UartBusCli;
import eu.javaexperience.generic.annotations.Ignore;
import eu.javaexperience.postcomfort.web.PostComfortBackendTools;
import eu.javaexperience.postcomfort.web.PostComfortStarter;
import eu.javaexperience.rpc.cli.RpcCliTools;
import eu.javaexperience.teasite.TeasiteBundle;

public class PostComfortCli
{
	@Ignore
	public static void main(String[] args)
	{
		RpcCliTools.tryExecuteCommandCollectorClassOrExit(new PostComfortCli(), 1, args);
	}
	
	public static void ub(String... params) throws Throwable
	{
		UartBusCli.main(params);
	}
	
	public static void compileScript(String... params)
	{
		PostComfortBackendTools.forceSetDevelopementMode(true);
		TeasiteBundle bundle = PostComfortStarter.createBundle();
		bundle.script.getScriptNode().refresh();
	}
	
	public static void webStart(String... params) throws Throwable
	{
		PostComfortStarter.main(params);
	}
}
