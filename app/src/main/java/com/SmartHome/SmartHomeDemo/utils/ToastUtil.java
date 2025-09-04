package com.SmartHome.SmartHomeDemo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class ToastUtil {
    private static final String PREFS_NAME = "settings";
    private static final String NOTICE_ENABLED_KEY = "notice_enabled";

    public static void showToast(Context context, String message, int duration) {
        // 检查通知是否启用
        if (isNoticeEnabled(context)) {
            Toast.makeText(context, message, duration).show();
        }
    }

    public static void showToast(Context context, int resId, int duration) {
        // 检查通知是否启用
        if (isNoticeEnabled(context)) {
            Toast.makeText(context, resId, duration).show();
        }
    }

    private static boolean isNoticeEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // 默认情况下通知是启用的
        return prefs.getBoolean(NOTICE_ENABLED_KEY, true);
    }
}
