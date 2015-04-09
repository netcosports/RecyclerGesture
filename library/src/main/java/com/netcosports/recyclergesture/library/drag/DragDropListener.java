/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.netcosports.recyclergesture.library.drag;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;


/**
 * Implementation of RecyclerView.OnItemTouchListener that allows reordering items in
 * RecyclerView by dragging and dropping. Instance of this class should be added to RecyclerView
 * using {@link RecyclerView#addOnItemTouchListener(RecyclerView.OnItemTouchListener)} method.
 */

class DragDropListener implements RecyclerView.OnItemTouchListener {

    /**
     * Switch motion delay in milliseconds.
     */
    private static final int MOVE_DURATION = 150;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    private final int scrollAmount;
    private int downY = -1;
    private int downX = -1;
    private View mobileView;
    private float mobileViewStartY = -1;
    private float mobileViewStartX = -1;
    private int mobileViewCurrentPos = -1;
    private int activePointerId;
    private boolean dragging;
    private boolean enabled = true;

    /**
     * Bitmap used to build the view displayed as dragging thumbnail.
     */
    private Bitmap draggingThumbnail;

    /**
     * Simple gesture listener used to cached long touched in order to start the drag.
     */
    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener;

    /**
     * Gesture detector used to process event.
     */
    private GestureDetector gestureDetector;

    /**
     * Auto scrolling while dragging.
     */
    private AutoScroller autoScroller;

    /**
     * Used to know if the auto scroller is already started.
     */
    private boolean isScrolling;

    /**
     * Behavior to adopt while dragging.
     */
    private DragBehavior dragBehavior;

    /**
     * Strategy used to enable or disable drag on specific items.
     */
    private DragStrategy dragStrategy;

    /**
     * View holder of the previous item which can't be crossed while dragging.
     */
    private RecyclerView.ViewHolder previousDividerViewHolder;

    /**
     * Index of the previous item which can't be hovered.
     */
    private int previousDividerPosition;

    /**
     * View holder of the next item which can't be crossed while dragging.
     */
    private RecyclerView.ViewHolder nextDividerViewHolder;

    /**
     * Index of the next item which can't be hovered.
     */
    private int nextDividerPosition;

    /**
     * Swapper used to swap data once an item is dropped.
     */
    private DragDropGesture.Swapper swapper;


    /**
     * Drag and drop listener.
     *
     * @param recyclerView recycler view on which listener will be applied.
     * @param adapter      adapter.
     * @param swapper      swapper used to swap items model once a drop event happened.
     * @param dragBehavior behavior to adopt while dragging.
     * @param dragStrategy strategy used to enable drag on items.
     */
    public DragDropListener(RecyclerView recyclerView, RecyclerView.Adapter adapter
            , DragDropGesture.Swapper swapper, DragBehavior dragBehavior, DragStrategy dragStrategy) {
        this.dragBehavior = dragBehavior;
        this.recyclerView = recyclerView;
        this.dragStrategy = dragStrategy;
        this.adapter = adapter;
        this.swapper = swapper;

        dragging = false;

        DisplayMetrics displayMetrics = recyclerView.getResources().getDisplayMetrics();
        this.scrollAmount = (int) (50 / displayMetrics.density);

        // init gesture listener used to catch long pressed event.
        initInternalGestureListener();

        // init auto scroller used to scroll while dragging.
        autoScroller = new AutoScroller();

        this.previousDividerViewHolder = null;
        this.nextDividerViewHolder = null;
        this.previousDividerPosition = -1;
        this.nextDividerPosition = -1;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent event) {
        if (!enabled) {
            return false;
        }

        // dragging not start, listen for long pressed
        if (!dragging) {
            gestureDetector.onTouchEvent(event);
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                return down(event);

            case MotionEvent.ACTION_MOVE:
                return dragging && move(event);

            case MotionEvent.ACTION_UP:
                return up(event);

            case MotionEvent.ACTION_CANCEL:
                return cancel(event);

            default:
                return false;
        }
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent event) {
        if (!dragging) {
            return;
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                move(event);
                break;

            case MotionEvent.ACTION_UP:
                up(event);
                break;

            case MotionEvent.ACTION_CANCEL:
                cancel(event);
                break;

            default:
                break;

        }
    }

