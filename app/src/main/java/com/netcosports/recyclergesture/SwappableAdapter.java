package com.netcosports.recyclergesture;

import android.support.v7.widget.RecyclerView;

import com.netcosports.recyclergesture.library.drag.DragDropGesture;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Simple {@link com.netcosports.recyclergesture.RecyclerArrayAdapter} which implements
 * {@link com.netcosports.recyclergesture.library.drag.DragDropGesture.Swapper} interface.
 */
public abstract class SwappableAdapter<T, H extends RecyclerView.ViewHolder> extends RecyclerArrayAdapter<T, H>
        implements DragDropGesture.Swapper {

    /**
     * Simple {@link com.netcosports.recyclergesture.RecyclerArrayAdapter} which implements
     * {@link com.netcosports.recyclergesture.library.drag.DragDropGesture.Swapper} interface.
     *
     * @param items models.
     */
    public SwappableAdapter(ArrayList<T> items) {
        super(items);
    }

    @Override
    public void swapPositions(int from, int to) {
        Collections.swap(getItems(), from, to);
    }
}
