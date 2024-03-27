package smart.share.dataconvert.parser;


import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import smart.share.GlobalConstantValue;
import smart.share.dataconvert.model.DataConvertCastPlayInfoModel;
import smart.share.dataconvert.model.DataConvertCastPlayModel;

public class XmlParser implements DataParser {
	private static final String TAG = "XmlParser";

	@Override
	public List<?> parse(InputStream is ,int type) throws Exception
	{
		Log.i(TAG, "[xxx] parse:type" + type);
		switch (type)
		{
			case GlobalConstantValue.CAST_PLAY_INFO: {
				List<DataConvertCastPlayInfoModel> models = null;
				DataConvertCastPlayInfoModel model = null;

				XmlPullParser parser = Xml.newPullParser();
				parser.setInput(is, "UTF-8");

				int eventType = parser.getEventType();
				while (eventType != XmlPullParser.END_DOCUMENT) {
					switch (eventType) {
						case XmlPullParser.START_DOCUMENT:
							models = new ArrayList<DataConvertCastPlayInfoModel>();
							break;
						case XmlPullParser.START_TAG:
							if (parser.getName().equals("parm")) {
								model = new DataConvertCastPlayInfoModel();
							} else if (parser.getName().equals("mediaType")) {
								eventType = parser.next();
								model.setMediaType(Integer.parseInt(parser.getText()));

							} else if (parser.getName().equals("stateCode")) {
								eventType = parser.next();
								model.setStateCode(Integer.parseInt(parser.getText()));
							} else if (parser.getName().equals("title")) {
								eventType = parser.next();
								model.setTitle(parser.getText());
							} else if (parser.getName().equals("url")) {
								eventType = parser.next();
								model.setUrl(parser.getText());
							} else if (parser.getName().equals("currentTime")) {
								eventType = parser.next();
								model.setCurrentTime(Integer.parseInt(parser.getText()));
							} else if (parser.getName().equals("totalTime")) {
								eventType = parser.next();
								model.setTotalTime(Integer.parseInt(parser.getText()));
							}
							break;
						case XmlPullParser.END_TAG:
							if (parser.getName().equals("parm")) {
								models.add(model);
								model = null;
							}
							break;
					}
					eventType = parser.next();
				}

				return models;
			}
			default :
				return null;
		}
	}

	public byte[] IntToByteArray(int index)
	{
		byte[] buff = new byte[8];
		
		for(int i = 0;i<8; i++)
		{
			buff[i] = (byte)((index>>(8*(7-i)))&0xffff);
		}
		return buff;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String serialize(List<?> models, int responseStyle) throws Exception {

		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		serializer.setOutput(writer);
		serializer.startDocument("UTF-8", true);

		serializer.startTag("", "Command");
		if(models == null)
		{
			serializer.attribute("", "request", responseStyle+"");
		}
		else{
			Object o = models.get(0);
			if(o instanceof Map)//o.getClass().getName().equals(Map.class.getName()))
			{
				List<Map<String, String>> dataMaps = (List<Map<String, String>>) models;
				serializer.attribute("", "request", responseStyle + "");
				for(Map<String, String> map: dataMaps)
				{
					Set<String> keys = map.keySet();
					for(String key: keys)
					{
						serializer.startTag("", key);
						serializer.text(map.get(key));
						serializer.endTag("", key);
					}
				}
			}else if (o instanceof DataConvertCastPlayModel) {
				if (responseStyle == GlobalConstantValue.S_MSG_CAST_DO_PLAY) {
					serializer.attribute("", "request", responseStyle + "");
					DataConvertCastPlayModel model = (DataConvertCastPlayModel) models.get(0);
					serializer.startTag("", DataConvertCastPlayModel.CAST_MEDIA_TYPE);
					serializer.text("" + model.getMediaType());
					serializer.endTag("", DataConvertCastPlayModel.CAST_MEDIA_TYPE);

					serializer.startTag("", DataConvertCastPlayModel.CAST_ACTION_CODE);
					serializer.text("" + model.getActionCode());
					serializer.endTag("", DataConvertCastPlayModel.CAST_ACTION_CODE);

					serializer.startTag("", DataConvertCastPlayModel.CAST_URL);
					serializer.text("" + model.getUrl());
					serializer.endTag("", DataConvertCastPlayModel.CAST_URL);

					serializer.startTag("", DataConvertCastPlayModel.CAST_SEEK_TIME);
					serializer.text("" + model.getSeekTime());
					serializer.endTag("", DataConvertCastPlayModel.CAST_SEEK_TIME);
				}
			}
		}

		serializer.endTag("", "Command");
		serializer.endDocument();
		return writer.toString();
	}
}