    /**
     * Enable/disable drag/drop
     *
     * @param enabled true to enable gesture.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private void startDrag() {
        View viewUnder = recyclerView.findChildViewUnder(downX, downY);
        mobileViewCurrentPos = recyclerView.getChildPosition(viewUnder);
        if (viewUnder == null) {
            return;
        }

        // check strategy to know if the current item is draggable.
        if (!dragStrategy.isItemDraggable(mobileViewCurrentPos)) {
            return;
        }

        dragging = true;

        // get closest divider index to block the drag if needed
        findClosestDivider();

        // initialize the view used as thumbnail while dragging.
        mobileView = getDraggingView(viewUnder);
        mobileView.setX(viewUnder.getX());
        mobileView.setY(viewUnder.getY());
        mobileViewStartY = mobileView.getY();
        mobileViewStartX = mobileView.getX();

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ((ViewGroup) recyclerView.getParent()).addView(mobileView, lp);
        mobileView.bringToFront();
        viewUnder.setVisibility(View.INVISIBLE);
    }

    /**
     * Find closest divider when drag start.
     * <p/>
     * Divider are items which can't be hovered by a drag motion.
     * <p/>
     * See also :
     * {@link com.netcosports.recyclergesture.library.drag.DragStrategy#isItemHoverable(int)}
     */
    private void findClosestDivider() {
        previousDividerViewHolder = null;
        nextDividerViewHolder = null;
        previousDividerPosition = -1;
        nextDividerPosition = -1;
        for (int i = 0; i < recyclerView.getAdapter().getItemCount(); i++) {
            boolean isHoverable = dragStrategy.isItemHoverable(i);
            // if hoverable, not a divider, go to the next.
            if (isHoverable) {
                continue;
            }

            if (i < mobileViewCurrentPos) {
                if (previousDividerPosition == -1) {
                    previousDividerPosition = i;
                } else if (i > previousDividerPosition) {
                    previousDividerPosition = i;
                }
            } else if (i > mobileViewCurrentPos) {
                if (nextDividerPosition == -1) {
                    nextDividerPosition = i;
                } else if (i < nextDividerPosition) {
                    nextDividerPosition = i;
                }
            }
        }

        previousDividerViewHolder = recyclerView.findViewHolderForPosition(previousDividerPosition);
        nextDividerViewHolder = recyclerView.findViewHolderForPosition(nextDividerPosition);
    }

    private boolean down(MotionEvent event) {
        activePointerId = event.getPointerId(0);
        downY = (int) event.getY();
        downX = (int) event.getX();
        return false;
    }

    private boolean move(MotionEvent event) {
        if (activePointerId == -1) {
            return false;
        }

        int pointerIndex = event.findPointerIndex(activePointerId);

        int currentX = (int) event.getX(pointerIndex);
        float deltaX = currentX - downX;
        float mobileViewX = mobileViewStartX + deltaX;

        int currentY = (int) event.getY(pointerIndex);
        float deltaY = currentY - downY;
        float mobileViewY = mobileViewStartY + deltaY;


        if (previousDividerViewHolder != null) {
            View previousDivider = previousDividerViewHolder.itemView;
            if (previousDivider != null
                    && dragBehavior.willHoverPreviousDivider(previousDivider, mobileViewX, mobileViewY)) {
                return false;
            }
        }

        if (nextDividerViewHolder != null) {
            View nextDivider = nextDividerViewHolder.itemView;
            if (nextDivider != null
                    && dragBehavior.willHoverNextDivider(nextDivider, mobileViewX, mobileViewY)) {
                return false;
            }
        }

        scrollIfNeeded();

        dragBehavior.move(mobileViewX, mobileViewY, mobileView);

        // is scrolling, switch will be handle by the auto scroller since
        // view will be recycled, closest divider should be refreshed.
        if (!isScrolling) {
            switchViewsIfNeeded();
        }

        return true;
    }

