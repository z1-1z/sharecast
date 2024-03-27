package com.intelligent.share.socketthread;

import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.jcraft.jzlib.GsZilb;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import smart.share.CreateSocket;
import smart.share.TranGlobalInfo;
import smart.share.GlobalConstantValue;
import smart.share.ConnectToDevice;
import smart.share.MobileLoginInfo;
import smart.share.SendSocket;
import smart.share.message.process.MessageProcessor;

public class SocketReceiveThread extends Thread
{
	private static final String TAG = SocketReceiveThread.class.getSimpleName();
	private static final int MAX_DATA_LENGTH = 1024 * 1024;
	private InputStream inStream;

	private boolean interruptFlag = false;
	private boolean enableRecvUsefulData;

	private int totalDataCount = 0;
	private final int SOCKET_KEEP_ALIVE_TIMEOUT = 12*1000;
	public static final int DEFAULT_SEND_KEEP_ALIVE_MSG_MAX_TIMES_LARGE = 20;
	public static final int DEFAULT_SEND_KEEP_ALIVE_MSG_MAX_TIMES_NORMAL = 5;
	private int sendKeepAliveMsgMaxTimes = DEFAULT_SEND_KEEP_ALIVE_MSG_MAX_TIMES_NORMAL;

	public void setSendKeepAliveMsgMaxTimes(int sendKeepAliveMsgMaxTimes) {
		this.sendKeepAliveMsgMaxTimes = sendKeepAliveMsgMaxTimes;
	}

	private MessageProcessor msgProc;

	public SocketReceiveThread()
	{
		super("SocketReceiveThread");
	}

