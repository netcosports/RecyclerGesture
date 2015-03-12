package com.netcosports.recyclergesture;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.netcosports.recyclergesture.library.RecyclerArrayAdapter;
import com.netcosports.recyclergesture.library.drag.DragDropGesture;

import java.util.ArrayList;

/**
 * Activity used as sample for {@link com.netcosports.recyclergesture.library.drag.DragDropGesture}
 */
public class DragActivity extends ActionBarActivity {

    /**
     * Used to know if the recycler should be horizontal.
     */
    private static final String KEY_HORIZONTAL = "horizontal_orientation";

    /**
     * Start activity pattern.
     *
     * @param context    context used to start the activity.
     * @param horizontal true if the recycler view should be display horizontally.
     */
    public static void startActivity(Context context, boolean horizontal) {
        Intent i = new Intent(context, DragActivity.class);
        i.putExtra(KEY_HORIZONTAL, horizontal);
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag);

        Bundle args = getIntent().getExtras();
        if (args == null || !args.containsKey(KEY_HORIZONTAL)) {
            throw new IllegalArgumentException("Use start activity pattern");
        }
        boolean isHorizontal = args.getBoolean(KEY_HORIZONTAL);

        // check is horizontal recycler view is wished
        int orientation = LinearLayoutManager.VERTICAL;
        if (isHorizontal) {
            orientation = LinearLayoutManager.HORIZONTAL;
        }

        // simulate data
        ArrayList<DummyModel> models = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            models.add(new DummyModel("Model " + i));
        }

        RecyclerView recyclerView = ((RecyclerView) findViewById(R.id.activity_drag_recycler_view));
        recyclerView.setLayoutManager(new LinearLayoutManager(this, orientation, false));
        recyclerView.setHasFixedSize(true);

        DummyAdapter adapter = new DummyAdapter(models);
        recyclerView.setAdapter(adapter);

        DragDropGesture.Builder builder = new DragDropGesture.Builder().on(recyclerView).with(adapter);
        if (isHorizontal) {
            builder.horizontal();
        }
        builder.build();
    }

    /**
     * Dummy model used for each item.
     */
    private static class DummyModel {

        public String text;

        public DummyModel(String text) {
            this.text = text;
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
    private static class DummyAdapter extends RecyclerArrayAdapter<DummyModel, DummyViewHolder> {

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
        }
    }
}