    private void switchViewsIfNeeded() {
        int pos = mobileViewCurrentPos;
        int previousPos = pos - 1;
        int nextPos = pos + 1;

        View previousView = getViewByPosition(previousPos);
        View nextView = getViewByPosition(nextPos);

        if (previousView != null && dragBehavior.shouldSwitchWithPrevious(mobileView, previousView)) {
            Log.d("LARGONNE", "switch previous");
            doSwitch(previousView, pos, previousPos);
        } else if (nextView != null && dragBehavior.shouldSwitchWithNext(mobileView, nextView)) {
            Log.d("LARGONNE", "switch next");
            doSwitch(nextView, pos, nextPos);
        }
    }

    private void doSwitch(final View switchView, final int originalViewPos, final int switchViewPos) {
        View originalView = getViewByPosition(originalViewPos);

        onItemSwitch(originalViewPos, switchViewPos);

        switchView.setVisibility(View.INVISIBLE);

        if (originalView != null) {
            originalView.setVisibility(View.VISIBLE);
            dragBehavior.getSwitchAnimator(originalView, switchView)
                    .setDuration(MOVE_DURATION);
        }


        mobileViewCurrentPos = switchViewPos;
    }

    private boolean up(MotionEvent event) {
        endDrag();
        return false;
    }

    private boolean cancel(MotionEvent event) {
        endDrag();
        return false;
    }

