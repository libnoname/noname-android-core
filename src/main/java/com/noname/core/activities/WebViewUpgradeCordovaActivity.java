package com.noname.core.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.noname.core.utils.CheckUtils;
import com.noname.core.NonameJavaScriptInterface;
import com.noname.core.utils.WebViewUpgradeUtils;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.LOG;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;

public class WebViewUpgradeCordovaActivity extends CordovaActivity {

    protected WebView webview;

    protected void ActivityOnCreate(Bundle extras) {
        try {
            if (extras != null && extras.getString("importExtensionName") != null) {
                String extName = extras.getString("importExtensionName");
                URI uri = new URI(launchUrl);
                String newQuery = uri.getQuery();
                String appendQuery = "importExtensionName=" + extName;
                if (newQuery == null) {
                    newQuery = appendQuery;
                } else {
                    newQuery += "&" + appendQuery;
                }
                URI newUri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), newQuery, uri.getFragment());
                Log.e(TAG, newUri.toString());
                loadUrl(newUri.toString());
            }
            else {
                loadUrl(launchUrl);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            // Set by <content src="index.html" /> in config.xml
            loadUrl(launchUrl);
        }

        View view = appView.getView();
        webview = (WebView) view;
        WebSettings settings = webview.getSettings();
        Log.e(TAG, settings.getUserAgentString());
        initWebViewSettings(webview, settings);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LOG.e("onCreate", String.valueOf(savedInstanceState));
        super.onCreate(savedInstanceState);

        // enable Cordova apps to be started in the background
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
            moveTaskToBack(true);
        }

        WebViewUpgradeUtils webViewUpgradeUtils = new WebViewUpgradeUtils();
        webViewUpgradeUtils.upgrade(this, new Thread(() -> {
            ActivityOnCreate(extras);
        }));
    }

    protected void initWebViewSettings(WebView webview, WebSettings settings) {
        settings.setTextZoom(100);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webview.addJavascriptInterface(new NonameJavaScriptInterface(this, webview, preferences), "NonameAndroidBridge");
        WebView.setWebContentsDebuggingEnabled(true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LOG.e("onNewIntent" ,"111");
        super.onNewIntent(intent);
        setIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String extName = extras.getString("importExtensionName");
            boolean importPackage = extras.getBoolean("importPackage", false);
            if (webview != null) {
                if (extName != null) {
                    webview.evaluateJavascript("(() => {" +
                            "const event = new CustomEvent('importExtension', { " +
                            "detail: { extensionName: '" + extName + "'}" +
                            "});" +
                            "window.dispatchEvent(event);" +
                            "})();", null);
                }
                if (importPackage) {
                    webview.evaluateJavascript("(() => {" +
                            "const event = new CustomEvent('importPackage', { " +
                            "detail: { importPackage: true }" +
                            "});" +
                            "window.dispatchEvent(event);" +
                            "})();", null);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        View rootview = getWindow().getDecorView();
        View focusView = rootview.findFocus();
        Log.e(TAG, String.valueOf(focusView));
        if (webview != null && focusView == webview && webview.canGoBack()) {
            webview.goBack();
            Log.e(TAG, "SystemWebView -> " + webview.getUrl());
        }
        else {
            super.onBackPressed();
        }
    }
}
