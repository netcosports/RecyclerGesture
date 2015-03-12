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
 * /
 */

package com.netcosports.recyclergesture.library;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Extension to standard RecyclerView.Adapter that also keep state of selected/activated items.
 *
 * @param <T> Type of the class in this adapter
 * @param <H> - ViewHolder type
 */
public abstract class RecyclerArrayAdapter<T, H extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<H> {

    private ArrayList<T> items;

    /**
     * Extension to standard RecyclerView.Adapter that also keep state of selected/activated items.
     *
     * @param items models.
     */
    public RecyclerArrayAdapter(ArrayList<T> items) {
        super();
        this.items = items;
        setHasStableIds(true);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= items.size()) {
            return RecyclerView.NO_ID;
        }
        return items.get(position).hashCode();

    }

    /**
     * Return the item at the given position.
     *
     * @param position item index
     * @return model.
     */
    public T getItem(int position) {
        return items.get(position);
    }

    /**
     * Swap two items.
     * <p/>
     * private package.
     *
     * @param from src position.
     * @param to   dest position.
     */
    public void swapPositions(int from, int to) {
        Collections.swap(items, from, to);
    }

    /**
     * Remove item.
     *
     * @param pos position of the item to remove.
     */
    public void removeItem(int pos) {
        items.remove(pos);
    }
}
