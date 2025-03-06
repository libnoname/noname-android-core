package com.noname.core.application;

import android.app.Application;

public class NonameCoreApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (NonameCoreApplication.class.getSuperclass() != Application.class) {
            throw new RuntimeException("this class is not my NonameCoreApplication");
        }
    }
}
