package com.bruinmon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class BruinInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bruin_info);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        Bruinmon bruinmon = (Bruinmon)intent.getSerializableExtra("bruinmon");
        Move move1 = bruinmon.getMove1();
        Move move2 = bruinmon.getMove2();
        Move move3 = bruinmon.getMove3();
        Move move4 = bruinmon.getMove4();
        ((TextView)findViewById(R.id.bruin_name)).setText(bruinmon.getName());
        ((ImageView)findViewById(R.id.bruin_image)).setImageResource(bruinmon.getImage());
        ((TextView)findViewById(R.id.bruin_type)).setText(Bruinmon.convertTypeToString(bruinmon.getType()));
        ((TextView)findViewById(R.id.bruin_desc)).setText(bruinmon.getDescription());
        ((TextView)findViewById(R.id.bruin_where)).setText(bruinmon.getLocationDescription());
        ((TextView)findViewById(R.id.bruin_move1)).setText(move1.getName() + " - " + Bruinmon.convertTypeToString(move1.getType()) + " Move");
        ((TextView)findViewById(R.id.bruin_move2)).setText(move2.getName() + " - " + Bruinmon.convertTypeToString(move2.getType()) + " Move");
        ((TextView)findViewById(R.id.bruin_move3)).setText(move3.getName() + " - " + Bruinmon.convertTypeToString(move3.getType()) + " Move");
        ((TextView)findViewById(R.id.bruin_move4)).setText(move4.getName() + " - " + Bruinmon.convertTypeToString(move4.getType()) + " Move");
    }
}
