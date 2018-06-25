
package net.oschina.app.improve.base.activities.swipe;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 *  向右滑动关闭本页面,显现上一个页面 仿微信右滑跳上一页面
 */
public class SwipeBackActivity extends AppCompatActivity implements SwipeBackActivityBase {
    private SwipeBackActivityHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new SwipeBackActivityHelper(this);
        mHelper.onActivityCreate();    //
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {    //Activity彻底运行起来了  视图创建成功调用
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate();
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v == null && mHelper != null)
            return mHelper.findViewById(id);
        return v;
    }

    @Override
    public SwipeBackLayout getSwipeBackLayout() {
        return mHelper.getSwipeBackLayout();
    }

    @Override
    public void setSwipeBackEnable(boolean enable) {
        getSwipeBackLayout().setEnableGesture(enable);  //设置手势
    }

    @Override
    public void scrollToFinishActivity() {
        Utils.convertActivityToTranslucent(this);   //转换到半透明
        getSwipeBackLayout().scrollToFinishActivity();
    }
}
