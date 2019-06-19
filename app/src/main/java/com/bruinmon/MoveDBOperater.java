package com.bruinmon;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.text.method.MovementMethod;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class MoveDBOperater {
    public static final String LOGTAG = "MOVE_MNGMNT_SYS";

    SQLiteOpenHelper dbhandler;
    SQLiteDatabase database;

    private static final String[] allColumns = {
            MoveDBHandler.MOVE_NAME,
            MoveDBHandler.MOVE_TYPE
    };

    private static final String[] bruinAllColumns = {
            MoveDBHandler.BRUINMON_NAME,
            MoveDBHandler.BRUINMON_IMAGE,
            MoveDBHandler.BRUINMON_DESCRIPTION,
            MoveDBHandler.BRUINMON_WHERE,
            MoveDBHandler.BRUINMON_TYPE,
            MoveDBHandler.BRUINMON_MOVE1,
            MoveDBHandler.BRUINMON_MOVE2,
            MoveDBHandler.BRUINMON_MOVE3,
            MoveDBHandler.BRUINMON_MOVE4,
            MoveDBHandler.BRUINMON_LOCATION_LATITUDE,
            MoveDBHandler.BRUINMON_LOCATION_LONGITUDE,
            MoveDBHandler.BRUINMON_RADIUS
    };

    public MoveDBOperater(Context context){
        dbhandler = new MoveDBHandler(context);
    }

    public void open(){
        Log.i(LOGTAG,"Database Opened");
        database = dbhandler.getWritableDatabase();
    }

    public void close(){
        Log.i(LOGTAG, "Database Closed");
        dbhandler.close();
    }

    public void addBruinmon(Bruinmon bruinmon){
        ContentValues values  = new ContentValues();
        values.put(MoveDBHandler.BRUINMON_NAME, bruinmon.getName());
        values.put(MoveDBHandler.BRUINMON_IMAGE, bruinmon.getImage() + "");
        values.put(MoveDBHandler.BRUINMON_DESCRIPTION, bruinmon.getDescription());
        values.put(MoveDBHandler.BRUINMON_WHERE, bruinmon.getLocationDescription());
        String type;
        if(bruinmon.getType() == Bruinmon.Type.NONE){
            type = "NONE";
        }
        else if(bruinmon.getType() == Bruinmon.Type.ROCK){
            type = "ROCK";
        }
        else if (bruinmon.getType() == Bruinmon.Type.PAPER){
            type = "PAPER";
        }
        else {
            type  = "SCISSORS";
        }
        values.put(MoveDBHandler.BRUINMON_TYPE, type);
        values.put(MoveDBHandler.BRUINMON_MOVE1, bruinmon.getMove1().getName());
        values.put(MoveDBHandler.BRUINMON_MOVE2, bruinmon.getMove2().getName());
        values.put(MoveDBHandler.BRUINMON_MOVE3, bruinmon.getMove3().getName());
        values.put(MoveDBHandler.BRUINMON_MOVE4, bruinmon.getMove4().getName());
        values.put(MoveDBHandler.BRUINMON_LOCATION_LATITUDE, bruinmon.getLatitude());
        values.put(MoveDBHandler.BRUINMON_LOCATION_LONGITUDE, bruinmon.getLongitude());
        values.put(MoveDBHandler.BRUINMON_RADIUS, bruinmon.getLocationRadius());
        database.insert(MoveDBHandler.TABLE_BRUINMON,null,values);

    }

    public void addMove(Move move){
        ContentValues values  = new ContentValues();
        String type;
        if(move.getType() == Bruinmon.Type.NONE){
            type = "NONE";
        }
        else if(move.getType() == Bruinmon.Type.ROCK){
            type = "ROCK";
        }
        else if (move.getType() == Bruinmon.Type.PAPER){
            type = "PAPER";
        }
        else {
            type  = "SCISSORS";
        }
        values.put(MoveDBHandler.MOVE_TYPE, type);
        values.put(MoveDBHandler.MOVE_NAME,move.getName());
        database.insert(MoveDBHandler.TABLE_MOVES,null,values);

    }

    public Bruinmon getBruinmon(String name) {

        Cursor cursor = database.query(MoveDBHandler.TABLE_BRUINMON,bruinAllColumns,MoveDBHandler.BRUINMON_NAME + "=?",new String[]{String.valueOf(name)},null,null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Bruinmon b = new Bruinmon();
        Bruinmon.Type type;
        if(cursor.getString(4).equals("NONE")){
            type = Bruinmon.Type.NONE;
        }
        else if(cursor.getString(4).equals("ROCK")){
            type = Bruinmon.Type.ROCK;
        }
        else if (cursor.getString(4).equals("PAPER")){
            type = Bruinmon.Type.PAPER;
        }
        else {
            type  = Bruinmon.Type.SCISSORS;
        }
        Move move1 = this.getMove(cursor.getString(5));
        Move move2 = this.getMove(cursor.getString(6));
        Move move3 = this.getMove(cursor.getString(7));
        Move move4 = this.getMove(cursor.getString(8));

        b.setName(cursor.getString(0));
        b.setImage(Integer.parseInt(cursor.getString(1)));
        b.setDescription(cursor.getString(2));
        b.setWhere(cursor.getString(3));
        b.setType(type);
        b.setMove1(move1);
        b.setMove2(move2);
        b.setMove3(move3);
        b.setMove4(move4);
        b.setLatitude(cursor.getDouble(9));
        b.setLongitude(cursor.getDouble(10));
        b.setLocationRadius(cursor.getFloat(11));
        return b;
    }

    public Move getMove(String name) {

        Cursor cursor = database.query(MoveDBHandler.TABLE_MOVES,allColumns,MoveDBHandler.MOVE_NAME + "=?",new String[]{String.valueOf(name)},null,null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Bruinmon.Type type;
        if(cursor.getString(1).equals("NONE")){
            type = Bruinmon.Type.NONE;
        }
        else if(cursor.getString(1).equals("ROCK")){
            type = Bruinmon.Type.ROCK;
        }
        else if (cursor.getString(1).equals("PAPER")){
            type = Bruinmon.Type.PAPER;
        }
        else {
            type  = Bruinmon.Type.SCISSORS;
        }
        Move move = new Move(cursor.getString(0), type);
        return move;
    }

    public List<Move> getAllMoves() {

        Cursor cursor = database.query(MoveDBHandler.TABLE_MOVES,allColumns,null,null,null, null, null);

        List<Move> moves = new ArrayList<>();
        if(cursor.getCount() > 0){
            while(cursor.moveToNext()){
                Bruinmon.Type type;
                if(cursor.getString(cursor.getColumnIndex(MoveDBHandler.MOVE_TYPE)).equals("NONE")){
                    type = Bruinmon.Type.NONE;
                }
                else if(cursor.getString(cursor.getColumnIndex(MoveDBHandler.MOVE_TYPE)).equals("ROCK")){
                    type = Bruinmon.Type.ROCK;
                }
                else if (cursor.getString(cursor.getColumnIndex(MoveDBHandler.MOVE_TYPE)).equals("PAPER")){
                    type = Bruinmon.Type.PAPER;
                }
                else {
                    type  = Bruinmon.Type.SCISSORS;
                }

                Move move = new Move(cursor.getString(cursor.getColumnIndex(MoveDBHandler.MOVE_NAME)), type);
                moves.add(move);
            }
        }
        // return All moves
        return moves;
    }

    public List<Bruinmon> getAllBruinmons() {
        Cursor cursor = database.query(MoveDBHandler.TABLE_BRUINMON,bruinAllColumns,null,null,null, null, null);

        List<Bruinmon> bruinmons = new ArrayList<>();
        if(cursor.getCount() > 0){
            while(cursor.moveToNext()){
                Bruinmon b = new Bruinmon();
                Bruinmon.Type type;
                if(cursor.getString(cursor.getColumnIndex(MoveDBHandler.BRUINMON_TYPE)).equals("NONE")){
                    type = Bruinmon.Type.NONE;
                }
                else if(cursor.getString(cursor.getColumnIndex(MoveDBHandler.BRUINMON_TYPE)).equals("ROCK")){
                    type = Bruinmon.Type.ROCK;
                }
                else if (cursor.getString(cursor.getColumnIndex(MoveDBHandler.BRUINMON_TYPE)).equals("PAPER")){
                    type = Bruinmon.Type.PAPER;
                }
                else {
                    type  = Bruinmon.Type.SCISSORS;
                }

                Move move1 = this.getMove(cursor.getString(cursor.getColumnIndex(MoveDBHandler.BRUINMON_MOVE1)));
                Move move2 = this.getMove(cursor.getString(cursor.getColumnIndex(MoveDBHandler.BRUINMON_MOVE2)));
                Move move3 = this.getMove(cursor.getString(cursor.getColumnIndex(MoveDBHandler.BRUINMON_MOVE3)));
                Move move4 = this.getMove(cursor.getString(cursor.getColumnIndex(MoveDBHandler.BRUINMON_MOVE4)));


                b.setName(cursor.getString(cursor.getColumnIndex(MoveDBHandler.BRUINMON_NAME)));
                b.setImage(Integer.parseInt(cursor.getString(cursor.getColumnIndex(MoveDBHandler.BRUINMON_IMAGE))));
                b.setDescription(cursor.getString(cursor.getColumnIndex(MoveDBHandler.BRUINMON_DESCRIPTION)));
                b.setWhere(cursor.getString(cursor.getColumnIndex(MoveDBHandler.BRUINMON_WHERE)));
                b.setType(type);
                b.setMove1(move1);
                b.setMove2(move2);
                b.setMove3(move3);
                b.setMove4(move4);
                b.setLatitude(cursor.getDouble(cursor.getColumnIndex(MoveDBHandler.BRUINMON_LOCATION_LATITUDE)));
                b.setLongitude(cursor.getDouble(cursor.getColumnIndex(MoveDBHandler.BRUINMON_LOCATION_LONGITUDE)));
                b.setLocationRadius(cursor.getFloat(cursor.getColumnIndex(MoveDBHandler.BRUINMON_RADIUS)));
                bruinmons.add(b);
            }
        }
        return bruinmons;
    }
    public void removeBruinmon(Bruinmon b) {
        database.delete(MoveDBHandler.TABLE_BRUINMON, MoveDBHandler.BRUINMON_NAME + "='" +  b.getName() +"'", null);
    }

    public void removeMove(Move move) {
        database.delete(MoveDBHandler.TABLE_MOVES, MoveDBHandler.MOVE_NAME + "='" + move.getName() + "'", null);
    }
}
