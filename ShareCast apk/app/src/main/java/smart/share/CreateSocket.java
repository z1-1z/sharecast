package smart.share;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class CreateSocket {
	private static final String TAG = "CreateSocket";
	/** TCP style */
	private static Socket socketHandle = null;
	private static String Address;
	private static int Port;
	
	public static void config(String NetAddress,int NetPort)
	{
		Address = NetAddress;
		Port = NetPort;
	}

	public static Socket GetSocket()
	{
		return socketHandle;
	}

	public static synchronized boolean connect() {
		DestroySocket();
		boolean ret = false;
		socketHandle = new Socket();
		try {
			socketHandle.connect(new InetSocketAddress(Address, Port), GlobalConstantValue.SOCKET_TCP_TIMEOUT);
			ret = socketHandle.isConnected();
			Log.i(TAG, "[xxx] GetSocket: connect " + socketHandle.isConnected());
		} catch (IOException e) {
			Log.i(TAG, "[xxx] GetSocket: e = " + e);
			e.printStackTrace();
		}
		return ret;
	}
	
	public static synchronized void DestroySocket()
	{
		if (socketHandle != null)
		{
			try
			{
				socketHandle.shutdownInput();
				socketHandle.shutdownOutput();
				socketHandle.close();
			}
			catch (Exception e)
			{
			}
			socketHandle = null;
		}
	}

	public static String getAddress() {
		return Address;
	}

	public static int getPort() {
		return Port;
	}
}
