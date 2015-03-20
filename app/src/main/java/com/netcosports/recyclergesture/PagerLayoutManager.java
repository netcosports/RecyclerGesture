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
 * Layout manager which allow to turn a recycler view into a pager.
 * <p/>
 * Mimic motion of a view pager.
 */
public class PagerLayoutManager extends LinearLayoutManager implements RecyclerView.OnItemTouchListener {

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
     * Used to keep recycler view scroll x offset.
     */
    private int mRecyclerViewScrollX = 0;

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

    public PagerLayoutManager(Context context, RecyclerView recyclerView, boolean reverseLayout) {
        super(context, LinearLayoutManager.HORIZONTAL, reverseLayout);

        mRecyclerView = recyclerView;
        mRecyclerView.addOnItemTouchListener(this);
        mRecyclerView.setOnScrollListener(new PageScrollListener());
        mRecyclerView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);

        ViewConfiguration configuration = ViewConfiguration.get(context);
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        final float density = context.getResources().getDisplayMetrics().density;
        mMinimumVelocityForSwipe = (int) (MIN_FLING_VELOCITY * density);

        mCurrentPage = 0;
        mPagerMotionEnable = true;
    }

    @Override
    public int findFirstVisibleItemPosition() {
        return super.findFirstVisibleItemPosition();
    }

    @Override
    public void measureChild(View child, int widthUsed, int heightUsed) {
        child.measure(View.MeasureSpec.makeMeasureSpec(mRecyclerView.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(mRecyclerView.getHeight(), View.MeasureSpec.EXACTLY));
    }

    @Override
    public void measureChildWithMargins(View child, int widthUsed, int heightUsed) {
        child.measure(View.MeasureSpec.makeMeasureSpec(mRecyclerView.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(mRecyclerView.getHeight(), View.MeasureSpec.EXACTLY));
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                break;
            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(PIXEL_PER_SECOND_VELOCITY, mMaximumVelocity);
                int motionVelocity = (int) VelocityTrackerCompat.getXVelocity(
                        velocityTracker, mActivePointerId);
                int page = getTargetPage(mRecyclerViewScrollX, motionVelocity);
                mCurrentPage = page;
                mOnPageChangeListener.onPageSelected(mCurrentPage);

                if (mPagerMotionEnable) {
                    this.smoothScrollToPosition(mRecyclerView, null, page);
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

    private int getTargetPage(int scrollX, int velocity) {
        if (velocity > mMinimumVelocityForSwipe) {

        }
        int pageWidth = mRecyclerView.getWidth();
        int targetPage = Math.round((float) scrollX / (float) pageWidth);

        if (targetPage == mCurrentPage && Math.abs(velocity) > mMinimumVelocityForSwipe) {
            if (scrollX > mCurrentPage * pageWidth) {
                targetPage++;
            } else if (mCurrentPage > 0) {
                targetPage--;
            }
        }
        return targetPage;
    }

    private class PageScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            mRecyclerViewScrollX += dx;
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                int page = getTargetPage(mRecyclerViewScrollX, 0);
                if (mPagerMotionEnable) {
                    PagerLayoutManager.this.smoothScrollToPosition(recyclerView, null, page);
                }
            }
        }
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
}