    /**
     * Animate dragged view to it's position.
     */
    private void endDrag() {
        final View view = getViewByPosition(mobileViewCurrentPos);
        if (view != null && mobileView != null) {

            dragBehavior.getDropAnimator(mobileView, view)
                    .setDuration(MOVE_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.setVisibility(View.VISIBLE);
                            if (mobileView != null) {
                                ViewGroup parent = (ViewGroup) mobileView.getParent();
                                parent.removeView(mobileView);
                                draggingThumbnail.recycle();
                                draggingThumbnail = null;
                                mobileView = null;
                                recyclerView.removeCallbacks(autoScroller);
                            }
                        }
                    })
                    .start();
        }

        dragging = false;
        mobileViewStartY = -1;
        mobileViewStartX = -1;
        mobileViewCurrentPos = -1;
    }

    /**
     * propagate the switch to the adapter.
     *
     * @param from original (start) drag position within adapter
     * @param to   new drag position withing adapter
     */
    private void onItemSwitch(int from, int to) {
        swapper.swapPositions(from, to);
        adapter.notifyItemChanged(to);
    }

    private View getViewByPosition(int position) {
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForPosition(position);
        return viewHolder == null ? null : viewHolder.itemView;
    }

    /**
     * Scroll the recycler view while dragging if needed.
     * <p/>
     * See also :
     * {@link DragBehavior#shouldStartScrollingToStart(android.view.View, android.view.View)}
     * {@link DragBehavior#shouldStartScrollingToEnd(android.view.View, android.view.View)}
     *
     * @return true if the recycler view is being scrolled
     */
    private boolean scrollIfNeeded() {

        boolean shouldScrollToStart = dragBehavior.shouldStartScrollingToStart(recyclerView, mobileView);
        boolean shouldScrollToEnd = dragBehavior.shouldStartScrollingToEnd(recyclerView, mobileView);

        if (shouldScrollToStart && !autoScroller.isScrollingStart()) {
            autoScroller.startScrolling(AutoScroller.START);
        } else if (shouldScrollToEnd && !autoScroller.isScrollingEnd()) {
            autoScroller.startScrolling(AutoScroller.END);
        } else if (!shouldScrollToEnd && !shouldScrollToStart && isScrolling) {
            autoScroller.stopScrolling();
        }
        return isScrolling;
    }

    /**
     * Build view which will be used while user performing a drag event.
     *
     * @param v touched view after a long press.
     * @return View which will be used as dragging thumbnail.
     */
    private View getDraggingView(View v) {
        //Clear ripple effect to not get into screenshot,
        // need something more clever here
        if (v instanceof FrameLayout) {
            FrameLayout frameLayout = (FrameLayout) v;
            Drawable foreground = frameLayout.getForeground();
            if (foreground != null) {
                foreground.setVisible(false, false);
            }
        } else {
            if (v.getBackground() != null) {
                v.getBackground().setVisible(false, false);
            }
        }

        draggingThumbnail = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(draggingThumbnail);
        v.draw(canvas);

        ImageView imageView = new ImageView(recyclerView.getContext());
        imageView.setAlpha(0.8f);
        imageView.setImageBitmap(draggingThumbnail);
        return imageView;
    }

    /**
     * Initialize internal gesture listener used to catch long press event on a raw in order to
     * start the drag event.
     */
    private void initInternalGestureListener() {
        simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

            @Override
            public void onLongPress(MotionEvent e) {
                startDrag();
            }

        };
        gestureDetector = new GestureDetector(recyclerView.getContext(), simpleOnGestureListener);
    }

    /**
     * Auto scroller used to scroll the recycler view while dragging.
     */
    private final class AutoScroller implements Runnable {

        /**
         * Scroll to the start of the recycle view.
         */
        static final int START = -1;

        /**
         * Scroll to the end of the recycle view.
         */
        static final int END = 1;

        /**
         * Direction when not scrolling.
         */
        static final int NONE = 0;

        private int direction;

        public AutoScroller() {
            direction = NONE;
        }

        public void startScrolling(int direction) {
            if (direction != START && direction != END) {
                throw new IllegalArgumentException("Direction unknown");
            }
            isScrolling = true;
            this.direction = direction;
            recyclerView.post(this);
        }

        public void stopScrolling() {
            isScrolling = false;
            direction = NONE;
            recyclerView.removeCallbacks(this);
        }

        @Override
        public void run() {
            dragBehavior.scroll(recyclerView, direction * scrollAmount);

            float nextX = mobileView.getX() + direction * scrollAmount;
            float nextY = mobileView.getY() + direction * scrollAmount;

            if (previousDividerPosition != -1) {
                previousDividerViewHolder = recyclerView.findViewHolderForPosition(previousDividerPosition);
            }

            if (nextDividerPosition != -1) {
                nextDividerViewHolder = recyclerView.findViewHolderForPosition(nextDividerPosition);
            }

            if (previousDividerViewHolder != null) {
                View previousDivider = previousDividerViewHolder.itemView;
                if (previousDivider != null
                        && dragBehavior.willHoverPreviousDivider(previousDivider, nextX, nextY)) {
                    // stop scrolling when blocked by a divider
                    isScrolling = false;
                    return;
                }
            }

            if (nextDividerViewHolder != null) {
                View nextDivider = nextDividerViewHolder.itemView;
                if (nextDivider != null
                        && dragBehavior.willHoverNextDivider(nextDivider, nextX, nextY)) {
                    // stop scrolling when blocked by a divider
                    isScrolling = false;
                    return;
                }
            }

            switchViewsIfNeeded();
            recyclerView.post(this);
        }

        /**
         * Used to know if scrolling to start.
         *
         * @return true when scroller is scrolling to start.
         */
        public boolean isScrollingStart() {
            return direction == START;
        }

        /**
         * Used to know if scrolling to end.
         *
         * @return true when scroller is scrolling to end.
         */
        public boolean isScrollingEnd() {
            return direction == END;
        }
    }
}
