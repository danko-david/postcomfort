package eu.javaexperience.postcomfort.web;

import java.io.Closeable;
import java.io.IOException;

import eu.javaexperience.electronic.uartbus.rpc.UartbusConnection;
import eu.javaexperience.electronic.uartbus.rpc.client.UartbusRpcClientTools;
import eu.javaexperience.io.IOTools;
import eu.javaexperience.log.JavaExperienceLoggingFacility;
import eu.javaexperience.log.LogLevel;
import eu.javaexperience.log.Loggable;
import eu.javaexperience.log.Logger;
import eu.javaexperience.log.LoggingTools;

public class UartbusConnectionDistributorUnit implements Closeable
{
	protected static final Logger LOG = JavaExperienceLoggingFacility.getLogger(new Loggable("UartbusConnectionDistributorUnit"));
	
	protected final UartbusConnectionDistributor dist;
	protected final Thread receiver;
	protected final UartbusConnection send;
	protected final UartbusConnection receive;
	
	public UartbusConnectionDistributorUnit(String ip, int port) throws IOException
	{
		send = UartbusRpcClientTools.connectTcp(ip, port);
		this.dist = new UartbusConnectionDistributor((data)->{
			try
			{
				send.sendPacket(data);
			}
			catch (IOException e)
			{
				LoggingTools.tryLogFormatException(LOG, LogLevel.ERROR, e, "Error while distributing packet");
			}
		});
		
		receive = UartbusRpcClientTools.connectTcp(ip, port);
		receiver = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					while(true)
					{
						dist.feedPacketToDistribute(receive.getNextPacket());
					}
				}
				catch(Exception e)
				{
					LoggingTools.tryLogFormatException(LOG, LogLevel.ERROR, e, "Error while polling packet");
				}
			}
		};
	}

	@Override
	public void close() throws IOException
	{
		IOTools.silentClose(receive);
		IOTools.silentClose(send);
		receiver.interrupt();
	}
}