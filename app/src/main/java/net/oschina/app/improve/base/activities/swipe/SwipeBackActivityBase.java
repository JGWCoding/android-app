package net.oschina.app.improve.base.activities.swipe;
/**
 * @author Yrom
 */
 interface SwipeBackActivityBase {
    /**
     * @return the SwipeBackLayout associated with this activity.
     */
    SwipeBackLayout getSwipeBackLayout();

    void setSwipeBackEnable(boolean enable); //设置支持可以滑动到上一页面

    /**
     * Scroll out contentView and finish the activity
     */
    void scrollToFinishActivity();//滑动并关闭activity

}
