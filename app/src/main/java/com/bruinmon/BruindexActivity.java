package com.bruinmon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class BruindexActivity extends AppCompatActivity {

    private ListView listView;
    private BruinListAdapter bruindex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bruindex);

        ArrayList<Bruinmon> allBruins = new ArrayList<Bruinmon>(Bruinmon.getAll());

        listView = findViewById(R.id.bruindex);
        bruindex = new BruinListAdapter(allBruins, getApplicationContext());
        listView.setAdapter(bruindex);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bruinmon bruinmon = bruindex.getItem(position);
                Intent intent = new Intent(getApplicationContext(), BruinInfoActivity.class);
                intent.putExtra("bruinmon", bruinmon);
                startActivity(intent);
            }
        });
    }
}
