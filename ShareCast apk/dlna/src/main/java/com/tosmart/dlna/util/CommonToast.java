package com.tosmart.dlna.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tosmart.dlna.R;
import com.tosmart.dlna.application.BaseApplication;

/**
 * Created by xxx on 2018/4/12.
 */

public class CommonToast
{
	private static CommonToast	sInstance;
	private Toast mToast;
	private TextView mTextView;

	private CommonToast(Context context)
	{
		View view = LayoutInflater.from(context).inflate(R.layout.toast_layout, null);
		mTextView = view.findViewById(R.id.toast_textView);
		mToast = new Toast(context);
		mToast.setView(view);
		mToast.setDuration(Toast.LENGTH_SHORT);
	}

	public synchronized static CommonToast obtain()
	{
		if (sInstance == null)
		{
			sInstance = new CommonToast(BaseApplication.getContext());
		}
		return sInstance;
	}

	public void setGravity(int position)
	{
		mToast.setGravity(position, 0, 0);
	}

	public void show(String contentStr)
	{
		mTextView.setText(contentStr);
		mToast.show();
	}

	public void show(int resId)
	{
		if (resId > 0) {
			mTextView.setText(resId);
			mToast.show();
		}
	}

	public void CancelToast()
	{
		mToast.cancel();
	}
}
