package com.netcosports.recyclergesture.library.drag;

import android.view.View;
import android.view.ViewPropertyAnimator;

/**
 * Simple drag behavior for drag only on x axis.
 */
public class DragBehaviorHorizontal implements DragBehavior {

    @Override
    public void move(float deltaX, float deltaY, View draggedView) {
        // simple translation on X axis
        draggedView.setTranslationX(deltaX);
    }

    @Override
    public boolean shouldSwitchWithPrevious(View draggedView, View previousView) {
        // simply check if the dragged view left is on the left of the view on the left
        return previousView != null && previousView.getX() > -1 && draggedView.getX() < previousView.getX();
    }

    @Override
    public boolean shouldSwitchWithNext(View draggedView, View nextView) {
        // simply check if the dragged view right is on the right of the view on the right
        return nextView != null && nextView.getX() > -1 && draggedView.getX() > nextView.getX();
    }

    @Override
    public ViewPropertyAnimator getSwitchAnimator(View viewToAnimate, View dest) {
        int switchViewLeft = dest.getLeft();
        int originalViewLeft = viewToAnimate.getLeft();
        int delta = originalViewLeft - switchViewLeft;
        viewToAnimate.setTranslationX(-delta);
        return viewToAnimate.animate().translationXBy(delta);
    }

    @Override
    public ViewPropertyAnimator getDropAnimator(View viewToAnimate, View dest) {
        float x = dest.getX();
        return viewToAnimate.animate().translationX(x);
    }

    @Override
    public boolean shouldStartScrollingToStart(View recyclerView, View draggedView) {
        int previousBoundary = 0;
        int hoverViewPosition = (int) draggedView.getX();
        return hoverViewPosition <= previousBoundary;
    }

    @Override
    public boolean shouldStartScrollingToEnd(View recyclerView, View draggedView) {
        int nextBoundary = recyclerView.getWidth();
        int hoverViewSize = draggedView.getWidth();
        int hoverViewPosition = (int) draggedView.getX();
        return hoverViewPosition + hoverViewSize >= nextBoundary;
    }

    @Override
    public void scroll(View recyclerView, int velocity) {
        recyclerView.scrollBy(velocity, 0);
    }
}
