package com.example.hospitalmap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

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
        setTitle(intent.getStringExtra("deptName"));

        recyclerView = findViewById(R.id.list_recycler_view);
        adapter = new RecyclerViewAdapter(ListActivity.this, hospitalItemList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
