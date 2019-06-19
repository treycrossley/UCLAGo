package com.bruinmon;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MoveDBHandler extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "moves.db";
    private static final int DATABASE_VERSION = 21;

    public static final String TABLE_MOVES = "moves";
    public static final String MOVE_NAME = "name";
    public static final String MOVE_TYPE = "type";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_MOVES + " (" +
                    MOVE_NAME + " TEXT PRIMARY KEY , " +
                    MOVE_TYPE + " TEXT " +
                    ")";

    public static final String TABLE_BRUINMON = "bruinmon";
    public static final String BRUINMON_NAME = "name";
    public static final String BRUINMON_IMAGE = "image";
    public static final String BRUINMON_DESCRIPTION = "description";
    public static final String BRUINMON_WHERE = "locationdesc";
    public static final String BRUINMON_TYPE = "type";
    public static final String BRUINMON_MOVE1 = "move1";
    public static final String BRUINMON_MOVE2 = "move2";
    public static final String BRUINMON_MOVE3 = "move3";
    public static final String BRUINMON_MOVE4 = "move4";
    public static final String BRUINMON_LOCATION_LATITUDE = "locationlatitude";
    public static final String BRUINMON_LOCATION_LONGITUDE = "locationlongitude";
    public static final String BRUINMON_RADIUS = "locationradius";

    private static final String TABLE_CREATE2 =
            "CREATE TABLE " + TABLE_BRUINMON + " (" +
                    BRUINMON_NAME + " TEXT PRIMARY KEY," +
                    BRUINMON_IMAGE + " TEXT," +
                    BRUINMON_DESCRIPTION + " TEXT," +
                    BRUINMON_WHERE + " TEXT," +
                    BRUINMON_TYPE + " TEXT," +
                    BRUINMON_MOVE1+ " TEXT," +
                    BRUINMON_MOVE2 + " TEXT," +
                    BRUINMON_MOVE3 + " TEXT," +
                    BRUINMON_MOVE4 + " TEXT," +
                    BRUINMON_LOCATION_LATITUDE + " TEXT," +
                    BRUINMON_LOCATION_LONGITUDE + " TEXT," +
                    BRUINMON_RADIUS + " TEXT" +
                    ")";

    public MoveDBHandler(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE2);
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_MOVES);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_BRUINMON);
        onCreate(db);
    }
}
