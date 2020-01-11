package eu.javaexperience.postcomfort.webapp;

import java.io.IOException;

import eu.javaexperience.electronic.uartbus.PacketAssembler;
import eu.javaexperience.electronic.uartbus.PacketReader;
import eu.javaexperience.electronic.uartbus.UartbusTools;
import eu.javaexperience.postcomfort.web.api.UartbusApi;
import eu.jvx.js.lib.bindings.H;
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
	
	protected static H cmdButton() throws IOException
	{
		PacketAssembler pa = new PacketAssembler();
		pa.writeAddress(30);
		pa.writeAddress(60);
		pa.write(new byte[] {1,2,2});
		pa.appendCrc8();
		byte[] cmd = pa.done();
		
		return new H("button").attrs("#text", "Toggle").on("click", e->ENGINE.sendPacket(cmd));
	}
	
	//body.addChilds(cmdButton());
	
	public static void preventSleep()
	{
		//TODO: https://stackoverflow.com/questions/11529247/in-html5-how-can-i-keep-an-android-device-s-screen-on
	}
	
	public static class CarMetrics
	{
		public H lambda = new H("span");
		public H throttle = new H("span");
		
		public H injection = new H("span");
		public H injectionCount = new H("span");
		
		
		public H root;
		public CarMetrics()
		{
			root = new H("div").addChilds
			(
				new H("span").attrs("#text", "Lambda: "),
				lambda,
				new H("span").attrs("#html", "&nbsp&nbsp&nbsp"),
				
				new H("span").attrs("#text", "TP: "),
				throttle,
				new H("span").attrs("#text", "%"),
				new H("span").attrs("#html", "&nbsp&nbsp&nbsp"),
				
				new H("span").attrs("#text", "Injection: "),
				injection,
				new H("span").attrs("#text", "%"),
				new H("span").attrs("#html", "&nbsp&nbsp&nbsp"),
				
				new H("span").attrs("#text", "("),
				injectionCount,
				new H("span").attrs("#text", ")")
			);
		}
	}
	
	protected static class AvgCarMetrics
	{
		protected CarMetrics dst;
		
		public AvgCarMetrics(CarMetrics dst)
		{
			this.dst = dst;
		}
		
		protected long time;
		protected long inject;

		protected int injectCount;
		
		protected long lambda;
		protected long tp;
		
		public void update(long dt, long inject, int count, int tp, int lambda)
		{
			this.time += dt;
			this.inject += inject;
			this.injectCount += count;
			this.lambda += lambda*dt;
			this.tp += tp*dt;
			
			dst.lambda.attrs("#text", String.valueOf(lambda/time));
			dst.throttle.attrs("#text", String.valueOf(this.tp/time));
			dst.injection.attrs("#text", String.valueOf((this.inject*400)/this.time));
			dst.injectionCount.attrs("#text", String.valueOf(this.injectCount));
		}
	}
	
	//binary arithmetic looks like broken in teavm
	public static int read_uint16(PacketReader pr)
	{
		byte[] data = pr.readBlob(2);
		int ret = 0;
		ret |= (data[0] & 0xff) << 8;
		ret |= data[1] & 0xff;
		return ret;
	}
	
	public static long read_uint32(PacketReader pr)
	{
		byte[] data = pr.readBlob(4);
		long ret = 0;
		ret |= (data[0] & 0xff) << 24;
		ret |= (data[1] & 0xff) << 16;
		ret |= (data[2] & 0xff) << 8;
		ret |= data[3] & 0xff;
		return ret;
	}
	
	public static void main(String[] args) throws Throwable
	{
		SkeletonStyleAlaCarte.values();
		H root = PostComfortFrontendTools.getDesignedRoot();
		
		CarMetrics current = new CarMetrics();

		CarMetrics session = new CarMetrics();
		
		root.addChilds
		(
			new H("div").addChilds
			(
				new H("h2").attrs("#text", "Current car metrics"),
				current.root
			),
			new H("br"),
			new H("div").addChilds
			(
				new H("h2").attrs("#text", "Session average car metrics"),
				session.root
			)
		);
		
		
		AvgCarMetrics avg = new AvgCarMetrics(session);
		
		//TODO total and since selected time
		
		new Thread()
		{
			public void run()
			{
				while(true)
				{
					byte[] data = ENGINE.getNextPacket();
					
					if(data.length < 16)
					{
						continue;
					}
					
					try
					{
						boolean valid = UartbusTools.crc8(data, data.length-1) == data[data.length-1];
						if(!valid)
						{
							continue;
						}
						
						if
						(
								95 == data[0]
							&&
								10 == data[2]
						)
						{
							PacketReader pr = new PacketReader(data);
							pr.readBlob(3);//skip first 3 bytes
							
							long dt = read_uint32(pr);
							int lambda = read_uint16(pr);
							int throttle = read_uint16(pr);
							int injectCount = read_uint16(pr);
							long injectTime = read_uint32(pr);
							
							current.lambda.attrs("#text", String.valueOf(lambda/10));
							current.throttle.attrs("#text", String.valueOf(throttle/10));
							current.injection.attrs("#text", String.valueOf((injectTime*400)/dt));
							current.injectionCount.attrs("#text", String.valueOf(injectCount));
							
							avg.update(dt, injectTime, injectCount, throttle/10, lambda/10);
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
}
