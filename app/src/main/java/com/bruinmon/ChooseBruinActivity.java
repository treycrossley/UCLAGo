package com.bruinmon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class ChooseBruinActivity extends AppCompatActivity {

    private ListView listView;
    private BruinListAdapter myBruinmon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_bruin);

        listView = findViewById(R.id.choose_bruins);
        myBruinmon = new BruinListAdapter(new ArrayList<Bruinmon>(), getApplicationContext());
        listView.setAdapter(myBruinmon);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bruinmon bruinmon = myBruinmon.getItem(position);
                Intent intent = new Intent(getApplicationContext(), BattleActivity.class);
                intent.putExtra("is_ai_battle", getIntent().getBooleanExtra("is_ai_battle", false));
                intent.putExtra("is_hosting", getIntent().getBooleanExtra("is_hosting", false));
                intent.putExtra("opponent_device", getIntent().getParcelableExtra("opponent_device"));
                intent.putExtra("player_bruinmon", bruinmon);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        myBruinmon.clear();
        myBruinmon.addAll(MainActivity.bruinDB.getAllBruinmons());
    }
}
