package smart.share;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import com.intelligent.share.R;
import com.intelligent.share.socketthread.UdpSocketReceiveBroadcastThread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import smart.share.dataconvert.parser.ParserFactory;
import smart.share.dataconvert.parser.XmlParser;
import smart.share.util.AndroidDeviceUtil;


public class ConnectToDevice {
	private static final String	UPNP_HANDSHARK_MSG_MOBILE			= "HANDSHAKE_MOBILE";
	private static final String	UPNP_HANDSHARK_MSG_MOBILE_LIST		= "HANDSHAKE_MOBILE_LIST";
	/*
	 * upnp connect to server
	 */
	public static MobileLoginInfo upnpConnectToServer(String Address,
													  int Port, int connectType) {

		/** TCP style */
		int bytesNum = -1;
		Socket tcpSocket;
		InputStream in;
		MobileLoginInfo loginInfoTemp = new MobileLoginInfo();
		loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_UNKONWN_ERROR);
		boolean is_create_socket_success = false;

		try {
			CreateSocket.config(Address,Port);
			CreateSocket.connect();
			CreateSocket.GetSocket().setSoTimeout(4000);
			in = CreateSocket.GetSocket().getInputStream();
			// hand shark send
			{
				byte[] handSharkRceveByte = new byte[GlobalConstantValue.DEVICE_LOGIN_INFO_DATA_LENGTH];
				OutputStream out = CreateSocket.GetSocket().getOutputStream();
				byte[] handSharkSendData = UPNP_HANDSHARK_MSG_MOBILE.getBytes();
				out.write(handSharkSendData, 0, handSharkSendData.length);
				Thread.sleep(300);
				in.read(handSharkRceveByte, 0, GlobalConstantValue.DEVICE_LOGIN_INFO_DATA_LENGTH);
				String handSharkReceveString = new String(handSharkRceveByte, "UTF-8");
				handSharkReceveString = handSharkReceveString.trim();
				if (!handSharkReceveString.equals(UPNP_HANDSHARK_MSG_MOBILE)) {
					loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_HAND_SHARK_ERROR);
					CreateSocket.DestroySocket();
					return loginInfoTemp;
				}
			}
			Thread.sleep(300);
			// send request
			ArrayList<Map<String, String>> mLogInfos = new ArrayList<Map<String, String>>();
			Map<String, String> logInfo = new HashMap<String, String>();
			logInfo.put("data", Build.MODEL);
			logInfo.put("uuid", AndroidDeviceUtil.getDeviceUUID());
			mLogInfos.add(logInfo);
//			ArrayList<XmlOneDataModel> devNames = new ArrayList<XmlOneDataModel>();
//			XmlOneDataModel devName = new XmlOneDataModel();
//			devName.setData(Build.DEVICE);
//			devNames.add(devName);
			XmlParser xmlParser = new XmlParser();
			byte[] sendData =xmlParser.serialize(mLogInfos, GlobalConstantValue.S_MSG_REQUEST_LOGIN_INFO).getBytes();
			SendSocket.sendSocketToDevice(sendData, CreateSocket.GetSocket(), 0, sendData.length, GlobalConstantValue.S_MSG_REQUEST_LOGIN_INFO);

			// receive confirm data
			byte[] receiveBuffer = new byte[GlobalConstantValue.DEVICE_LOGIN_INFO_DATA_LENGTH];
			is_create_socket_success = true;
			bytesNum = in.read(receiveBuffer, 0, GlobalConstantValue.DEVICE_LOGIN_INFO_DATA_LENGTH);

			System.out.println("Connect type:" + connectType);
			System.out.println("connect server data bytes Num: " + bytesNum);

