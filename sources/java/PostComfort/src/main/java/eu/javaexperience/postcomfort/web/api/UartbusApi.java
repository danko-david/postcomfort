package eu.javaexperience.postcomfort.web.api;

import java.io.IOException;
import java.util.Map;

import eu.javaexperience.electronic.uartbus.rpc.UartbusConnection;
import eu.javaexperience.text.Format;
import eu.teasite.frontend.api.ApiInterface;

public class UartbusApi extends ApiInterface implements UartbusConnection
{
	public void sendPacket(byte[] data)
	{
		transfer.transmitSync(pack("sendPacket", Format.base64Encode(data)));
	}
	
	public byte[] getNextPacket()
	{
		return Format.base64Decode(transfer.transmitSync(pack("getNextPacket")));
	}

	@Override
	public void close() throws IOException{}

	@Override
	public Map<String, String> listAttributes()
	{
		return null;
	}

	@Override
	public String getAttribute(String key) throws IOException
	{
		return transfer.transmitSync(pack("getAttribute", key));
	}

	@Override
	public void setAttribute(String key, String value) throws IOException
	{
		transfer.transmitSync(pack("setAttribute", key, value));
	}

	@Override
	public long getCurrentPacketIndex() throws IOException
	{
		return 0;
	}

	@Override
	public byte[] getPacket(long index) throws IOException
	{
		return null;
	}
}