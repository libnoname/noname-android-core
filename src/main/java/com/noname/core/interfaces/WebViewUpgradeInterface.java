package com.noname.core.interfaces;

import android.content.Context;

public interface WebViewUpgradeInterface {
    void upgrade(Context context, Runnable callback);

    void navigateToAppSettingsAndExit();

    void changeWebviewProvider();
}
