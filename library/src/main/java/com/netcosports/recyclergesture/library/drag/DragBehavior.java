package com.netcosports.recyclergesture.library.drag;

import android.view.View;
import android.view.ViewPropertyAnimator;

/**
 * Interface used to define which behavior to adopt while dragging.
 */
interface DragBehavior {

    /**
     * Define how the dragged view should move during a drag event.
     *
     * @param deltaX      delta between starting x and current x.
     * @param deltaY      delta between starting y and current y.
     * @param draggedView view currently dragged.
     */
    public void move(float deltaX, float deltaY, View draggedView);

    /**
     * Define if the dragged current view should be switched with the previous one in the
     * recycle view.
     *
     * @param draggedView  view currently dragged.
     * @param previousView previous view.
     * @return true to perform a switch.
     */
    public boolean shouldSwitchWithPrevious(View draggedView, View previousView);

    /**
     * Define if the dragged current view should be switched with the next one in the
     * recycle view.
     *
     * @param draggedView view currently dragged.
     * @param nextView    previous view.
     * @return true to perform a switch.
     */
    public boolean shouldSwitchWithNext(View draggedView, View nextView);

    /**
     * Define how the switched view should be animated.
     * <p/>
     * See also :
     * {@link DragBehavior#shouldSwitchWithNext(android.view.View, android.view.View)}
     * {@link DragBehavior#shouldSwitchWithPrevious(android.view.View, android.view.View)}
     *
     * @param viewToAnimate view from witch the animator should be created.
     * @param dest          current view at the destination.
     * @return animator started on the viewToSwitch.
     */
    public ViewPropertyAnimator getSwitchAnimator(View viewToAnimate, View dest);

    /**
     * Define how the dragged view will be animated once the user release it.
     *
     * @param viewToAnimate view from witch the animator should be created.
     * @param dest          recycle view destination.
     * @return animator started on the dragged view when user drop it.
     */
    public ViewPropertyAnimator getDropAnimator(View viewToAnimate, View dest);

    /**
     * Define if the recycle view should be scrolled in the "start" direction while dragging.
     *
     * @param recyclerView recycler view where drag event happened.
     * @param pointerX     x coordinate of the pointer.
     * @param pointerY     y coordinate of the pointer.
     * @param draggedView  view currently being dragged.
     * @return true if a scroll to the "start" direction should be performed.
     */
    public boolean shouldStartScrollingToStart(View recyclerView, float pointerX, float pointerY, View draggedView);

    /**
     * Define if the recycle view should be scrolled in the "end" direction while dragging.
     *
     * @param recyclerView recycler view where drag event happened.
     * @param pointerX     x coordinate of the pointer.
     * @param pointerY     y coordinate of the pointer.
     * @param draggedView  view currently being dragged.
     * @return true if a scroll to the "end" direction should be performed.
     */
    public boolean shouldStartScrollingToEnd(View recyclerView, float pointerX, float pointerY, View draggedView);

    /**
     * Define how recycle view should be scrolled when a scroll is requested.
     *
     * @param recyclerView recycler view where drag event happened to perform a scroll.
     * @param velocity     velocity which should basically be used.
     */
    public void scroll(View recyclerView, int velocity);

    /**
     * Define if the dragged view will hover the previous divider with the new coordinates.
     *
     * @param previous previous view.
     * @param newX     future dragged view x.
     * @param newY     future dragged view y.
     * @return true if the dragged view should be blocked due to hover.
     */
    public boolean willHoverPreviousDivider(View previous, float newX, float newY);

    /**
     * Define if the dragged view will hover the next divider with the new coordinates.
     *
     * @param next next view.
     * @param newX future dragged view x.
     * @param newY future dragged view y.
     * @return true if the dragged view should be blocked due to hover.
     */
    public boolean willHoverNextDivider(View next, float newX, float newY);

}
