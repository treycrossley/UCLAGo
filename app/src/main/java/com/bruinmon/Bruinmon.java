package com.bruinmon;

import android.location.Location;

import java.io.Serializable;
import java.util.*;

public class Bruinmon implements Serializable {

    /** Enumeration for type of the Bruinmon and its moves **/
    public enum Type {
        ROCK, PAPER, SCISSORS, NONE
    }

    /** Gets the name of a particular Bruinmon **/
    public String getName() {
        return name;
    }

    /** Gets the ID for the Bruinmon **/
    public int getId() {
        return id;
    }

    /** Sets the name of the Bruinmon **/
    public void setName(String name) {
        this.name = name;
    }

    /** Gets the image ID of a particular Bruinmon **/
    public int getImage() {
        return image;
    }

    /** Sets the image of the Bruinmon **/
    public void setImage(int image) {
        this.image = image;
    }

    /** Gets the description of a particular Bruinmon **/
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /** Gets the location description of a particular Bruinmon **/
    public String getLocationDescription() {
        return where;
    }

    /** Sets the location description of the Bruinmon **/
    public void setWhere(String where) {
        this.where = where;
    }

    /** Gets the type of a particular Bruinmon **/
    public Type getType() {
        return type;
    }

    /** Sets the type of the Bruinmon **/
    public void setType(Type type) {
        this.type = type;
    }

    /** Converts a Type to a String **/
    static public String convertTypeToString(Type type) {
        switch (type) {
            case ROCK :
                return "Rock Type";
            case PAPER :
                return "Paper Type";
            case SCISSORS :
                return "Scissors Type";
        }
        return "Typeless";
    }

    /** Gets the first move of a particular Bruinmon **/
    public Move getMove1() {
        return move1;
    }

    /** Sets the move1 of the Bruinmon **/
    public void setMove1(Move move1) {
        this.move1 = move1;
    }

    /** Gets the second move of a particular Bruinmon **/
    public Move getMove2() {
        return move2;
    }

    /** Sets the move2 of the Bruinmon **/
    public void setMove2(Move move2) {
        this.move2 = move2;
    }

    /** Gets the third move of a particular Bruinmon **/
    public Move getMove3() {
        return move3;
    }

    /** Sets the move3 of the Bruinmon **/
    public void setMove3(Move move3) {
        this.move3 = move3;
    }

    /** Gets the fourth move of a particular Bruinmon **/
    public Move getMove4() {
        return move4;
    }

    /** Sets the move4 of the Bruinmon **/
    public void setMove4(Move move4) {
        this.move4 = move4;
    }

    /** Gets the location a particular Bruinmon **/
    public Location getLocation() {
        if (latitude == null || longitude == null) {
            return null;
        } else {
            return createNewLocation(latitude, longitude);
        }
    }

    /** Gets the latitude of the Bruin's location **/
    public Double getLatitude() {
        return latitude;
    }

    /** Gets the longitude of the Bruin's location **/
    public Double getLongitude() { return longitude; }

    /** Sets the latitude of the Bruinmon **/
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /** Sets the longitude of the Bruinmon **/
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /** Gets the location radius a particular Bruinmon (in meters) **/
    public float getLocationRadius() {
        return locationRadius;
    }

    /** Sets the location of the Bruinmon **/
    public void setLocationRadius(float locationRadius) { this.locationRadius = locationRadius; }

    /** Returns a list containing all Bruinmon **/
    static List<Bruinmon> getAll() {
        if (bruinmon.size() < 1) createBruinmon();
        return Collections.unmodifiableList(bruinmon);
    }

