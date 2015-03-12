package com.netcosports.recyclergesture;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;


/**
 * Sample home.
 */
public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.main_activity_drag_horizontal).setOnClickListener(this);
        findViewById(R.id.main_activity_drag_vertical).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_activity_drag_horizontal:
                DragActivity.startActivity(this, true);
                break;
            case R.id.main_activity_drag_vertical:
                DragActivity.startActivity(this, false);
                break;
            default:
                break;
        }
    }
}
