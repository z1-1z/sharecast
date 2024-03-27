package com.intelligent.share.tool;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

/**
 * @author xxx
 * @date 2024/2/4
 */
public class GlideUtil {
   private static final String TAG = "GlideUtil";
   public static void load(Context context, String url, RequestOptions options,
                    DiskCacheStrategy strategy, int placeholderId, int errorId, ImageView imageView){
      if (context instanceof Activity) {
         if (((Activity) context).isFinishing() || ((Activity) context).isDestroyed()) {
            return;
         }
      }
      RequestBuilder<Drawable> requestBuilder = Glide.with(context).load(url)
              .diskCacheStrategy(strategy);
      if (placeholderId != -1) {
         requestBuilder = requestBuilder.placeholder(placeholderId);
      }
      if (errorId != -1) {
         requestBuilder = requestBuilder.error(errorId);
      }
      if (options != null) {
         requestBuilder = requestBuilder.apply(options);
      }
      requestBuilder.into(imageView);
   }

}
