package smart.share.dataconvert.parser;


import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import smart.share.GlobalConstantValue;
import smart.share.dataconvert.model.DataConvertCastPlayInfoModel;
import smart.share.dataconvert.model.DataConvertCastPlayModel;
import smart.share.util.CommonHelper;


public class JsonParser implements DataParser {

	private static final String ARRAY = "array";
	private static final String TAG = "DataParser";
	@Override
	public List<?> parse(InputStream is, int type) throws Exception {
		String s = CommonHelper.getStrFromInputSteam(is);
		JSONArray jsonArray = JSON.parseArray(s);
		Log.i(TAG,"test json parse jsonArray = " + jsonArray.toString());
		switch (type) {
			case GlobalConstantValue.CAST_PLAY_INFO : {
				List<DataConvertCastPlayInfoModel> models = new ArrayList<>();
				DataConvertCastPlayInfoModel model;
				for (int i = 0; i < jsonArray.size(); i++) {
					model = new DataConvertCastPlayInfoModel();
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					String mediaType = jsonObject.getString("mediaType");
					String stateCode = jsonObject.getString("stateCode");
					String title = jsonObject.getString("title");
					String url = jsonObject.getString("url");
					String currentTime = jsonObject.getString("currentTime");
					String totalTime = jsonObject.getString("totalTime");
					if (!TextUtils.isEmpty(mediaType)) {
						model.setMediaType(Integer.parseInt(mediaType));
					}
					if (!TextUtils.isEmpty(stateCode)) {
						model.setStateCode(Integer.parseInt(stateCode));
					}
					model.setTitle(title);
					model.setUrl(url);
					if (!TextUtils.isEmpty(currentTime)) {
						model.setCurrentTime(Integer.parseInt(currentTime));
					}
					if (!TextUtils.isEmpty(totalTime)) {
						model.setTotalTime(Integer.parseInt(totalTime));
					}
					models.add(model);

				}
				return models;
			}

			default :
				return null;
		}
	}
	
	@Override
	public String serialize(List<?> models, int responseStyle) throws Exception {
		JSONObject jsonObject = new JSONObject(true);
		JSONArray jsonArray = new JSONArray();
		if (models == null) {
			jsonObject.put("request", responseStyle + "");
		} else {
			jsonObject.put("request", responseStyle + "");
			Object o = models.get(0);
			if (o instanceof DataConvertCastPlayModel) {
				if (responseStyle == GlobalConstantValue.S_MSG_CAST_DO_PLAY) {
					DataConvertCastPlayModel model = (DataConvertCastPlayModel)models.get(0);
					jsonObject.put(DataConvertCastPlayModel.CAST_ACTION_CODE, model.getActionCode());
					jsonObject.put(DataConvertCastPlayModel.CAST_MEDIA_TYPE, model.getMediaType());
					jsonObject.put(DataConvertCastPlayModel.CAST_URL, model.getUrl());
					jsonObject.put(DataConvertCastPlayModel.CAST_SEEK_TIME, model.getSeekTime());
				}
			}
		}
		Log.i(TAG,"test json serialize jsonObject = " + jsonObject.toString());
		return jsonObject.toString();
	}
}
