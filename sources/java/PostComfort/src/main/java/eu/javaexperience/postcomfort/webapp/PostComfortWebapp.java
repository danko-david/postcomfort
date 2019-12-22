package eu.javaexperience.postcomfort.webapp;

import java.io.IOException;

import org.teavm.jso.dom.html.HTMLDocument;

import eu.javaexperience.electronic.uartbus.PacketAssembler;
import eu.javaexperience.electronic.uartbus.UartbusTools;
import eu.javaexperience.postcomfort.web.api.UartbusApi;
import eu.jvx.js.lib.bindings.H;
import eu.jvx.js.lib.bindings.VanillaTools;
import eu.jvx.js.lib.teavm.NativeJsSupportTeaVM;
import eu.teasite.frontend.api.ApiClient;

public class PostComfortWebapp
{
	static
	{
		NativeJsSupportTeaVM.init();
	}
	
	public static ApiClient API_CLIENT = new ApiClient("/api", false);
	
	public static UartbusApi ENGINE = API_CLIENT.getApiClass(UartbusApi.class, "uartbus_engine"); 
	
	static
	{
		try
		{
			ENGINE.setAttribute("loopback_send_packets", "true");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Throwable
	{
		HTMLDocument dom = VanillaTools.getDom();
		H body = new H(dom.querySelector("body"));
		
		
		PacketAssembler pa = new PacketAssembler();
		pa.writeAddress(30);
		pa.writeAddress(60);
		pa.write(new byte[] {1,2,2});
		pa.appendCrc8();
		byte[] cmd = pa.done();
		
		body.addChilds(new H("button").attrs("#text", "Toggle").on("click", e->ENGINE.sendPacket(cmd)));
		
		new Thread()
		{
			public void run()
			{
				while(true)
				{
					byte[] e = ENGINE.getNextPacket();
					boolean valid = UartbusTools.crc8(e, e.length-1) == e[e.length-1];
					StringBuilder sb = new StringBuilder();
					if(!valid)
					{
						sb.append("!");
					}
					sb.append(UartbusTools.formatColonData(e));
					System.out.println(sb.toString());
				}
			};
		}.start();
	}
}
