package com.netcosports.recyclergesture;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.netcosports.recyclergesture.library.drag.DragDropGesture;
import com.netcosports.recyclergesture.library.drag.DragStrategy;

import java.util.ArrayList;

/**
 * Activity used as sample for {@link com.netcosports.recyclergesture.library.drag.DragDropGesture}
 */
public class DragActivity extends ActionBarActivity implements HorizontalPagerLayoutManager.OnPageChangeListener {

    /**
     * Used to know if the recycler should be horizontal.
     */
    private static final String KEY_HORIZONTAL = "horizontal_orientation";

    /**
     * Used to know if the adapter should have divider.
     */
    private static final String KEY_DIVIDER = "with_divider";

    /**
     * Models
     */
    private ArrayList<DummyModel> models;

    private HorizontalPagerLayoutManager mPagerLayoutManager;

    /**
     * Start activity pattern.
     *
     * @param context     context used to start the activity.
     * @param horizontal  true if the recycler view should be display horizontally.
     * @param withDivider true to add divider in the list.
     */
    public static void startActivity(Context context, boolean horizontal, boolean withDivider) {
        Intent i = new Intent(context, DragActivity.class);
        i.putExtra(KEY_HORIZONTAL, horizontal);
        i.putExtra(KEY_DIVIDER, withDivider);
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag);

        Bundle args = getIntent().getExtras();
        if (args == null
                || !args.containsKey(KEY_HORIZONTAL)
                || !args.containsKey(KEY_DIVIDER)) {
            throw new IllegalArgumentException("Use start activity pattern");
        }
        boolean isHorizontal = args.getBoolean(KEY_HORIZONTAL);
        boolean hasDivider = args.getBoolean(KEY_DIVIDER);

        // check is horizontal recycler view is wished
        int orientation = LinearLayoutManager.VERTICAL;
        if (isHorizontal) {
            orientation = LinearLayoutManager.HORIZONTAL;
        }

        // simulate data
        this.models = initData(hasDivider);

        RecyclerView recyclerView = ((RecyclerView) findViewById(R.id.activity_drag_recycler_view));
        mPagerLayoutManager = new HorizontalPagerLayoutManager(this, recyclerView, false);
        mPagerLayoutManager.setOnPageChangeListener(this);
        recyclerView.setLayoutManager(mPagerLayoutManager);
        recyclerView.setHasFixedSize(true);

        DummyAdapter adapter = new DummyAdapter(this.models);
        recyclerView.setAdapter(adapter);

        DragDropGesture.Builder builder = new DragDropGesture.Builder()
                .on(recyclerView)
                .apply(new DummyDragStrategy());

        if (isHorizontal) {
            builder.horizontal();
        }
        builder.build();
    }

    /**
     * Init internal model.
     *
     * @param hasDivider true id divider should be added.
     * @return true;
     */
    private ArrayList<DummyModel> initData(boolean hasDivider) {
        ArrayList<DummyModel> models = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            models.add(new DummyModel("Model " + i));
        }

        if (hasDivider) {
            models.get(2).isDivider = true;
            models.get(28).isDivider = true;
        }
        return models;
    }

    @Override
    public void onPageSelected(int position) {
        if (position < 5) {
            mPagerLayoutManager.enablePager(true);
        } else {
            mPagerLayoutManager.enablePager(false);
        }
    }

    /**
     * Dummy model used for each item.
     */
    private static class DummyModel {

        public String text;

        public boolean isDivider;

        public DummyModel(String text) {
            this.text = text;
            this.isDivider = false;
        }
    }

    /**
     * Dummy view holder.
     */
    private static class DummyViewHolder extends RecyclerView.ViewHolder {

        public TextView text;

        public DummyViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.dummy_item_text);

        }
    }

    /**
     * Dummy adapter.
     */
    private static class DummyAdapter extends SwappableAdapter<DummyModel, DummyViewHolder> {

        public DummyAdapter(ArrayList<DummyModel> items) {
            super(items);
        }

        @Override
        public DummyViewHolder onCreateViewHolder(ViewGroup viewGroup, final int position) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).
                    inflate(R.layout.dummy_item, viewGroup, false);
            return new DummyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(DummyViewHolder viewHolder, final int position) {
            DummyModel model = getItem(position);
            viewHolder.text.setText(model.text);
            if (model.isDivider) {
                viewHolder.text.setTextColor(Color.BLACK);
            } else {
                viewHolder.text.setTextColor(Color.WHITE);
            }
        }
    }

    /**
     * Dummy strategy used to disable drag on divider.
     */
    private class DummyDragStrategy extends DragStrategy {
        @Override
        public boolean isItemDraggable(int position) {
            DummyModel model = models.get(position);
            return !model.isDivider;
        }

        @Override
        public boolean isItemHoverable(int position) {
            DummyModel model = models.get(position);
            return !model.isDivider;
        }
    }
}
