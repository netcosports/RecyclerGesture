package com.netcosports.recyclergesture.library.swipe;

/**
 * Define own dismiss strategy to enable / disable swipe to dismiss motion on specific items.
 */
public class SwipeToDismissStrategy {

    /**
     * Default direction.
     */
    private SwipeToDismissDirection mDefaultSwipeDirection;


    /**
     * Allow to define "dismissable" policy for an item according to it's adapter position
     *
     * @param position position of the item on which a swipe to dismiss is about to begin.
     * @return {@link SwipeToDismissDirection} to indicate which direction can trigger the swipe
     * to dismiss motion. {@link SwipeToDismissDirection#NONE} if the item can't be dismissed with a
     * swipe motion.
     */
    public SwipeToDismissDirection getDismissDirection(int position) {
        return mDefaultSwipeDirection;
    }

    /**
     * Default swipe to dismiss strategy.
     *
     * @param direction default swipe direction.
     */
    final void setDefaultSwipeToDismissDirection(SwipeToDismissDirection direction) {
        mDefaultSwipeDirection = direction;
    }
}
