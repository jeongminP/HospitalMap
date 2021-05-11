package com.example.hospitalmap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListActivity extends Activity {
    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;
    ArrayList<HospitalItem> hospitalItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Intent intent = getIntent();
        hospitalItemList = (ArrayList<HospitalItem>) intent.getSerializableExtra("hospitalItemList");

        TextView titleTextView = findViewById(R.id.list_title_textview);
        titleTextView.setText(intent.getStringExtra("emdongName") + " " + intent.getStringExtra("deptName") + " : " + hospitalItemList.size() + "건");

        ImageButton backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.list_recycler_view);
        adapter = new RecyclerViewAdapter(ListActivity.this, hospitalItemList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
