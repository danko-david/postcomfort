package eu.javaexperience.postcomfort.web;

import static eu.javaexperience.electronic.uartbus.rpc.UartbusCliTools.RPC_HOST;
import static eu.javaexperience.electronic.uartbus.rpc.UartbusCliTools.RPC_PORT;

import java.util.List;
import java.util.Map;

import eu.javaexperience.cli.CliTools;
import eu.javaexperience.electronic.uartbus.rpc.UartbusConnection;
import eu.javaexperience.reflect.Mirror;
import eu.javaexperience.rpc.JavaClassRpcUnboundFunctionsInstance;
import eu.javaexperience.rpc.RpcFacility;

/**
 * There will be 3 channel of UARTBus in a vehicle:
 * 	- engine (receive fuel consumption, fuel level, pedal state)
 * 	- chassis (door locks, interior lighting )
 * 	- user (buttons, knobs [potentiometers], displays)
 * 
 * This can be done with an atmega2650 (it has 4 UARTs) and UARTBus gateway
 * multichannel support (not yet implemented)
 * 
 * */
public class PostComfortFacilities
{
/********************* UARTBus related initialization *************************/
	
	protected static final UartbusConnectionDistributorUnit ENGINE;
	
	public static void touch(){}
	
	
	//actually this is a bad practice to initialize stuffs but
	//should getting started somewhere.
	static
	{
		UartbusConnectionDistributorUnit engine; 
		try
		{
			Map<String, List<String>> pa = CliTools.getStoredCliOptions();
			engine = new UartbusConnectionDistributorUnit
			(
				RPC_HOST.tryParseOrDefault(pa, "127.0.0.1"),
				RPC_PORT.tryParseOrDefault(pa, 2112)
			);
			
			engine.receive.setAttribute("loopback_send_packets", "true");
			engine.receiver.start();
		}
		catch(Exception e)
		{
			engine = null;
			Mirror.propagateAnyway(e);
			
		}
		ENGINE = engine;
	}
	
/************************** Web RPC initailization ****************************/
	
	protected static final RpcFacility[] RPC_NS = new RpcFacility[]
	{
		new JavaClassRpcUnboundFunctionsInstance<>("uartbus_engine", ENGINE.dist, UartbusConnection.class)
	};
	
	public static RpcFacility[] getRpcNodes()
	{
		return RPC_NS;
	}
}
