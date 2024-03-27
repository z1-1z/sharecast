package com.intelligent.share.socketthread;

import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import smart.share.TranGlobalInfo;
import smart.share.GlobalConstantValue;
import smart.share.MobileLoginInfo;
import smart.share.message.process.MessageProcessor;

public class UdpSocketReceiveBroadcastThread extends Thread
{
	private String TAG = UdpSocketReceiveBroadcastThread.class.getSimpleName();
	private static final int TIMEOUT_5S = 5 * 1000;//CN:５秒作为socket接收超时时间
	private DatagramPacket udpPacket;
	private DatagramSocket udpBroadcastSocket;
	private long timeMark;

	private boolean interruptFlag = false;
	private ArrayList<MobileLoginInfo> deviceInfoList = new ArrayList<MobileLoginInfo>();

	private MessageProcessor msgProc;

	public UdpSocketReceiveBroadcastThread()
	{
		super("UdpSocketReceiveBroadcastThread");
	}

	@Override
	public void run()
	{
		super.run();
		msgProc = MessageProcessor.obtain();
		try
		{
			udpBroadcastSocket = new DatagramSocket(GlobalConstantValue.BROADCAST_PORT);
			udpBroadcastSocket.setSoTimeout(TIMEOUT_5S);
			byte[] buffer = new byte[GlobalConstantValue.BUFF_LENGTH_RECEIVE_DATA_PER_TIME];
			udpPacket = new DatagramPacket(buffer, buffer.length);
			Log.d(TAG, "Thread  BroadcastThread onStart");
			timeMark = SystemClock.uptimeMillis();
			while (interruptFlag == false)
			{
				try
				{
					udpBroadcastSocket.receive(udpPacket);
					String hostAddress = udpPacket.getAddress().getHostAddress();
//					Log.d(TAG, "receive udp packet from " + hostAddress);
					byte[] receiveBuffer = new byte[udpPacket.getLength()];
					receiveBuffer = udpPacket.getData();
					if (udpPacket.getLength() != GlobalConstantValue.DEVICE_LOGIN_INFO_DATA_LENGTH)
					{
						continue;
					}
					scramble_info_for_broadcast(receiveBuffer, udpPacket.getLength());
					String stringMagicCode = new String(receiveBuffer, 0, GlobalConstantValue.BROADCAST_INFO_MAGIC_CODE_LEN);
					if (!TextUtils.isEmpty(stringMagicCode) && (stringMagicCode.equals(GlobalConstantValue.BROADCAST_INFO_MAGIC_CODE)))
					{
						MobileLoginInfo loginInfoTemp = new MobileLoginInfo(receiveBuffer);
						Log.i(TAG, "[xxx] run: getModel_name " + loginInfoTemp.getModel_name());
						Log.i(TAG, "[xxx] run: getPlatform_id " + loginInfoTemp.getPlatform_id());
						Log.i(TAG, "[xxx] run: check_is_apk_match_platform " + TranGlobalInfo.check_is_apk_match_platform(loginInfoTemp.getPlatform_id()));
						loginInfoTemp.setLastFoundTime(SystemClock.uptimeMillis());//CN:记录发现时间
						if (TranGlobalInfo.check_is_apk_match_platform(loginInfoTemp.getPlatform_id()))
						{
							int index;
							boolean bFoundNew = true;
							for (index = 0; index < deviceInfoList.size(); index++)
							{
								MobileLoginInfo longinInfoInList = deviceInfoList.get(index);
								Log.i(TAG, "[xxx] run: longinInfoInList.getDevice_sn_disp() " + longinInfoInList.getDevice_sn_disp());
								Log.i(TAG, "[xxx] run: longinInfoInList.getDevice_sn_disp().equals(loginInfoTemp.getDevice_sn_disp()) " + longinInfoInList.getDevice_sn_disp().equals(loginInfoTemp.getDevice_sn_disp()));
								if (!TextUtils.isEmpty(longinInfoInList.getDevice_sn_disp()) && longinInfoInList.getDevice_sn_disp().equals(loginInfoTemp.getDevice_sn_disp()))
								{
									longinInfoInList.setLastFoundTime(loginInfoTemp.getLastFoundTime());
									// Reduplicated login info.
									if (loginInfoTemp.getIs_current_device_connected_full() == 1)
									{
										deviceInfoList.remove(index);
										update_device_info_to_login_list();
										Log.d(TAG, "device is full remove it " + hostAddress);
									}
									else
									{
										Log.d(TAG, "device update " + hostAddress);
										deviceInfoList.set(index, loginInfoTemp);//CN:更新设备信息
										if (!TextUtils.isEmpty(longinInfoInList.getDevice_ip_address_disp()) && !longinInfoInList.getDevice_ip_address_disp().equals(loginInfoTemp.getDevice_ip_address_disp()))
										{
											update_device_info_to_login_list();
										}
									}
									bFoundNew = false;
									break;
								}
							}
							Log.d(TAG, "bFoundNew " + bFoundNew);
							Log.d(TAG, "loginInfoTemp.getIs_current_device_connected_full() " + loginInfoTemp.getIs_current_device_connected_full());
							if (bFoundNew && (loginInfoTemp.getIs_current_device_connected_full() == 0))
							{
								deviceInfoList.add(loginInfoTemp);
								update_device_info_to_login_list();
							}
						}
					}
					else
					{
						Log.d(TAG, "Thread  BroadcastThread receive error data. magic code wrong!: " + stringMagicCode);
					}
				}
				catch (SocketTimeoutException e)
				{
					deviceInfoList.clear();
					Log.d(TAG, "SocketTimeoutException, no device found");
					update_device_info_to_login_list();
					timeMark = SystemClock.uptimeMillis();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				//CN:5秒检测一次所有设备的上次发现时间
				if (SystemClock.uptimeMillis() - timeMark >= TIMEOUT_5S)
				{
					boolean bDeviceRM = false;
					for (int index = 0; index < deviceInfoList.size(); )
					{
						MobileLoginInfo longinInfoInList = deviceInfoList.get(index);
						if (SystemClock.uptimeMillis() - longinInfoInList.getLastFoundTime() > TIMEOUT_5S)
						{
							deviceInfoList.remove(index);
							Log.d(TAG, "remove " + longinInfoInList.getDevice_ip_address_disp() + " because device no response");
							bDeviceRM = true;
						}
						else
						{
							index++;
						}
					}
					if(bDeviceRM)
					{
						update_device_info_to_login_list();
					}
					timeMark = SystemClock.uptimeMillis();
				}
			}
			Log.d(TAG, "run interrupt1");
		}
		catch (SocketException e1)
		{
			e1.printStackTrace();
		}
	}

	@Override
	public void interrupt()
	{
		Log.d(TAG, "recv interrupt2");
		interruptFlag = true;
		if (udpBroadcastSocket != null)
		{
			udpBroadcastSocket.disconnect();
			udpBroadcastSocket.close();
		}
		super.interrupt();
	}

	public static void scramble_info_for_broadcast(byte[] send_buff, int buffLength)
	{
		int device_info_length = 0;
		int index = 0;
		byte temp;

		device_info_length = buffLength;
		for (index = 0; index < device_info_length / 2; index++)
		{
			// reverse the order of info string.
			temp = send_buff[(device_info_length - 1) - index];
			send_buff[(device_info_length - 1) - index] = send_buff[index];
			send_buff[index] = temp;
			// xor the every byte.
			send_buff[index] = (byte) (send_buff[index] ^ GlobalConstantValue.BROADCAST_INFO_XOR_VALUE);
			send_buff[(device_info_length - 1) - index] = (byte) (send_buff[(device_info_length - 1) - index] ^ GlobalConstantValue.BROADCAST_INFO_XOR_VALUE);
		}
		if (device_info_length % 2 != 0)
		{
			send_buff[device_info_length / 2] = (byte) (send_buff[device_info_length / 2] ^ GlobalConstantValue.BROADCAST_INFO_XOR_VALUE);
		}
	}

	private void update_device_info_to_login_list()
	{
		Message dataMessage;
		dataMessage = Message.obtain();
		dataMessage.what = GlobalConstantValue.GSCMD_NOTIFY_BROADCAST_LOGIN_INFO_UPDATED;
		dataMessage.obj = deviceInfoList;
		msgProc.postMessage(dataMessage);
	}
}
