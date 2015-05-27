package com.netcosports.recyclergesture.library.swipe;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

/**
 * Simple {@link android.support.v7.widget.RecyclerView.OnItemTouchListener}
 * that allows items to be swiped and dismissed.
 */
class SwipeToDismissListener implements RecyclerView.OnItemTouchListener {

    private final RecyclerView mRecyclerView;
    private int mSlop;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;
    private long mAnimationTime;

    private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero

    // Transient properties
    private float mDownX;
    private float mDownY;
    private boolean mSwiping;
    private int mSwipingSlop;
    private VelocityTracker mVelocityTracker;
    private float mTranslationX;
    private boolean mEnable = false;
    private boolean mIsDismissing = true;
    private View mSwipeView;
    private SwipeToDismissDirection mAllowedSwipeToDismissDirection = SwipeToDismissDirection.NONE;
    private SwipeToDismissStrategy mDismissStrategy;
    private SwipeToDismissGesture.Dismisser mDismisser;

    // Fake background only used when the recycler background is transparent.
    private int mBackgroundColor; // color used when recycler background is transparent.
    private View mBackground; // background used when recycler background is transparent.
    private Runnable mDelayedBackgroundDismiss;
    private boolean mIsRemoving;


    /**
     * Constructs a new swipe-to-dismiss OnItemTouchListener for RecyclerView
     *
     * @param recyclerView    RecyclerView
     * @param direction       swipe direction.
     * @param dismisser       Dismisser used to process to the dismiss when dismiss motion is triggered.
     * @param strategy        strategy applied for dismiss motion, if null all items will follow the main policy.
     * @param backgroundColor color displayed under swiped view when recycler background is transparent.
     *                        -1 if none should be used.
     */
    public SwipeToDismissListener(RecyclerView recyclerView, SwipeToDismissDirection direction,
                                  SwipeToDismissStrategy strategy, SwipeToDismissGesture.Dismisser dismisser,
                                  int backgroundColor) {

        ViewConfiguration vc = ViewConfiguration.get(recyclerView.getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 4;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = recyclerView.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
        mRecyclerView = recyclerView;
        mDismisser = dismisser;
        if (strategy == null) {
            mDismissStrategy = new SwipeToDismissStrategy();
        } else {
            mDismissStrategy = strategy;
        }
        mDismissStrategy.setDefaultSwipeToDismissDirection(direction);
        mBackgroundColor = backgroundColor;
        mBackground = null;
        mIsDismissing = false;
        mIsRemoving = false;
        mDelayedBackgroundDismiss = new Runnable() {
            @Override
            public void run() {
                resetBackground();
            }
        };
    }

    /**
     * Enable / disable dismiss.
     *
     * @param enabled true to enable swipe to dismiss motion.
     */
    public void setEnabled(boolean enabled) {
        mEnable = !enabled;
    }

    @Override
    public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
        motionEvent.offsetLocation(mTranslationX, 0);

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_UP: {
                up(motionEvent);
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                cancel();
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                move(motionEvent);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(final RecyclerView view, MotionEvent motionEvent) {
        if (mEnable) {
            return false;
        }
        // offset because the view is translated during swipe
        motionEvent.offsetLocation(mTranslationX, 0);

        if (mViewWidth < 2) {
            mViewWidth = view.getWidth();
        }

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                return down(motionEvent);
            }
            case MotionEvent.ACTION_MOVE: {
                return move(motionEvent);
            }
            default:
                return false;
        }
    }

    private boolean down(MotionEvent motionEvent) {
        if (mEnable || mIsDismissing) {
            return false;
        }

        mDownX = motionEvent.getRawX();
        mDownY = motionEvent.getRawY();
        mSwipeView = mRecyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
        if (mSwipeView == null) {
            return false;
        }

        int pos = mRecyclerView.getChildPosition(mSwipeView);

        // check specific policy for a given item.
        mAllowedSwipeToDismissDirection = mDismissStrategy.getDismissDirection(pos);

        if (mAllowedSwipeToDismissDirection == SwipeToDismissDirection.NONE) {
            resetMotion();
            return false;
        } else {
            mVelocityTracker = VelocityTracker.obtain();
            mVelocityTracker.addMovement(motionEvent);
            return false;
        }
    }

    private void cancel() {
        if (mVelocityTracker == null) {
            return;
        }

        mSwipeView.animate()
            .translationX(0)
            .translationY(0)
            .alpha(1)
            .setDuration(mAnimationTime)
            .setListener(null);
        removeBackground(false);

        mVelocityTracker.recycle();
        mVelocityTracker = null;
        mTranslationX = 0;
        mDownX = 0;
        mDownY = 0;
        mSwiping = false;
        mSwipeView = null;
    }

