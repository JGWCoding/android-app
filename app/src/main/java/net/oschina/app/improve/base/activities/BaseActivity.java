package net.oschina.app.improve.base.activities;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.baidu.mobstat.StatService;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import net.oschina.app.R;
import net.oschina.app.improve.base.activities.swipe.SwipeBackActivity;
import net.oschina.app.improve.main.ClipManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import butterknife.ButterKnife;

import static android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

/**
 * Created by JuQiu
 * on 16/6/20.
 * 主要有add,replace Fragment
 * 设置{@link #setStatusBarDarkMode 设置状态栏}
 */

public abstract class BaseActivity extends SwipeBackActivity {
    protected RequestManager mImageLoader;
    private boolean mIsDestroy;
    public static boolean IS_ACTIVE = true;
    private static boolean isMiUi = false;
    public static boolean hasSetStatusBarColor;//是否需要单独设置状态栏颜色
    private Fragment mFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);     //带左滑动回上一个Activity的功能
        setSwipeBackEnable(true);       //默认设置是向左滑回上一个Activity
        if (initBundle(getIntent().getExtras())) {  //默认为true
            setContentView(getContentView());   //向子类要视图
            initWindow();   //21版本以上设置状态栏为沉浸式
            ButterKnife.bind(this); //butterknife初始化
            initWidget();   //一般用于子类初始化view     不强制
            initData();     //一般用于子类初始化数据     不强制
        } else {
            finish();
        }

        StatService.setDebugOn(false);      //百度服务的开启,并设置为release版本
        //umeng analytics
//        MobclickAgent.setDebugMode(false);
//        MobclickAgent.openActivityDurationTrack(false);
//        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
    }

    protected void addFragment(int frameLayoutId, Fragment fragment) {  //添加Fragment
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (fragment.isAdded()) {
                if (mFragment != null) {
                    transaction.hide(mFragment).show(fragment);
                } else {
                    transaction.show(fragment);
                }
            } else {
                if (mFragment != null) {
                    transaction.hide(mFragment).add(frameLayoutId, fragment);
                } else {
                    transaction.add(frameLayoutId, fragment);
                }
            }
            mFragment = fragment;
            transaction.commit();
        }
    }

    @SuppressWarnings("unused")
    protected void replaceFragment(int frameLayoutId, Fragment fragment) {  //替换Fragment
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(frameLayoutId, fragment);
            transaction.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ClipManager.onResume();     //剪切板监听管理 --- 触发剪切板监听器
        StatService.onResume(this); //百度服务
//        MobclickAgent.onPageStart(this.mPackageNameUmeng);    //友盟分析
//        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatService.onPause(this);  //百度服务  ---  百度统计
//        MobclickAgent.onPageEnd(this.mPackageNameUmeng);
//        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        IS_ACTIVE = isOnForeground();
    }

    protected abstract int getContentView();

    protected boolean initBundle(Bundle bundle) {
        return true;
    }

    protected void initWindow() {   //21版本以上设置状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //View.SYSTEM_UI_FLAG_FULLSCREEN 隐藏状态栏，点击屏幕区域不会出现，需要从状态栏位置下拉才会出现。
            //SYSTEM_UI_FLAG_LAYOUT_STABLE 稳定布局，主要是在全屏和非全屏切换时，布局不要有大的变化。一般和View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN、View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION搭配使用。同时，android:fitsSystemWindows要设置为true。
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);//沉浸式状态栏 一般跟随顶部颜色
            window.setStatusBarColor(Color.TRANSPARENT);    //设置透明 配合上面配置
        }
    }

    protected void initWidget() {
    }

    protected void initData() {
    }

    public synchronized RequestManager getImageLoader() {   //得到图片加载器
        if (mImageLoader == null)
            mImageLoader = Glide.with(this);
        return mImageLoader;
    }


    @Override
    protected void onDestroy() {
        mIsDestroy = true;
        super.onDestroy();
    }

    public boolean isDestroy() {
        return mIsDestroy;
    }


    @SuppressLint("PrivateApi")
    private void setMIUIStatusBarDarkMode() {   //设置小米类手机状态栏
        if (isMiUi) {
            Class<? extends Window> clazz = getWindow().getClass();
            try {
                int darkModeFlag;
                Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                extraFlagField.invoke(getWindow(), darkModeFlag, darkModeFlag);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * 静态域，获取系统版本是否基于MIUI
     */

    static {
        try {
            @SuppressLint("PrivateApi")
            Class<?> sysClass = Class.forName("android.os.SystemProperties");
            Method getStringMethod = sysClass.getDeclaredMethod("get", String.class);
            String version = (String) getStringMethod.invoke(sysClass, "ro.miui.ui.version.name");
            isMiUi = !(version.compareTo("V9") >= 0 && Build.VERSION.SDK_INT >= 23) && version.compareTo("V6") >= 0 && Build.VERSION.SDK_INT < 24;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 设置魅族手机状态栏图标颜色风格
     * 可以用来判断是否为Flyme用户
     *
     * @param window 需要设置的窗口
     * @return boolean 成功执行返回true
     */
    @SuppressWarnings("JavaReflectionMemberAccess")
    private static boolean setMeizuDarkMode(Window window) {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= 24) {
            return false;
        }
        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                value |= bit;
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @SuppressLint("InlinedApi")
    private int getStatusBarLightMode() { //设置状态栏Light模式
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (isMiUi) {
                result = 1;
            } else if (setMeizuDarkMode(getWindow())) {
                result = 2;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 状态栏标记为浅色，然后状态栏的字体颜色自动转换为深色
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                result = 3;
            } else {
                result = 4;
            }
        }
        return result;
    }

    /**
     * 是否设置状态栏颜色 --- 默认要设置状态栏颜色
     *
     * @return return
     */
    protected boolean isSetStatusBarColor() {
        return true;
    }

    @SuppressLint("InlinedApi")
    protected void setStatusBarDarkMode() {     //设置状态栏颜色
        int type = getStatusBarLightMode();
        if (type == 1) {    //小米
            setMIUIStatusBarDarkMode();
        } else if (type == 2) { //魅族
            setMeizuDarkMode(getWindow());
        } else if (type == 3) { //23版本以上(包含23版本)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else if (type == 4) { //19 - 22 版本
            hasSetStatusBarColor = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isSetStatusBarColor()) {
                Window window = getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.status_bar_color));
            }
        }
    }

    /**
     * 是否在前台
     *
     * @return isOnForeground APP是否在前台
     */
    protected boolean isOnForeground() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();

        assert activityManager != null;
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.fontScale != 1)//非默认值
            getResources();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        if (res.getConfiguration().fontScale != 1) {    //非默认值
            Configuration newConfig = new Configuration();
            newConfig.setToDefaults();//设置默认
            res.updateConfiguration(newConfig, res.getDisplayMetrics());
        }
        return res;
    }
}