			boolean isDeviceValid = false;
			int isDeviceConnectFull = 0;
			if (bytesNum == GlobalConstantValue.DEVICE_LOGIN_INFO_DATA_LENGTH) {
				UdpSocketReceiveBroadcastThread.scramble_info_for_broadcast(receiveBuffer, bytesNum);
				System.out.println("receiveBuffer =  " + new String(receiveBuffer));
				String stringMagicCode = new String(receiveBuffer, 0, GlobalConstantValue.BROADCAST_INFO_MAGIC_CODE_LEN);
				if ((stringMagicCode.equals(GlobalConstantValue.BROADCAST_INFO_MAGIC_CODE))) {
					loginInfoTemp = new MobileLoginInfo(receiveBuffer);
					if (TranGlobalInfo.check_is_apk_match_platform(loginInfoTemp.getPlatform_id())) {
						isDeviceValid = true;
						if (connectType == GlobalConstantValue.CONNECT_TYEP_IP_LOGIN) {
							loginInfoTemp.setmIpLoginMark(1);
						}
						loginInfoTemp.setmConnectStatus(bytesNum);
						(TranGlobalInfo.getCurDeviceInfo()).setClient_type(loginInfoTemp.getClient_type());
						isDeviceConnectFull = loginInfoTemp.getIs_current_device_connected_full();
						System.out.println("connect server getClient_type: " + loginInfoTemp.getClient_type());
					}
				} else {
					System.out.println("stringMagicCode =  " + stringMagicCode);
				}
			}
			if (isDeviceValid == false) {
				loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_DATA_TRANSMISSION_FAIL);
				CreateSocket.DestroySocket();
			} else if (isDeviceConnectFull == 1) // Device have connected full.
			{
				loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_DEVICE_IS_FULL);
				CreateSocket.DestroySocket();
			}else{
				ParserFactory.setDataType(loginInfoTemp.getSend_data_type());
			}
		} catch (IllegalArgumentException e) {
			loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_IP_ERROR);
			CreateSocket.DestroySocket();
		} catch (SocketTimeoutException e) {
			if (is_create_socket_success) {
				loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_DATA_TRANSMISSION_FAIL);
			} else {
				loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_NOT_RESPONSE);
			}
			CreateSocket.DestroySocket();
		} catch (ConnectException e) {
			loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_NOT_REACHABLE);
			CreateSocket.DestroySocket();
		} catch (SocketException e) {
			loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_NOT_VALID);
			CreateSocket.DestroySocket();
		} catch (IOException e) {
			CreateSocket.DestroySocket();
		} catch (Exception e) {
			CreateSocket.DestroySocket();
		}
		return loginInfoTemp;
	}
	
	/*
	 * send UPNP_HANDSHARK_MSG_MOBILE_LIST to Device and the Login Info whether
	 * success for fail should close the socket
	 */
	public static MobileLoginInfo upnpGetDeviceList(String Address, int Port,
													int connectType) {

		/** TCP style */
		int bytesNum = -1;
		Socket tcpSocket;
		InputStream in;
		MobileLoginInfo loginInfoTemp = new MobileLoginInfo();
		loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_UNKONWN_ERROR);

		try {
			CreateSocket.config(Address,Port);
			CreateSocket.DestroySocket();
			CreateSocket.connect();
			tcpSocket = CreateSocket.GetSocket();
			tcpSocket.setSoTimeout(4000);
			in = tcpSocket.getInputStream();
			// send UPNP_HANDSHARK_MSG_MOBILE_LIST to Device
			{
				OutputStream out = tcpSocket.getOutputStream();
				byte[] handSharkSendData = UPNP_HANDSHARK_MSG_MOBILE_LIST.getBytes();
				out.write(handSharkSendData, 0, handSharkSendData.length);
			}
			Thread.sleep(300);

			// receive confirm data(Login Info)
			byte[] receiveBuffer = new byte[GlobalConstantValue.DEVICE_LOGIN_INFO_DATA_LENGTH];
			bytesNum = in.read(receiveBuffer, 0, GlobalConstantValue.DEVICE_LOGIN_INFO_DATA_LENGTH);
			System.out.println("Connect type:" + connectType);
			System.out.println("connect server data bytes Num: " + bytesNum);

			boolean isDeviceValid = false;
			int isDeviceConnectFull = 0;
			if (bytesNum == GlobalConstantValue.DEVICE_LOGIN_INFO_DATA_LENGTH) {
				UdpSocketReceiveBroadcastThread.scramble_info_for_broadcast(receiveBuffer, bytesNum);
				System.out.println("receiveBuffer =  " + new String(receiveBuffer));
				String stringMagicCode = new String(receiveBuffer, 0, GlobalConstantValue.BROADCAST_INFO_MAGIC_CODE_LEN);
				if ((stringMagicCode.equals(GlobalConstantValue.BROADCAST_INFO_MAGIC_CODE))) {
					loginInfoTemp = new MobileLoginInfo(receiveBuffer);
					if (TranGlobalInfo.check_is_apk_match_platform(loginInfoTemp.getPlatform_id())) {
						isDeviceValid = true;
						if (connectType == GlobalConstantValue.CONNECT_TYEP_IP_LOGIN) {
							loginInfoTemp.setmIpLoginMark(1);
						}
						loginInfoTemp.setmConnectStatus(bytesNum);
						(TranGlobalInfo.getCurDeviceInfo()).setClient_type(loginInfoTemp.getClient_type());
						isDeviceConnectFull = loginInfoTemp.getIs_current_device_connected_full();
						System.out.println("connect server getClient_type: " + loginInfoTemp.getClient_type());
					}
				} else {
					System.out.println("stringMagicCode =  " + stringMagicCode);
				}
			}
			/*
			 * send UPNP_HANDSHARK_MSG_MOBILE_LIST to Device and the Login Info
			 * whether success or fail should close the socket
			 */
			if (isDeviceValid == false) {
				loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_DATA_TRANSMISSION_FAIL);
				CreateSocket.DestroySocket();
			} else if (isDeviceConnectFull == 1) // Device have connected full.

			{
				loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_DEVICE_IS_FULL);
				CreateSocket.DestroySocket();
			} else {
				CreateSocket.DestroySocket();
			}
		} catch (IllegalArgumentException e) {
			loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_IP_ERROR);
			CreateSocket.DestroySocket();
		} catch (SocketTimeoutException e) {
				loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_NOT_RESPONSE);
			CreateSocket.DestroySocket();
		} catch (ConnectException e) {
			loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_NOT_REACHABLE);
			CreateSocket.DestroySocket();
		} catch (SocketException e) {
			loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_NOT_VALID);
			CreateSocket.DestroySocket();
		} catch (IOException e) {
			CreateSocket.DestroySocket();
		} catch (Exception e) {
			CreateSocket.DestroySocket();
		}
		CreateSocket.DestroySocket(); // make sure close the scoket
		return loginInfoTemp;
	}
	
	/*
	 * port: 20000
	 */
	public static MobileLoginInfo connecttoserver(String Address, int Port,
												  int connectType) {

		/** TCP style */
		int bytesNum = -1;
		Socket tcpSocket;
		InputStream in;
		CreateSocket cSocket = null;
		MobileLoginInfo loginInfoTemp = new MobileLoginInfo();
		loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_UNKONWN_ERROR);

		try {
			CreateSocket.config(Address,Port);
			CreateSocket.connect();
			tcpSocket = CreateSocket.GetSocket();
			tcpSocket.setSoTimeout(4000);
			in = tcpSocket.getInputStream();

			// send request
			ArrayList<Map<String, String>> mLogInfos = new ArrayList<Map<String, String>>();
			Map<String, String> logInfo = new HashMap<String, String>();
			logInfo.put("data", Build.MODEL);
			logInfo.put("uuid", AndroidDeviceUtil.getDeviceUUID());
			mLogInfos.add(logInfo);
//			ArrayList<XmlOneDataModel> devNames = new ArrayList<XmlOneDataModel>();
//			XmlOneDataModel devName = new XmlOneDataModel();
//			devName.setData(Build.DEVICE);
//			devNames.add(devName);
			XmlParser xmlParser = new XmlParser();
			byte[] sendData = xmlParser.serialize(mLogInfos, GlobalConstantValue.S_MSG_REQUEST_LOGIN_INFO).getBytes();
			SendSocket.sendSocketToDevice(sendData, tcpSocket, 0, sendData.length, GlobalConstantValue.S_MSG_REQUEST_LOGIN_INFO);
			System.out.println("sendBuffer =  "+ new String(sendData));
			Thread.sleep(300);

			// receive confirm data
			byte[] receiveBuffer = new byte[GlobalConstantValue.DEVICE_LOGIN_INFO_DATA_LENGTH];
			bytesNum = in.read(receiveBuffer, 0,
					GlobalConstantValue.DEVICE_LOGIN_INFO_DATA_LENGTH);

			System.out.println("Connect type:" + connectType);
			System.out.println("connect server data bytes Num: " + bytesNum);

			boolean isDeviceValid = false;
			int isDeviceConnectFull = 0;
			if (bytesNum == GlobalConstantValue.DEVICE_LOGIN_INFO_DATA_LENGTH) {
				UdpSocketReceiveBroadcastThread
						.scramble_info_for_broadcast(receiveBuffer,
								bytesNum);
				System.out.println("receiveBuffer =  "
						+ new String(receiveBuffer));
				String stringMagicCode = new String(receiveBuffer, 0,
						GlobalConstantValue.BROADCAST_INFO_MAGIC_CODE_LEN);
				if ((stringMagicCode.equals(GlobalConstantValue.BROADCAST_INFO_MAGIC_CODE))) {
					loginInfoTemp = new MobileLoginInfo(receiveBuffer);
					if (TranGlobalInfo.check_is_apk_match_platform(loginInfoTemp.getPlatform_id())) {
						isDeviceValid = true;
						if (connectType == GlobalConstantValue.CONNECT_TYEP_IP_LOGIN) {
							loginInfoTemp.setmIpLoginMark(1);
						}
						loginInfoTemp.setmConnectStatus(bytesNum);
						(TranGlobalInfo.getCurDeviceInfo()).setClient_type(loginInfoTemp.getClient_type());
						isDeviceConnectFull = loginInfoTemp.getIs_current_device_connected_full();
						System.out.println("connect server getClient_type: " + loginInfoTemp.getClient_type());
					}
				} else {
					System.out.println("stringMagicCode =  " + stringMagicCode);
				}
			}
			if (isDeviceValid == false) {
				loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_DATA_TRANSMISSION_FAIL);
				cSocket.DestroySocket();
			} else if (isDeviceConnectFull == 1) // device have connected full.
			{
				loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_DEVICE_IS_FULL);
				cSocket.DestroySocket();
			}else{
				ParserFactory.setDataType(loginInfoTemp.getSend_data_type());
			}
		} catch (IllegalArgumentException e) {
			loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_IP_ERROR);
			cSocket.DestroySocket();
		} catch (SocketTimeoutException e) {
				loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_NOT_RESPONSE);
			cSocket.DestroySocket();
		} catch (ConnectException e) {
			loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_NOT_REACHABLE);
			cSocket.DestroySocket();
		} catch (SocketException e) {
			loginInfoTemp.setmConnectStatus(GlobalConstantValue.CONNECT_DEVICE_ERROR_NOT_VALID);
			cSocket.DestroySocket();
		} catch (IOException e) {
			cSocket.DestroySocket();
		} catch (Exception e) {
			cSocket.DestroySocket();
		}
		return loginInfoTemp;
	}
	
	public static void makeTextForConnectError(Context context, int errorType) {
		int resId = R.string.ConnectUnkonwnError;
		switch (errorType) {
			case GlobalConstantValue.CONNECT_DEVICE_ERROR_UNKONWN_ERROR:
				resId = R.string.ConnectUnkonwnError;
				break;
			case GlobalConstantValue.CONNECT_DEVICE_ERROR_NOT_RESPONSE:
				resId = R.string.ServerNotResponse;
				break;
			case GlobalConstantValue.CONNECT_DEVICE_ERROR_NOT_REACHABLE:
				resId = R.string.NetworkNotReachable;
				break;
			case GlobalConstantValue.CONNECT_DEVICE_ERROR_NOT_VALID:
				resId = R.string.IpNotVaild;
				break;
			case GlobalConstantValue.CONNECT_DEVICE_ERROR_IP_ERROR:
				resId = R.string.IpError;
			case GlobalConstantValue.CONNECT_DEVICE_ERROR_DATA_TRANSMISSION_FAIL:
				resId = R.string.device_connect_data_transmission_fail;
				break;
			case GlobalConstantValue.CONNECT_DEVICE_ERROR_DEVICE_IS_FULL:
				resId = R.string.str_device_is_full;
				break;
			case GlobalConstantValue.CONNECT_DEVICE_ERROR_HAND_SHARK_ERROR:
				resId = R.string.ConnectUnkonwnError;
				break;
			case GlobalConstantValue.CONNECT_DEVICE_ERROR_SERVER_IP_NON_EXIST:
				resId = R.string.server_ip_non_exist;
				break;
			default :
				return;
		}
		Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
	}
}