    private void up(MotionEvent motionEvent) {
        if (mEnable || mVelocityTracker == null || mSwipeView == null || !mSwiping) {
            return;
        }
        mIsDismissing = true;
        mSwipeView.setPressed(false);
        float deltaX = motionEvent.getRawX() - mDownX;
        float deltaY = motionEvent.getRawY() - mDownY;
        mVelocityTracker.addMovement(motionEvent);
        mVelocityTracker.computeCurrentVelocity(1000);

        boolean isDismissTriggered = mAllowedSwipeToDismissDirection.triggerDismiss(deltaX, deltaY,
            mSwipeView, mVelocityTracker, mMinFlingVelocity, mMaxFlingVelocity);

        if (isDismissTriggered) {
            // dismiss
            final int pos = mRecyclerView.getChildPosition(mSwipeView);
            final View swipeViewCopy = mSwipeView;
            mAllowedSwipeToDismissDirection.animateTriggeredDismiss(mSwipeView, mAnimationTime);

            //this is instead of unreliable onAnimationEnd callback
            swipeViewCopy.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDismisser.dismiss(pos);
                    mRecyclerView.getAdapter().notifyItemRemoved(pos);
                    swipeViewCopy.setTranslationX(0);
                    swipeViewCopy.setTranslationY(0);
                }
            }, mAnimationTime + 100);

            removeBackground(true);

        } else if (mSwiping) {
            // cancel
            mSwipeView.animate()
                .translationX(0)
                .translationY(0)
                .alpha(1)
                .setDuration(mAnimationTime)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        removeBackground(false);
                    }
                });
        }

        resetMotion();
    }

    private boolean move(MotionEvent motionEvent) {
        if (mSwipeView == null || mVelocityTracker == null || mEnable) {
            return false;
        }

        mVelocityTracker.addMovement(motionEvent);
        float deltaX = motionEvent.getRawX() - mDownX;
        float deltaY = motionEvent.getRawY() - mDownY;

        if (mAllowedSwipeToDismissDirection.isSwiping(deltaX, deltaY, mSlop)) {
            mSwiping = true;
            mSwipingSlop = (deltaX > 0 ? mSlop : -mSlop);
            mSwipeView.setPressed(false);

            MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
            cancelEvent.setAction(MotionEvent.ACTION_CANCEL
                | (motionEvent.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
            mSwipeView.onTouchEvent(cancelEvent);

            // display a simple view to fill the blank space let by the swiped view.
            if (mBackgroundColor != -1 && mBackground == null) {
                mBackground = new View(mRecyclerView.getContext());
                mBackground.setBackgroundColor(mBackgroundColor);
                mBackground.setX(mSwipeView.getX());
                mBackground.setY(mSwipeView.getY());
                ViewGroup.LayoutParams layoutParams
                    = new ViewGroup.LayoutParams(mSwipeView.getWidth(), mSwipeView.getHeight());
                mBackground.setLayoutParams(layoutParams);

                int index = ((ViewGroup) mRecyclerView.getParent()).indexOfChild(mRecyclerView);
                ((ViewGroup) mRecyclerView.getParent()).addView(mBackground, Math.max(0, index - 1));
            }
        }

        //Prevent swipes to disallowed directions
        if ((deltaX < 0 && mAllowedSwipeToDismissDirection == SwipeToDismissDirection.RIGHT)
            || (deltaX > 0 && mAllowedSwipeToDismissDirection == SwipeToDismissDirection.LEFT)
            || (deltaY > 0 && mAllowedSwipeToDismissDirection == SwipeToDismissDirection.TOP)
            || (deltaY < 0 && mAllowedSwipeToDismissDirection == SwipeToDismissDirection.BOTTOM)) {
            if (mSwiping) {
                // cancel
                mSwipeView.animate()
                    .translationX(0)
                    .translationY(0)
                    .alpha(1)
                    .setDuration(mAnimationTime)
                    .setListener(null);
                removeBackground(false);
            }
            resetMotion();
            return false;
        }

        if (mSwiping) {
            mTranslationX = deltaX;
            mAllowedSwipeToDismissDirection.animateDismissMotion(deltaX, deltaY, mSwipeView, mSwipingSlop);
            return true;
        }
        return false;
    }

    private void removeBackground(boolean delayed) {
        if (mBackground != null) {
            if (delayed) {
                mRecyclerView.postDelayed(mDelayedBackgroundDismiss, mAnimationTime * 3 + 100);
            } else {
                resetBackground();
            }
        } else {
            mIsDismissing = false;
        }
    }

    private void resetBackground() {
        ((ViewGroup) mRecyclerView.getParent()).removeView(mBackground);
        mBackground = null;
        mIsDismissing = false;
    }

    private void resetMotion() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
        mTranslationX = 0;
        mDownX = 0;
        mDownY = 0;
        mSwiping = false;
        mSwipeView = null;
    }
}
