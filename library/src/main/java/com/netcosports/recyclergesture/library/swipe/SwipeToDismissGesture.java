package com.netcosports.recyclergesture.library.swipe;

import android.support.v7.widget.RecyclerView;

import com.netcosports.recyclergesture.library.RecyclerGesture;

/**
 * Simple swipe to dismiss gesture.
 */
public final class SwipeToDismissGesture extends RecyclerGesture {

    /**
     * Internal listener used to catch dismiss event.
     */
    private SwipeToDismissListener mSwipeToDismissListener;

    /**
     * Default constructor.
     */
    private SwipeToDismissGesture() {

    }

    /**
     * Simple swipe to dismiss gesture.
     *
     * @param recycler  recycler view on which the gesture will be applied.
     * @param direction swipe direction.
     * @param strategy  dismiss strategy applied.
     * @param dismisser dismisser which will perform the dismiss.
     */
    private SwipeToDismissGesture(RecyclerView recycler, SwipeToDismissDirection direction,
                                  SwipeToDismissStrategy strategy, Dismisser dismisser) {
        mSwipeToDismissListener = new SwipeToDismissListener(recycler, direction, strategy, dismisser);
        recycler.addOnItemTouchListener(mSwipeToDismissListener);
    }

    @Override
    public void setEnable(boolean enable) {
        super.setEnable(enable);
        mSwipeToDismissListener.setEnabled(enable);
    }

    /**
     * Builder pattern.
     */
    public static class Builder {
        /**
         * recycler vie won which the gesture will be applied.
         */
        private RecyclerView recyclerView;

        /**
         * dismiss behavior.
         */
        private Dismisser dismisser;

        /**
         * swipe strategy.
         */
        private SwipeToDismissStrategy strategy;

        /**
         * direction for which the swipe-to-dismiss can be triggered.
         */
        private SwipeToDismissDirection direction;

        /**
         * Builder pattern for {@link SwipeToDismissGesture}
         *
         * @param direction Direction which will triggered the swipe-to-dismiss motion.
         *                  {@link SwipeToDismissDirection#TOP}
         *                  {@link SwipeToDismissDirection#BOTTOM}
         *                  {@link SwipeToDismissDirection#VERTICAL}
         *                  {@link SwipeToDismissDirection#LEFT}
         *                  {@link SwipeToDismissDirection#RIGHT}
         *                  {@link SwipeToDismissDirection#HORIZONTAL}
         */
        public Builder(SwipeToDismissDirection direction) {
            this.direction = direction;
            recyclerView = null;
            dismisser = null;
            strategy = null;
        }

        /**
         * Attach the gesture to the recycler view.
         * <p/>
         * Note : the recycler adapter must implements
         * {@link com.netcosports.recyclergesture.library.swipe.SwipeToDismissGesture.Dismisser} interface to
         * proceed to the dismiss.
         *
         * @param target recycler view on which the dismiss gesture will be attached.
         * @return builder to chain param.
         */
        public Builder on(RecyclerView target) {
            this.recyclerView = target;
            if (!(this.recyclerView.getAdapter() instanceof Dismisser)) {
                throw new IllegalArgumentException("RecyclerView adapter must implement Dismisser"
                        + " interface to proceed to the data swapping");
            }
            this.dismisser = ((Dismisser) this.recyclerView.getAdapter());
            return this;
        }

        /**
         * Strategy applied to know if the items can be dismissed.
         *
         * @param strategy strategy used to know if the items can be "dismissable".
         * @return builder to chain param.
         */
        public Builder apply(SwipeToDismissStrategy strategy) {
            if (strategy != null) {
                this.strategy = strategy;
            }
            return this;
        }

        /**
         * Builder pattern.
         *
         * @return swipe to dismiss gesture instance.
         */
        public SwipeToDismissGesture build() {
            if (this.recyclerView == null) {
                throw new IllegalStateException("Recycler view can't be null");
            }

            if (this.direction == null) {
                throw new IllegalStateException("A swipe direction must be specified through withDirection");
            }

            return new SwipeToDismissGesture(recyclerView, direction, strategy, dismisser);
        }
    }

    /**
     * Interface used to dismiss item when dismiss motion occurred.
     */
    public interface Dismisser {
        /**
         * Called when a swipe to dismiss motion trigger a dismiss.
         *
         * @param position position of the item which should be remove from the data.
         */
        void dismiss(int position);
    }
}
