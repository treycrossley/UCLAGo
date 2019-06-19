package com.bruinmon;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.*;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private ListView listView;
    private BruinListAdapter nearbyBruinmon;
    public static MoveDBOperater bruinDB;

    private LocationManager locationManager;

    @Override
    public void onLocationChanged(Location location) {
        nearbyBruinmon.clear();
        for (Bruinmon bruinmon : Bruinmon.getAll()) {
            Location bruinmonLocation = bruinmon.getLocation();
            if (bruinmonLocation == null || location.distanceTo(bruinmonLocation) < bruinmon.getLocationRadius()) {
                nearbyBruinmon.add(bruinmon);
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(MainActivity.this, "Please enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bruinDB = new MoveDBOperater(this);
        bruinDB.open();

        listView = findViewById(R.id.bruins_nearby);
        nearbyBruinmon = new BruinListAdapter(new ArrayList<Bruinmon>(), getApplicationContext());
        for (Bruinmon bruinmon : Bruinmon.getAll()) {
            Location bruinmonLocation = bruinmon.getLocation();
            if (bruinmonLocation == null) {
                nearbyBruinmon.add(bruinmon);
            }
        }
        listView.setAdapter(nearbyBruinmon);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bruinmon bruinmon = nearbyBruinmon.getItem(position);
                if (Bruinmon.captureBruinmon(bruinmon, bruinDB)) {
                    Toast.makeText(getApplicationContext(), bruinmon.getName() + " captured!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "You already own " + bruinmon.getName(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000, 0, this);
        } catch (SecurityException e) {
            Toast.makeText(getApplicationContext(), "Cannot get location", Toast.LENGTH_SHORT).show();
        }
    }

    /** Called when the user touches the about icon in the top right of the main menu **/
    public void showInfoBox(View view) {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle("About Bruinmon");
        dialog.setMessage(
                "A fun game made for CS M117 at UCLA, in order to help us learn about GPS, Bluetooth, and general networking technologies\n" +
                "\n" +
                "Authors\n" +
                "    Uday Alla\n" +
                "    Trey Crossley\n" +
                "    Brandon Haffen\n" +
                "    Nicholas Turk"
        );
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );
        dialog.show();
    }

    /** Called when the user touches the My Bruins button **/
    public void navigateMyBruins(View view) {
        Intent intent = new Intent(this, MyBruinsActivity.class);
        startActivity(intent);
    }

    /** Called when the user touches the Bruindex button **/
    public void navigateBruindex(View view) {
        Intent intent = new Intent(this, BruindexActivity.class);
        startActivity(intent);
    }

    /** Called when the user touches the Battle button **/
    public void navigateBattle(View view) {
        Intent intent = new Intent(this, PreBattleActivity.class);
        startActivity(intent);
    }
}
