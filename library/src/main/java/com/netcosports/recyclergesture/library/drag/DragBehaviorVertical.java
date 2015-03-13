package com.netcosports.recyclergesture.library.drag;

import android.view.View;
import android.view.ViewPropertyAnimator;

/**
 * Simple drag behavior for drag only on y axis.
 */
class DragBehaviorVertical implements DragBehavior {

    @Override
    public void move(float deltaX, float deltaY, View draggedView) {
        // simple translation on Y axis
        draggedView.setTranslationY(deltaY);
    }

    @Override
    public boolean shouldSwitchWithPrevious(View draggedView, View previousView) {
        // simply check if the dragged view top is above the view at the top.
        return previousView != null && previousView.getY() > -1 && draggedView.getY() < previousView.getY();
    }

    @Override
    public boolean shouldSwitchWithNext(View draggedView, View nextView) {
        // simply check if the dragged view bottom is below the view at the bottom.
        return nextView != null && nextView.getY() > -1 && draggedView.getY() > nextView.getY();
    }

    @Override
    public ViewPropertyAnimator getSwitchAnimator(View viewToAnimate, View dest) {
        // simply move the view to animate on the destination one with a vertical motion.
        int switchViewTop = dest.getTop();
        int originalViewTop = viewToAnimate.getTop();
        int delta = originalViewTop - switchViewTop;
        viewToAnimate.setTranslationY(-delta);
        return viewToAnimate.animate().translationYBy(delta);
    }

    @Override
    public ViewPropertyAnimator getDropAnimator(View viewToAnimate, View dest) {
        float y = dest.getY();
        return viewToAnimate.animate().translationY(y);
    }

    @Override
    public boolean shouldStartScrollingToStart(View recyclerView, float pointerX, float pointerY, View draggedView) {
        return pointerY <= draggedView.getHeight();
    }

    @Override
    public boolean shouldStartScrollingToEnd(View recyclerView, float pointerX, float pointerY, View draggedView) {
        return pointerY >= recyclerView.getHeight() - draggedView.getHeight();
    }

    @Override
    public void scroll(View recyclerView, int velocity) {
        recyclerView.scrollBy(0, velocity);
    }

    @Override
    public boolean willHoverPreviousDivider(View previous, float newX, float newY) {
        return newY < previous.getY();
    }

    @Override
    public boolean willHoverNextDivider(View next, float newX, float newY) {
        return newY > next.getY();
    }
}
