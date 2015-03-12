package com.netcosports.recyclergesture.library.drag;

/**
 * Allow to define custom strategy for drag gesture.
 */
public class DragStrategy {

    /**
     * Allow to enable drag on specific item or disable it on others.
     *
     * @param position position of the item in the adapter.
     * @return true if the item is draggable.
     */
    public boolean isItemDraggable(int position) {
        return true;
    }

}
