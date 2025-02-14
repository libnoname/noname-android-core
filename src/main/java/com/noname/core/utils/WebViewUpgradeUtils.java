package com.noname.core.utils;

import static com.noname.core.activities.WebViewSelectionActivity.SELECTED_WEBVIEW_PACKAGE;
import static com.noname.core.activities.WebViewSelectionActivity.WEBVIEW_PACKAGES;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;

import com.noname.core.activities.WebViewSelectionActivity;
import com.noname.core.interfaces.WebViewUpgradeInterface;
import com.norman.webviewup.lib.UpgradeCallback;
import com.norman.webviewup.lib.WebViewUpgrade;
import com.norman.webviewup.lib.source.UpgradePackageSource;
import com.norman.webviewup.lib.source.UpgradeSource;

import java.util.concurrent.Executors;

import cn.hle.skipselfstartmanager.util.MobileInfoUtils;

public class WebViewUpgradeUtils implements WebViewUpgradeInterface {

    private static final String TAG = "WebViewUpgradeUtils";

    private ProgressDialog WebViewUpgradeProgressDialog;

    public static boolean WebviewUpgraded = false;

    private Context context;

    private Activity activity;

    @Override
    public void upgrade(Context context, Runnable callback) {
        this.context = context;
        this.activity = (Activity) context;

        if (SELECTED_WEBVIEW_PACKAGE == null) {
            SELECTED_WEBVIEW_PACKAGE = context.getSharedPreferences("nonameyuri", Context.MODE_PRIVATE)
                    .getString("selectedWebviewPackage", WEBVIEW_PACKAGES[0]);
        }

        boolean useUpgrade = context.getSharedPreferences("nonameyuri", Context.MODE_PRIVATE).getBoolean("useUpgrade", true);

        if (!useUpgrade || WebviewUpgraded) {
            finish(callback);
        }
        else {
            WebviewUpgraded = true;

            if (WebViewUpgradeProgressDialog == null) {
                WebViewUpgradeProgressDialog = new ProgressDialog(context);
                WebViewUpgradeProgressDialog.setTitle("正在更新Webview内核");
                WebViewUpgradeProgressDialog.setCancelable(false);
                WebViewUpgradeProgressDialog.setIndeterminate(false);
                WebViewUpgradeProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                WebViewUpgradeProgressDialog.setMax(100);
                WebViewUpgradeProgressDialog.setProgress(0);
                if (WebViewUpgradeProgressDialog.isShowing()) WebViewUpgradeProgressDialog.hide();
            }

            WebViewUpgrade.addUpgradeCallback(new UpgradeCallback() {
                @Override
                public void onUpgradeProcess(float percent) {
                    if (percent <= 0.9 && !WebViewUpgradeProgressDialog.isShowing()) {
                        WebViewUpgradeProgressDialog.show();
                    }
                    WebViewUpgradeProgressDialog.setProgress((int) (percent * 100));
                }

                @Override
                public void onUpgradeComplete() {
                    Log.e(TAG, "onUpgradeComplete");
                    WebViewUpgradeProgressDialog.setProgress(100);

                    try {
                        PackageInfo upgradePackageInfo = context.getPackageManager().getPackageInfo(SELECTED_WEBVIEW_PACKAGE, 0);
                        if (upgradePackageInfo != null) {

                            if (Build.VERSION.SDK_INT > 34) {
                                String serviceName =  "org.chromium.content.app.SandboxedProcessService0";

                                ServiceConnection mConnection = new ServiceConnection() {
                                    @Override
                                    public void onServiceConnected(ComponentName className, IBinder service) {
                                        Log.e(TAG, serviceName + "服务连接成功");
                                    }

                                    @Override
                                    public void onServiceDisconnected(ComponentName arg0) {
                                        Log.e(TAG, serviceName + "服务意外断开");
                                    }
                                };

                                try {
                                    Intent intent = new Intent();
                                    intent.setClassName(SELECTED_WEBVIEW_PACKAGE, serviceName);
                                    boolean isServiceBound = activity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

                                    if (isServiceBound) {
                                        Log.e(TAG, serviceName + "服务已启动并且绑定成功");
                                    }
                                    else {
                                        Log.e(TAG, serviceName + "是服务未启动或不存在");
                                        navigateToAppSettingsAndExit();
                                    }
                                } catch (java.lang.SecurityException e) {
                                    e.printStackTrace();
                                    Log.e(TAG, serviceName + "服务已启动");
                                }
                            }

                            finish(callback);
                        } else {
                            finish(callback);
                        }
                    } catch (Exception e) {
                        finish(callback);
                    }
                }

                @Override
                public void onUpgradeError(Throwable throwable) {
                    Log.e(TAG, "onUpgradeError: " + throwable);
                    android.app.AlertDialog.Builder dlg = new android.app.AlertDialog.Builder(context);
                    dlg.setMessage("Webview内核升级失败，是否设置其它Webview实现？(" + throwable.getMessage() + ")");
                    dlg.setTitle("Alert");
                    dlg.setCancelable(false);
                    dlg.setPositiveButton("立即设置",
                            (dialog1, which1) -> {
                                changeWebviewProvider();
                                finish(callback);
                            });
                    dlg.setNegativeButton("暂时不设置",
                            (dialog3, which2) -> {
                                dialog3.dismiss();
                                finish(callback);
                            });
                    dlg.setOnKeyListener((dialog2, keyCode, event) -> {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            finish(callback);
                            return false;
                        }
                        else {
                            changeWebviewProvider();
                            finish(callback);
                            return true;
                        }
                    });
                    dlg.show();
                }
            });

            try {
                // 添加webview
                UpgradeSource upgradeSource = new UpgradePackageSource(
                        context.getApplicationContext(),
                        SELECTED_WEBVIEW_PACKAGE
                );

                String SystemWebViewPackageName = WebViewUpgrade.getSystemWebViewPackageName();
                Log.e(TAG, "SystemWebViewPackageName: " + SystemWebViewPackageName);
                Log.e(TAG, "SelectedWebviewPackage: " + SELECTED_WEBVIEW_PACKAGE);

                // 如果webview就是已经选的
                if (SELECTED_WEBVIEW_PACKAGE.equals(SystemWebViewPackageName)) {
                    finish(callback);
                    return;
                }

                WebViewUpgrade.upgrade(upgradeSource);
            } catch (Exception e) {
                Log.e(TAG, String.valueOf(e));
                finish(callback);
            }
        }
    }

    @Override
    public void navigateToAppSettingsAndExit() {
        android.app.AlertDialog.Builder dlg = new android.app.AlertDialog.Builder(context);
        dlg.setMessage("请授予Webview(" + SELECTED_WEBVIEW_PACKAGE + ")自启动权限后重新进入APP，否则本App将无法正常使用Webview组件！");
        dlg.setTitle("Alert");
        dlg.setCancelable(false);
        dlg.setPositiveButton("立即设置",
                (dialog1, which1) -> {
                    MobileInfoUtils.jumpStartInterface(context);
                    activity.finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                });
        dlg.setNegativeButton("暂时不设置",
                (dialog3, which2) -> dialog3.dismiss());
        dlg.setOnKeyListener((dialog2, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                return false;
            }
            else {
                MobileInfoUtils.jumpStartInterface(context);
                activity.finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                return true;
            }
        });
        dlg.show();
    }

    @Override
    public void changeWebviewProvider() {
        Intent newIntent = new Intent(context, WebViewSelectionActivity.class);
        newIntent.setAction(Intent.ACTION_VIEW);
        activity.startActivity(newIntent);
    }
    
    private void finish(Runnable callback) {
        if (WebViewUpgradeProgressDialog != null) {
            WebViewUpgradeProgressDialog.hide();
            WebViewUpgradeProgressDialog.dismiss();
            WebViewUpgradeProgressDialog = null;
        }

        CheckUtils.check(context, Executors.newFixedThreadPool(5));

        callback.run();
    }
}
