-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip

-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.common.internal.DoNotStrip *;
}

-keepclassmembers class * {
    native <methods>;
}

-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn com.android.volley.toolbox.**

-dontwarn com.squareup.okhttp.**
-dontwarn okhttp3.**



-dontwarn  com.bumptech.**
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

-keepattributes Signature

-keepattributes *Annotation*

-keepattributes EnclosingMethod

-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.**{*;}
-keep class com.google.gson.examples.android.model.** { *; }

-dontwarn com.google.zxing.**
-dontwarn com.google.zxing.client.android.**
-keep  class com.google.zxing.**{*;}

-keepclassmembers class ** {
    public void onEvent*(***);
}

-keepclassmembers class * extends de.greenrobot.event.util.ThrowableFailureEvent {
    public <init>(java.lang.Throwable);
}

-dontwarn de.greenrobot.event.util.*$Support
-dontwarn de.greenrobot.event.util.*$SupportManagerFragment

-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}


-dontwarn sun.misc.**
-dontwarn org.apache.http.**

-keep class rx.** { *; }

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

-dontwarn okhttp3.**
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okio.**
-keep class okio.** {*;}



-dontnote retrofit2.Platform
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
-dontwarn retrofit2.Platform$Java8
-keepattributes Signature
-keepattributes Exceptions

-dontwarn retrofit2.adapter.**

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}



-keep class com.j256.ormlite.** {*;}

-keep class com.dodola.rocoofix.** {*;}
-keep class com.lody.legend.** {*;}


-dontwarn com.google.common.**
-keep  class net.sqlcipher.** {*;}
-keep  class net.sqlcipher.database.** {*;}



-keep class com.qiniu.** {*;}
-dontwarn com.qiniu.**


-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}

-dontwarn org.apache.http.**

-keep class org.apache.http.** { *;}

-keep class com.umeng.analytics.** {*;}
-dontwarn com.umeng.analytics.**



-keep class com.amap.api.** {*;}
-keep class com.autonavi.** {*;}
-keep class com.a.a.** {*;}
-keep class com.loc.** {*;}
-dontwarn com.amap.api.**
-dontwarn com.autonavi.**
-dontwarn com.a.a.**
-dontwarn com.loc.**


-keep class com.baidu.** {*;}
-keep class vi.com.** {*;}
-dontwarn com.baidu.**


-dontwarn net.soureceforge.pinyin4j.**
-dontwarn demo.**
-keep class net.sourceforge.pinyin4j.** { *;}
-keep class demo.** { *;}
-keep class com.hp.** { *;}

-dontwarn android.net.compatibility.**
-dontwarn android.net.http.**
-dontwarn com.android.internal.http.multipart.**
-dontwarn org.apache.commons.**
-dontwarn org.apache.http.**
-dontwarn org.apache.http.protocol.**
-keep class android.net.compatibility.**{*;}
-keep class android.net.http.**{*;}
-keep class com.android.internal.http.multipart.**{*;}
-keep class org.apache.commons.**{*;}
-keep class org.apache.org.**{*;}
-keep class org.apache.harmony.**{*;}

-keep class org.greenrobot.greendao.**{*;}
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
-keep class **$Properties

-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

-keep class com.umeng.** {*;}
-keep class com.ta.** {*;}
-keep class com.ut.** {*;}
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class tv.danmaku.ijk.media.player.**{*; }

-keep class com.sun.**{*; }
-keep class javax.activation.**{*; }

-keep class myjava.**{*; }
-keep class org.apache.**{*; }

-keep class org.teleal.**{*; }

-keep class org.google.zxing.**{*; }

-keep class master.flame.**{*; }
-keep class tv.cjump.jni.**{*; }

-keep class com.alibaba.fastjson.**{*; }

-keep class com.sum.mail.**{*; }
-keep class javax.mail.**{*; }

-keep class org.apache.mina.**{*; }

-keep class com.hisilicon.**{*; }
-keep class com.huawei.**{*; }
-keep class org.cybergarage.**{*; }

-keep class com.iflytek.**{*; }

-keep class com.baoyz.**{*; }

-keep class software.amazon.**{*; }
-keep class com.amazonaws.**{*; }

-keep class com.jaeger.library.**{*; }
-keep class com.excellence.basetoolslibrary.**{*; }

