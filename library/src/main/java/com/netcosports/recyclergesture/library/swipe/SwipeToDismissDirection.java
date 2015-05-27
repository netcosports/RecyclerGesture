package com.netcosports.recyclergesture.library.swipe;

import android.view.VelocityTracker;
import android.view.View;

/**
 * Allowed swipe to dismiss direction.
 */
public enum SwipeToDismissDirection {
    /**
     * Swipe to dismiss only triggered by horizontal motion to the left.
     */
    LEFT(DismissStrategy.HORIZONTAL),
    /**
     * Swipe to dismiss only triggered by horizontal motion to the left.
     */
    RIGHT(DismissStrategy.HORIZONTAL),
    /**
     * Swipe to dismiss only triggered by horizontal motion.
     */
    HORIZONTAL(DismissStrategy.HORIZONTAL),
    /**
     * Swipe to dismiss only triggered by vertical motion.
     */
    VERTICAL(DismissStrategy.VERTICAL),
    /**
     * Swipe to dismiss only triggered by vertical motion to the top.
     */
    TOP(DismissStrategy.VERTICAL),
    /**
     * Swipe to dismiss only triggered by vertical motion to the bottom.
     */
    BOTTOM(DismissStrategy.VERTICAL),
    /**
     * Swipe to dismiss won't be triggered.
     */
    NONE(DismissStrategy.VERTICAL);

    private final DismissStrategy dismissStrategy;

    /**
     * Allowed swipe to dismiss direction.
     *
     * @param strategy strategy used during swipe to dismiss motion.
     */
    SwipeToDismissDirection(DismissStrategy strategy) {
        this.dismissStrategy = strategy;
    }


    /**
     * Animate the dismissing motion.
     * Should be called when a new {@link android.view.MotionEvent#ACTION_MOVE} is catch.
     * private package.
     *
     * @param deltaX      event delta X.
     * @param deltaY      event delta Y.
     * @param swipedView  view which is currently dismissed.
     * @param swipingSlop motion slop.
     */
    void animateDismissMotion(float deltaX, float deltaY, View swipedView, int swipingSlop) {
        dismissStrategy.animateDismissMotion(deltaX, deltaY, swipedView, swipingSlop);
    }

    /**
     * Animate the dismissing motion once the dismiss is triggered.
     * Should be called when a {@link android.view.MotionEvent#ACTION_UP} triggered a dismiss.
     *
     * @param swipeView view which is currently dismissed.
     * @param duration  wished duration of the dismiss animation.
     */
    void animateTriggeredDismiss(View swipeView, long duration) {
        if (dismissStrategy == DismissStrategy.HORIZONTAL) {
            swipeView.animate()
                    .translationX(dismissStrategy.getTriggeredDismissDirection()
                        * swipeView.getWidth())
                    .alpha(0)
                    .setDuration(duration)
                    .setListener(null);
        } else {
            swipeView.animate()
                    .translationY(dismissStrategy.getTriggeredDismissDirection()
                        * swipeView.getHeight())
                    .alpha(0)
                    .setDuration(duration)
                    .setListener(null);
        }
    }

    /**
     * Used to know if the dismiss event should be triggered according to the user motion.
     * package private.
     *
     * @param deltaX           x delta between current pointerX and x at down.
     * @param deltaY           y delta between current pointerY and y at down.
     * @param swipedView       view being swiped.
     * @param velocityTracker  velocity tracker used to process the velocity of the motion.
     * @param minFlingVelocity minimum velocity to trigger a dismiss.
     * @param maxFlingVelocity maximum velocity to trigger a dismiss.
     * @return true if the dismiss event can be fired.
     */
    boolean triggerDismiss(float deltaX, float deltaY, View swipedView,
                           VelocityTracker velocityTracker, int minFlingVelocity, int maxFlingVelocity) {
        return dismissStrategy.isDismissing(deltaX, deltaY, swipedView, velocityTracker,
                minFlingVelocity, maxFlingVelocity);
    }

    /**
     * Used to know it the started swipe motion leads to a dismiss or has been canceled.
     *
     * @param deltaX x delta between current pointerX and x at down.
     * @param deltaY y delta between current pointerY and y at down.
     * @param slop   touch slop.
     * @return true if the started motion is a swipe to dismiss one.
     */
    boolean isSwiping(float deltaX, float deltaY, float slop) {
        return dismissStrategy.isSwiping(deltaX, deltaY, slop);
    }

