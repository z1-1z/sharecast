package smart.share;

import android.util.Log;

import com.tosmart.dlna.util.ThreadUtils;

import java.io.OutputStream;
import java.net.Socket;

import smart.share.dataconvert.parser.DataParser;
import smart.share.dataconvert.parser.ParserFactory;

public class SendSocket
{
	private static final String TAG = "SendSocket";
	private static final int	MAX_DATA_LENGTH_BIT			= 7;
	private static final String	SOCKET_HEADER_START_FLAG	= "Start";
	private static final String	SOCKET_HEADER_END_FLAG		= "End";

	private static byte[] subBytes(byte[] src, int begin, int count)
	{
		byte[] bs = new byte[count];
		for (int i = 0; i < count; i++)
			bs[i] = src[i + begin];
		return bs;
	}

	public synchronized static void sendSocketToDevice(final byte[] buffer, final Socket tcpSocket, final int offset, final int dataLength, final int commandType)
	{
		ThreadUtils.execute(() -> SyncSendSocketToDevice(buffer,tcpSocket,offset,dataLength,commandType));
	}

	public synchronized static boolean SyncSendSocketToDevice(byte[] buffer, Socket tcpSocket, int offset, int dataLength, int commandType)
	{
		if (tcpSocket == null) {
			return false;
		}

		OutputStream out;
		byte[] newBuffer;
		byte[] newBytes;

		newBytes = subBytes(buffer, offset, dataLength);
		String str = new String(newBytes);
		String dataLenStr = "" + dataLength;
		int needAddZeroNum = MAX_DATA_LENGTH_BIT - dataLenStr.length();
		String strDataLen = "";
		for (int i = 0; i < needAddZeroNum; i++)
		{
			strDataLen += "0";
		}
		newBuffer = ((SOCKET_HEADER_START_FLAG + strDataLen + dataLength + SOCKET_HEADER_END_FLAG) + str).getBytes();
		try
		{
			out = tcpSocket.getOutputStream();
			out.write(newBuffer, 0, newBuffer.length);
			out.flush();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static void sendOnlyCommandSocketToDevice(Socket tcpSocket, int commandType)
	{
		byte[] dataCommand = null;
		DataParser parser = ParserFactory.getParser();

		try
		{
			dataCommand = parser.serialize(null, commandType).getBytes("UTF-8");
		}
		catch (Exception e)
		{
			Log.i(TAG, "[xxx] sendOnlyCommandSocketToDevice:" + e);
		}
		sendSocketToDevice(dataCommand, tcpSocket, 0, dataCommand.length, commandType);
	}

	public static boolean SyncSendOnlyCommandSocketToDevice(Socket tcpSocket, int commandType)
	{
		byte[] dataCommand = null;
		DataParser parser = ParserFactory.getParser();

		try
		{
			dataCommand = parser.serialize(null, commandType).getBytes("UTF-8");
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return SyncSendSocketToDevice(dataCommand, tcpSocket, 0, dataCommand.length, commandType);
	}
}