	@Override
	public void run()
	{
		byte[] buffer = new byte[GlobalConstantValue.BUFF_LENGTH_RECEIVE_DATA_PER_TIME];
		int dataLen = 0;
		int dataLenLeft = 0;
		int dataType = 0;
		int msgResponseState = 0;
		String dataHeader;
		int recvDataCount = -1;
		int buffLen = GlobalConstantValue.BUFF_LENGTH_RECEIVE_DATA_PER_TIME;
		long recDataTimeMark;
		boolean needSendKeepAliveMsg = true;
		int sendKeepAliveTryTimes = 0;
		msgProc = MessageProcessor.obtain();
		enableRecvUsefulData = false;
		recDataTimeMark = SystemClock.uptimeMillis();
		while (interruptFlag == false)
		{
			try
			{
				if (CreateSocket.GetSocket() != null) {
					inStream = CreateSocket.GetSocket().getInputStream();
				}
				if (inStream != null) {
					if (enableRecvUsefulData == false)
					{
						try
						{
							recvDataCount = inStream.read(buffer, 0, GlobalConstantValue.CONTROL_DATA_MSG_LENGTH);
							if (recvDataCount != -1)
							{
								dataHeader = new String("" + (char) buffer[0] + (char) buffer[1] + (char) buffer[2] + (char) buffer[3]);
								if (dataHeader.equals(GlobalConstantValue.CONTROL_DATA_HEADER_STR))
								{
									dataLen = (int) (((buffer[7] << 24) & 0xff000000) | ((buffer[6] << 16) & 0x00ff0000) | ((buffer[5] << 8) & 0x0000ff00) | (buffer[4] & 0x000000ff));
									dataType = (int) (((buffer[11] << 24) & 0xff000000) | ((buffer[10] << 16) & 0x00ff0000) | ((buffer[9] << 8) & 0x0000ff00) | (buffer[8] & 0x000000ff));
									msgResponseState = (int) (((buffer[15] << 24) & 0xff000000) | ((buffer[14] << 16) & 0x00ff0000) | ((buffer[13] << 8) & 0x0000ff00) | (buffer[12] & 0x000000ff));
									enableRecvUsefulData = dataLen >= 0 && dataLen < MAX_DATA_LENGTH;
									Log.d(TAG, "dataLen = " + dataLen + ",dataType = " + dataType + ",enableRecvUsefulData = " + enableRecvUsefulData);
									needSendKeepAliveMsg = true;
									sendKeepAliveTryTimes = 0;
								}
							}
							else
							{
								Log.i(TAG, "[xxx] run: App return login menu, because receive data is empty.");
								if (!interruptFlag) {
									CreateSocket.DestroySocket();
									MobileLoginInfo loginInfoTemp = ConnectToDevice.connecttoserver(CreateSocket.getAddress(), CreateSocket.getPort(), GlobalConstantValue.CONNECT_TYEP_AUTO_LOGIN);
									TranGlobalInfo.setCurDeviceInfo(loginInfoTemp);
								}
							}
						}
						catch (SocketTimeoutException e)
						{
							Log.i(TAG, "[xxx] run: e " + e);
							if (needSendKeepAliveMsg)
							{
								recDataTimeMark = SystemClock.uptimeMillis();
								needSendKeepAliveMsg = false;
								boolean bsendOk = SendSocket.SyncSendOnlyCommandSocketToDevice(CreateSocket.GetSocket(), GlobalConstantValue.S_MSG_REQUEST_SOCKET_KEEP_ALIVE);
								Log.d(TAG, "send heart run "+(bsendOk ? "ok":"fail"));
							}
							else
							{
								/**EN:If the time between the first time of send heartrun over 30 seconds,
								 * and nothing reply, we consider the connection has been disconnected */
								if (SystemClock.uptimeMillis() - recDataTimeMark > SOCKET_KEEP_ALIVE_TIMEOUT)
								{
									Log.i(TAG, "[xxx] run: sendKeepAliveTryTimes = " + sendKeepAliveTryTimes);
									Log.i(TAG, "[xxx] run: needSendKeepAliveMsg = " + needSendKeepAliveMsg);
									if (++sendKeepAliveTryTimes <= sendKeepAliveMsgMaxTimes) {
										needSendKeepAliveMsg = true;
									} else {
										sendKeepAliveTryTimes = 0;
										msgProc.postEmptyMessage(GlobalConstantValue.GSCMD_NOTIFY_SOCKET_CLOSED);
//										interruptFlag = true;
										Log.d(TAG, "send heartrun over 30 seconds, nothing reveive");
									}

								}
							}
						}
						catch (SocketException e)
						{
							Log.i(TAG, "App return login menu, SocketException :\n" + e.getMessage());
							if (!interruptFlag) {
								CreateSocket.DestroySocket();
								MobileLoginInfo loginInfoTemp = ConnectToDevice.connecttoserver(CreateSocket.getAddress(), CreateSocket.getPort(), GlobalConstantValue.CONNECT_TYEP_AUTO_LOGIN);
								TranGlobalInfo.setCurDeviceInfo(loginInfoTemp);
							}
						}
					}
					else
					{
						byte[] response_buffer = new byte[dataLen + 8];
						totalDataCount = 0;
						dataLenLeft = dataLen;
						while (totalDataCount < dataLen)
						{
							Log.i(TAG,"-------data left len: " + dataLenLeft);
							try
							{
								recvDataCount = inStream.read(buffer, 0, Math.min(dataLenLeft, buffLen));
								if (recvDataCount != -1)
								{
									System.arraycopy(buffer, 0, response_buffer, totalDataCount, recvDataCount);
									totalDataCount = totalDataCount + recvDataCount;
									dataLenLeft = dataLenLeft - recvDataCount;
								}
								else
								{
									break; // Avoid infinite loop;
								}
							}
							catch (SocketTimeoutException e)
							{
								Log.i(TAG,"SocketTimeoutException=totalDataCount=" + totalDataCount);
								Message dataMessage;
								dataMessage = Message.obtain();
								dataMessage.arg1 = 0;
								dataMessage.arg2 = TranGlobalInfo.SOCKET_TIME_OUT_EXCEPTION;
								dataMessage.what = dataType;
								msgProc.postMessage(dataMessage);

								e.printStackTrace();

								// msgProc.postEmptyMessage(GlobalConstantValue.GSCMD_SEND_EMAIL_EXCEPTION);
								break;
							}
						}
						Log.i(TAG, "[xxx] run: recvDataCount " + recvDataCount);
						if (totalDataCount == dataLen)
						{
							if (dataType <= GlobalConstantValue.S_MSG_NOTIFY_INPUT_PASSWORD_CANCEL || dataType >= GlobalConstantValue.S_MSG_NOTIFY_MAX_VALUE)
							{
								recDataTimeMark = SystemClock.uptimeMillis();
								needSendKeepAliveMsg = true;
								sendKeepAliveTryTimes = 0;
							}
							if (dataType == GlobalConstantValue.S_MSG_NOTIFY_CLIENT_TYPE_BECOME_MASTER)
							{
								TranGlobalInfo.getCurDeviceInfo().setClient_type(TranGlobalInfo.CLIENT_TYPE_MASTER);
							}

							Message dataMessage;
							dataMessage = Message.obtain();
							dataMessage.arg1 = 0;
							dataMessage.arg2 = msgResponseState;

							byte[] unCompressBuffer = null;
							if (dataLen != 0)
							{
								unCompressBuffer = GsZilb.UnCompress(response_buffer);
								dataMessage.arg1 = unCompressBuffer.length;
							}

							dataMessage.what = dataType;
							Bundle data = new Bundle();
							data.putByteArray("ReceivedData", unCompressBuffer);
							dataMessage.setData(data);

						 	Log.i(TAG,"dataMessage.what=="+dataMessage.what+"==msgResponseState=="+msgResponseState);

							msgProc.postMessage(dataMessage);
						}
						enableRecvUsefulData = false;
					}
				}
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Log.i(TAG,"run interrupt");
	}

	@Override
	public void interrupt()
	{
		interruptFlag = true;
		super.interrupt();
	}
}