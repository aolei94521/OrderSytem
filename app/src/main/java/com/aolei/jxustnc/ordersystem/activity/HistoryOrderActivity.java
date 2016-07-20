package com.aolei.jxustnc.ordersystem.activity;

import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.aolei.jxustnc.ordersystem.R;
import com.aolei.jxustnc.ordersystem.fragment.MessageFragment;

public class HistoryOrderActivity extends AppCompatActivity {

    private Toolbar history_order_toolbar;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_order);
        history_order_toolbar = (Toolbar) findViewById(R.id.history_order_toolbar);
        history_order_toolbar.setTitle("历史订单");
        history_order_toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(history_order_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.history_fragment_container, new MessageFragment(true));
        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
