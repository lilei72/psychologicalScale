package com.example.android.questionnaire;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
    public static Context getContext(){
        return getContext();
    }
}