    /**
     * Dismiss strategy.
     * <p/>
     * Encapsulate dismissing motion and triggering behavior.
     */
    private enum DismissStrategy {
        /**
         * Dismiss strategy for any horizontal {@link SwipeToDismissDirection}
         */
        HORIZONTAL {
            void animateDismissMotion(float deltaX, float deltaY, View swipedView, int swipingSlop) {
                swipedView.setTranslationX(deltaX - swipingSlop);
                swipedView.setAlpha(Math.max(0f, Math.min(1f,
                        1f - 2f * Math.abs(deltaX) / swipedView.getWidth())));
            }

            boolean isDismissing(float deltaX, float deltaY, View swipedView, VelocityTracker velocityTracker,
                                 int minFlingVelocity, int maxFlingVelocity) {
                float velocityX = velocityTracker.getXVelocity();
                float absVelocityX = Math.abs(velocityX);
                float absVelocityY = Math.abs(velocityTracker.getYVelocity());
                boolean dismiss = false;
                setDismissDirection(-1);
                boolean dismissRight = false;
                if (Math.abs(deltaX) > swipedView.getWidth() / 2) {
                    dismiss = true;
                    dismissRight = deltaX > 0;
                } else if (minFlingVelocity <= absVelocityX && absVelocityX <= maxFlingVelocity
                        && absVelocityY < absVelocityX) {
                    // dismiss only if flinging in the same direction as dragging
                    dismiss = (velocityX < 0) == (deltaX < 0);
                    dismissRight = velocityTracker.getXVelocity() > 0;
                }
                if (dismissRight) {
                    setDismissDirection(1);
                }
                return dismiss;
            }

            boolean isSwiping(float deltaX, float deltaY, float slop) {
                return Math.abs(deltaX) > slop && Math.abs(deltaY) < Math.abs(deltaX) / 2;
            }

        },
        /**
         * Dismiss strategy for any vertical {@link SwipeToDismissDirection}
         */
        VERTICAL {
            void animateDismissMotion(float deltaX, float deltaY, View swipedView, int swipingSlop) {
                swipedView.setTranslationY(deltaY - swipingSlop);
                swipedView.setAlpha(Math.max(0f, Math.min(1f,
                        1f - 2f * Math.abs(deltaY) / swipedView.getHeight())));
            }

            boolean isDismissing(float deltaX, float deltaY, View swipeView,
                                 VelocityTracker velocityTracker, int minFlingVelocity, int maxFlingVelocity) {

                float velocityY = velocityTracker.getYVelocity();
                float absVelocityY = Math.abs(velocityY);
                float absVelocityX = Math.abs(velocityTracker.getXVelocity());
                boolean dismiss = false;
                setDismissDirection(-1);
                boolean dismissBottom = false;
                if (Math.abs(deltaY) > swipeView.getHeight() / 2) {
                    dismiss = true;
                    dismissBottom = deltaX > 0;
                } else if (minFlingVelocity <= absVelocityY && absVelocityY <= maxFlingVelocity
                        && absVelocityX < absVelocityY) {
                    // dismiss only if flinging in the same direction as dragging
                    dismiss = (velocityY < 0) == (deltaY < 0);
                    dismissBottom = velocityTracker.getYVelocity() > 0;
                }
                if (dismissBottom) {
                    setDismissDirection(1);
                }
                return dismiss;
            }

            boolean isSwiping(float deltaX, float deltaY, float slop) {
                return Math.abs(deltaY) > slop && Math.abs(deltaX) < Math.abs(deltaY) / 2;
            }
        };

        private int dismissDirection;


        /**
         * Animate view while user is swiping to perform a  dismiss.
         *
         * @param deltaX      x delta between current pointerX and x at down.
         * @param deltaY      y delta between current pointerY and y at down.
         * @param swipedView  view being swiped.
         * @param swipingSlop touch slop.
         */
        abstract void animateDismissMotion(float deltaX, float deltaY, View swipedView, int swipingSlop);


        /**
         * Used to know if the started swipe to dismiss motion should triggered a dismiss event.
         *
         * @param deltaX           x delta between current pointerX and x at down.
         * @param deltaY           y delta between current pointerY and y at down.
         * @param swipeView        view being swiped.
         * @param velocityTracker  velocity tracker used to process the velocity of the motion.
         * @param minFlingVelocity minimum velocity to trigger a dismiss.
         * @param maxFlingVelocity maximum velocity to trigger a dismiss.
         * @return true if a dismiss is triggered.
         */
        abstract boolean isDismissing(float deltaX, float deltaY, View swipeView,
                                      VelocityTracker velocityTracker, int minFlingVelocity, int maxFlingVelocity);

        /**
         * Used to know it the started swipe motion leads to a dismiss or has been canceled.
         *
         * @param deltaX x delta between current pointerX and x at down.
         * @param deltaY y delta between current pointerY and y at down.
         * @param slop   touch slop.
         * @return true if the started motion is a swipe to dismiss one.
         */
        abstract boolean isSwiping(float deltaX, float deltaY, float slop);

        /**
         * Retrieve the dismissing direction.
         * -1 triggered dismissing motion to start (top if vertical, left if horizontal)
         * 0 no dismissing motion triggered.
         * 1 triggered dismissing motion to end (bottom if vertical, right if horizontal)
         *
         * @return triggered dismissing motion direction.
         */
        int getTriggeredDismissDirection() {
            return dismissDirection;
        }

        /**
         * Set the dismissing direction.
         *
         * @param direction -1 triggered dismissing motion to start (top if vertical, left if horizontal)
         *                  0 no dismissing motion triggered.
         *                  1 triggered dismissing motion to end (bottom if vertical, right if horizontal)
         */
        void setDismissDirection(int direction) {
            dismissDirection = direction;
        }
    }
}
