package com.netcosports.recyclergesture;

import android.content.Context;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Layout manager which allow to turn a recycler view into an horizontal pager.
 * <p/>
 * Mimic motion of a view pager.
 */
public class HorizontalPagerLayoutManager extends LinearLayoutManager implements RecyclerView.OnItemTouchListener {

    /**
     * Since {@link android.view.ViewConfiguration#getScaledMaximumFlingVelocity()} is in pixel per
     * second same unit should be applied when compute velocity tracker.
     */
    private static final int PIXEL_PER_SECOND_VELOCITY = 1000;

    /**
     * Should fling a least in 400 dp per seconds to trigger switch page.
     */
    private static final int MIN_FLING_VELOCITY = 400;

    /**
     * Dummy listener.
     */
    private static OnPageChangeListener sDummyListener = new OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {

        }
    };

    /**
     * Current listener callbacks.
     */
    private OnPageChangeListener mOnPageChangeListener = sDummyListener;

    /**
     * RecyclerView which holds the Layout manager.
     */
    private RecyclerView mRecyclerView;

    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;

    /**
     * Max velocity allowed.
     */
    private int mMaximumVelocity;

    /**
     * Velocity boundary to trigger
     */
    private int mMinimumVelocityForSwipe;

    /**
     * Active pointer id.
     */
    private int mActivePointerId;

    /**
     * Current page index.
     */
    private int mCurrentPage;

    /**
     * True if pager motion should be enable.
     */
    private boolean mPagerMotionEnable;

    /**
     * X coordinate at down event.
     */
    private float mXAtDown;

    public HorizontalPagerLayoutManager(Context context, RecyclerView recyclerView, boolean reverseLayout) {
        super(context, LinearLayoutManager.HORIZONTAL, reverseLayout);

        mRecyclerView = recyclerView;
        mRecyclerView.addOnItemTouchListener(this);
        mRecyclerView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);

        ViewConfiguration configuration = ViewConfiguration.get(context);
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        final float density = context.getResources().getDisplayMetrics().density;
        mMinimumVelocityForSwipe = (int) (MIN_FLING_VELOCITY * density);

        mCurrentPage = 0;
        mPagerMotionEnable = true;
    }

    @Override
    public void measureChild(View child, int widthUsed, int heightUsed) {
        child.measure(View.MeasureSpec.makeMeasureSpec(mRecyclerView.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(mRecyclerView.getHeight(), View.MeasureSpec.EXACTLY));
    }

    @Override
    public void measureChildWithMargins(View child, int widthUsed, int heightUsed) {
        super.measureChildWithMargins(child, widthUsed, heightUsed);
        
        child.measure(View.MeasureSpec.makeMeasureSpec(
                        mRecyclerView.getWidth() - getDecoratedMeasuredWidth(child)
                                + child.getMeasuredWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(
                        mRecyclerView.getHeight() - getDecoratedMeasuredHeight(child)
                                + child.getMeasuredHeight(), View.MeasureSpec.EXACTLY));
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent ev) {
        if (!mPagerMotionEnable) {
            return false;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                mXAtDown = ev.getX();
                break;
            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(PIXEL_PER_SECOND_VELOCITY, mMaximumVelocity);
                int motionVelocity = (int) VelocityTrackerCompat.getXVelocity(
                        velocityTracker, mActivePointerId);

                float scrollX = mXAtDown - ev.getX();
                final int page = getTargetPage(scrollX, motionVelocity);
                mCurrentPage = page;
                mOnPageChangeListener.onPageSelected(mCurrentPage);

                if (mPagerMotionEnable) {
                    // since ViewFlinger posted on animation isn't runned when scrolling motion
                    // is smooth and not flinged, delay the smoothScrollRequest.
                    mRecyclerView.postDelayed(new SmoothScroller(page), 1);
                }
            case MotionEvent.ACTION_CANCEL:
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent ev) {
    }

    /**
     * Enable / disable pager.
     *
     * @param enable true to enable the pager motion.
     */
    public void enablePager(boolean enable) {
        mPagerMotionEnable = enable;
    }

    /**
     * Set a listener used to catch page change events.
     *
     * @param listener listener which will be called during page motion.
     */
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        if (listener == null) {
            mOnPageChangeListener = sDummyListener;
        } else {
            mOnPageChangeListener = listener;
        }
    }

    private int getTargetPage(float scrollX, int velocity) {
        int direction = 1;
        int targetPage = mCurrentPage;

        if (scrollX < 0) {
            if (mCurrentPage == 0) {
                return mCurrentPage;
            } else {
                direction = -1;
            }
        } else if (scrollX > 0) {
            if (mCurrentPage == getItemCount() - 1) {
                return mCurrentPage;
            } else {
                direction = 1;
            }
        }

        if (Math.abs(velocity) > mMinimumVelocityForSwipe
                || Math.abs(scrollX) > mRecyclerView.getWidth() / 2) {
            targetPage += direction;
        }
        return targetPage;
    }

    /**
     * Listener used to catch page event.
     */
    public interface OnPageChangeListener {
        /**
         * This method will be invoked when a new page becomes selected. Animation is not
         * necessarily complete.
         *
         * @param position Position index of the new selected page.
         */
        public void onPageSelected(int position);
    }

    /**
     * Scroller used to perform smooth scroll to a given position.
     */
    private class SmoothScroller implements Runnable {

        private int mPosition;

        public SmoothScroller(int position) {
            mPosition = position;
        }

        @Override
        public void run() {
            HorizontalPagerLayoutManager.this.smoothScrollToPosition(mRecyclerView, null, mPosition);
        }
    }
}
