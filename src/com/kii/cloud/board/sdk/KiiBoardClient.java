package com.kii.cloud.board.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.kii.cloud.storage.KiiClient;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;

public class KiiBoardClient {
    public static final String CONTAINER_TOPIC = "topic";
    public static final String CONTAINER_MESSAGE = "post";

    private static KiiBoardClient mClient;

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String CURRENT_USER = "current_user";
    private static final String USER_PASSWORD = "password";
    private static final String UPDATE_TIME = "update_time";
    private static final String EMAIL = "email";

    private static final String DEFAULT_BASE_URL = "http://api.kii.com:12110";
    private static final String APP_ID = "20df2cb0";
    private static final String APP_KEY = "18cb6a798b6367844ee96e55f3b71f86";

    public static KiiBoardClient getInstance() {
        if (mClient == null) {
            mClient = new KiiBoardClient();
        }

        return mClient;
    }

    private KiiBoardClient() {
        KiiClient.initialize(APP_ID, APP_KEY, DEFAULT_BASE_URL);
    }

    public static void setLoginUserName(Context context, String username,
            String password) {
        SharedPreferences settings = context
                .getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(CURRENT_USER, username);
        editor.putString(USER_PASSWORD, password);
        editor.commit();

    }

    public static void setUpdateTime(Context context, long time) {
        SharedPreferences settings = context
                .getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(UPDATE_TIME, time);
        editor.commit();

    }

    public static long getUpdateTime(Context context) {
        SharedPreferences settings = context
                .getSharedPreferences(PREFS_NAME, 0);
        long time = settings.getLong(UPDATE_TIME, 0);
        return time;

    }

    public static String getLoginUserName(Context context) {
        SharedPreferences settings = context
                .getSharedPreferences(PREFS_NAME, 0);
        String name = settings.getString(CURRENT_USER, "");
        return name;
    }

    public static String getLoginUserPassword(Context context) {
        SharedPreferences settings = context
                .getSharedPreferences(PREFS_NAME, 0);
        String name = settings.getString(USER_PASSWORD, "");
        return name;
    }

    public static void setLoginEmail(Context context, String email) {
        SharedPreferences settings = context
                .getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(EMAIL, email);
        editor.commit();
    }

    public KiiUser getloginUser() {
        return KiiClient.getCurrentUser();
    }

    public static KiiObject getKiiObjectByUuid(String container, String uuid) {
        Uri uri = Uri.fromParts(KII_URI, container, uuid);
        return new KiiObject(uri);
    }
    
    public static final String KII_URI = "kiicloud://";

}