    private static Location createNewLocation(double latitude, double longitude) {
        Location location = new Location("dummyprovider");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    /** Initializes the list of all Bruinmon **/
    private static void createBruinmon() {
        Bruinmon x = new Bruinmon();
        x.name = "Muscley Bruin";
        x.image = R.drawable.muscle_bruin;
        x.description = "Using its heavy muscles, it throws powerful punches that can send the victim clear over the horizon.";
        x.where = "John Wooden Center";
        x.type = Type.ROCK;
        x.move1 = new Move("Mega Punch", Type.ROCK);
        x.move2 = new Move("Full Nelson", Type.PAPER);
        x.move3 = new Move("Karate Chop", Type.SCISSORS);
        x.move4 = new Move("Throw", Type.NONE);
        x.latitude = 34.071321;
        x.longitude = -118.445434;
        x.locationRadius = 80;
        x.id = bruinmon.size();
        bruinmon.add(x);
        MainActivity.bruinDB.addMove(x.getMove1());
        MainActivity.bruinDB.addMove(x.getMove2());
        MainActivity.bruinDB.addMove(x.getMove3());
        MainActivity.bruinDB.addMove(x.getMove4());

        x = new Bruinmon();
        x.name = "Classic Bruin";
        x.image = R.drawable.classic_bruin;
        x.description = "An all-around fighter.";
        x.where = "Everywhere";
        x.type = Type.ROCK;
        x.move1 = new Move("Jab", Type.ROCK);
        x.move2 = new Move("Squeeze", Type.PAPER);
        x.move3 = new Move("Bite", Type.SCISSORS);
        x.move4 = new Move("Indian Burn", Type.NONE);
        x.latitude = null;
        x.longitude = null;
        x.locationRadius = Float.MAX_VALUE;
        x.id = bruinmon.size();
        bruinmon.add(x);
        MainActivity.bruinDB.addMove(x.getMove1());
        MainActivity.bruinDB.addMove(x.getMove2());
        MainActivity.bruinDB.addMove(x.getMove3());
        MainActivity.bruinDB.addMove(x.getMove4());

        x = new Bruinmon();
        x.name = "Cheerful Bruin";
        x.image = R.drawable.cheerful_bruin;
        x.description = "A cheerful bruin that would rather do an 8-clap than fight.";
        x.where = "Rose Bowl";
        x.type = Type.PAPER;
        x.move1 = new Move("8-Clap", Type.PAPER);
        x.move2 = new Move("High V", Type.SCISSORS);
        x.move3 = new Move("Kick", Type.ROCK);
        x.move4 = new Move("Cheer", Type.NONE);
        x.latitude = 34.161297;
        x.longitude = -118.167648;
        x.locationRadius = 600;
        bruinmon.add(x);
        MainActivity.bruinDB.addMove(x.getMove1());
        MainActivity.bruinDB.addMove(x.getMove2());
        MainActivity.bruinDB.addMove(x.getMove3());
        MainActivity.bruinDB.addMove(x.getMove4());

        x = new Bruinmon();
        x.name = "Big Baller Bruin";
        x.image = R.drawable.basketball_bruin;
        x.description = "Using its big baller skills, it can match any foe, on or off the court.";
        x.where = "Pauley Pavilion";
        x.type = Type.SCISSORS;
        x.move1 = new Move("Precise Shot", Type.SCISSORS);
        x.move2 = new Move("Slam Dunk", Type.ROCK);
        x.move3 = new Move("Dribble", Type.PAPER);
        x.move4 = new Move("Box-Out", Type.NONE);
        x.latitude = 34.070355;
        x.longitude = -118.446774;
        x.locationRadius = 100;
        bruinmon.add(x);
        MainActivity.bruinDB.addMove(x.getMove1());
        MainActivity.bruinDB.addMove(x.getMove2());
        MainActivity.bruinDB.addMove(x.getMove3());
        MainActivity.bruinDB.addMove(x.getMove4());

        // TODO: If you add more Bruinmon, always add them below the previous ones (do not re-order them)
    }

    /** Adds a Bruinmon to the list of owned Bruinmon and returns false if the Bruinmon was already captured before **/
    static boolean captureBruinmon(Bruinmon bruinmon, MoveDBOperater bruinmonDb) {
        List<Bruinmon> ownedBruinmon = bruinmonDb.getAllBruinmons();
        for(Bruinmon mon: ownedBruinmon){
            if(mon.getName().equals(bruinmon.getName())){
                return false;
            }
        }
        bruinmonDb.addBruinmon(bruinmon);
        return true;
    }

    private String name;
    private int image;
    private String description;
    private String where;
    private Type type;
    private Move move1;
    private Move move2;
    private Move move3;
    private Move move4;
    private int id;
    private Double latitude;
    private Double longitude;
    private float locationRadius;

    private static List<Bruinmon> bruinmon = new ArrayList<Bruinmon>();
}
