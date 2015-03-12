package com.netcosports.recyclergesture.library.drag;

import android.support.v7.widget.RecyclerView;

import com.netcosports.recyclergesture.library.RecyclerArrayAdapter;
import com.netcosports.recyclergesture.library.RecyclerGesture;

/**
 * Gesture which allow user to perform a drag and drop between raw items of a
 * {@link android.support.v7.widget.RecyclerView}
 */
public final class DragDropGesture extends RecyclerGesture {

    /**
     * Manager which process gesture detection.
     */
    private DragDropListener dragDropListener;

    /**
     * Turn default constructor private.
     * <p/>
     * See also : {@link com.netcosports.recyclergesture.library.drag.DragDropGesture.Builder}
     */
    private DragDropGesture() {
        super();
    }

    /**
     * Builder constructor.
     * <p/>
     * See also : {@link com.netcosports.recyclergesture.library.drag.DragDropGesture.Builder}
     *
     * @param recyclerView recyclerView on which gesture is detected.
     * @param adapter      data of the recyclerView.
     * @param dragBehavior behavior to adopt while dragging.
     * @param strategy     drag strategy.
     */
    private DragDropGesture(RecyclerView recyclerView, RecyclerArrayAdapter adapter,
                            DragBehavior dragBehavior, DragStrategy strategy) {
        super();
        dragDropListener = new DragDropListener(recyclerView, adapter, dragBehavior, strategy);
        recyclerView.addOnItemTouchListener(dragDropListener);
    }

    @Override
    public void setEnable(boolean enable) {
        super.setEnable(enable);
        this.dragDropListener.setEnabled(enable);
    }

    /**
     * Builder pattern.
     */
    public static final class Builder {

        /**
         * Recycler view on which the detector will be attached
         */
        private RecyclerView attachedRecyclerView;

        /**
         * Currently only works with ArrayList based adapter.
         */
        private RecyclerArrayAdapter recyclerArrayAdapter;

        /**
         * Gesture orientation.
         */
        private DragBehavior dragBehavior;

        /**
         * Define which items are draggable.
         */
        private DragStrategy dragStrategy;

        /**
         * Builder pattern.
         */
        public Builder() {
            this.attachedRecyclerView = null;
            this.recyclerArrayAdapter = null;
            this.dragBehavior = null;
            this.dragStrategy = null;
        }

        /**
         * Attach the gesture to the recycler view.
         *
         * @param target recycler view on which the drag and drop gesture will be attached.
         * @return builder to chain param.
         */
        public Builder on(RecyclerView target) {
            this.attachedRecyclerView = target;
            return this;
        }

        /**
         * Currently drag an drop works only on ArrayList based adapter.
         *
         * @param adapter adapter attached to the recycler view.
         * @return builder to chain param.
         */
        public Builder with(RecyclerArrayAdapter adapter) {
            this.recyclerArrayAdapter = adapter;
            return this;
        }

        /**
         * Apply drag strategy to customize which items are draggable.
         *
         * @param strategy drag strategy.
         * @return builder to chain param.
         */
        public Builder apply(DragStrategy strategy) {
            this.dragStrategy = strategy;
            return this;
        }

        /**
         * Indicate the orientation of your recycler view is horizontal.
         *
         * @return builder to chain param.
         */
        public Builder horizontal() {
            this.dragBehavior = new DragBehaviorHorizontal();
            return this;
        }

        /**
         * Indicate the orientation of your recycler view is vertical.
         *
         * @return builder to chain param.
         */
        public Builder vertical() {
            this.dragBehavior = new DragBehaviorVertical();
            return this;
        }

        /**
         * Build the gesture based on builder param.
         *
         * @return well instantiate gesture.
         */
        public DragDropGesture build() {
            if (this.recyclerArrayAdapter == null) {
                throw new IllegalStateException("Adapter can't be null, see Builder.with(adapter)");
            }

            if (this.attachedRecyclerView == null) {
                throw new IllegalStateException("Recycler view can't be null, see Builder.on(recyclerView)");
            }

            if (this.dragBehavior == null) {
                this.dragBehavior = new DragBehaviorVertical();
            }

            if (this.dragStrategy == null) {
                this.dragStrategy = new DragStrategy();
            }

            return new DragDropGesture(this.attachedRecyclerView, this.recyclerArrayAdapter,
                    this.dragBehavior, this.dragStrategy);
        }
    }
}
