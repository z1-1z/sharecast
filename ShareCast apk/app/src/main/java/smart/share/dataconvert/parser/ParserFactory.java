package smart.share.dataconvert.parser;

import android.util.Log;

public class ParserFactory {
	private static final int	XML_DATA	= 0;
	private static final int	JSON_DATA	= 1;
	private static int			mDataType	= XML_DATA;
	
	private static DataParser	XmlParser	= null;
	private static DataParser	JsonParser	= null;
	
	public static DataParser getParser() {
		Log.d("benson", "mDataType = "+mDataType);
		switch (mDataType) {
			case JSON_DATA :
				if (JsonParser == null) {
					JsonParser = new JsonParser();
				}
				return JsonParser;
			case XML_DATA :
			default :
				if (XmlParser == null) {
					XmlParser = new XmlParser();
				}
				return XmlParser;
		}
	}
	public static int getDataType() {
		return mDataType;
	}
	public static void setDataType(int mDataType) {
		ParserFactory.mDataType = mDataType;
	}
	
}
