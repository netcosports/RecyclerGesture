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
 * Created by thomas on 16/03/15.
 */
public class PageManager extends LinearLayoutManager implements RecyclerView.OnItemTouchListener {

    /**
     * Since {@link android.view.ViewConfiguration#getScaledMaximumFlingVelocity()} is in pixel per
     * second same unit should be applied when compute velociy tracker.
     */
    private static final int PIXEL_PER_SECOND_VELOCITY = 1000;

    /**
     * Should fling a least in 400 dp per seconds to trigger switch page.
     */
    private static final int MIN_FLING_VELOCITY = 400;

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

    public PageManager(Context context, RecyclerView recyclerView, boolean reverseLayout) {
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
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(PIXEL_PER_SECOND_VELOCITY, mMaximumVelocity);
                int motionVelocity = (int) VelocityTrackerCompat.getXVelocity(
                        velocityTracker, mActivePointerId);
                int page = getTargetPage(mRecyclerViewScrollX, motionVelocity);
                this.smoothScrollToPosition(mRecyclerView, null, page);
                mCurrentPage = page;
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
                PageManager.this.smoothScrollToPosition(recyclerView, null, page);
            }
        }
    }
}
