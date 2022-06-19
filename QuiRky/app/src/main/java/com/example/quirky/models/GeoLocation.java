/*
 * Copyright (c) 2022. CMPUT301W22T29
 * Subject to MIT License
 * See full terms at https://github.com/CMPUT301W22T29/QuiRky/blob/main/LICENSE
 */

package com.example.quirky.models;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import org.osmdroid.util.GeoPoint;

import java.lang.Math;

/**
 * Adapter class (OO Design Pattern) to allow Firebase API and OSMDroid API to work together easily
 * Represents a location on the earth. Does not account for height/altitude
 */
public class GeoLocation implements Parcelable {

    // These are the exact coordinates of the location
    private double exactLat;
    private double exactLong;

    // Coordinates above 180* are not valid
    private static final int limit = 180;

    // These are approximate coordinates of the location, which are helpful for getting nearby points from FireStore using the Query class
    private int approxLat;
    private int approxLong;

    // An optional description of the location. ex. an address or area description
    private String description;

    /**
     * Empty constructor for FireStore
     */
    public GeoLocation() {
        exactLat = 0;
        exactLong = 0;

        approxLat = 0;
        approxLong = 0;

        description = "";
    }

    /**
     * Initialize GeoLocation with a latitude and longitude
     */
    public GeoLocation(double exactLat, double exactLong) {
        assert( Math.abs(exactLat) < limit && Math.abs(exactLong) < limit) : "Those are invalid coordinates";
        this.exactLat = exactLat;
        this.exactLong = exactLong;

        approxLat = (int) exactLat;
        approxLong = (int) exactLong;

        description = "";
    }

    /**
     * Initialize a GeoLocation with a latitude, longitude, and a description
     */
    public GeoLocation(double exactLat, double exactLong, String description) {
        assert( Math.abs(exactLat) < limit && Math.abs(exactLong) < limit) : "Those are invalid coordinates";
        this.exactLat = exactLat;
        this.exactLong = exactLong;

        approxLat = (int) exactLat;
        approxLong = (int) exactLong;

        this.description = description;
    }

    /**
     * Initialize a Geolocation from an android.util.location
     * @param location The location object to initialize from
     */
    public GeoLocation(Location location) {
        this.exactLat = location.getLatitude();
        this.exactLong = location.getLongitude();

        this.approxLat = (int) exactLat;
        this.approxLong = (int) exactLong;

        this.description = "";
    }

    /**
     * Getter for exact latitude
     */
    public double getExactLat() {
        return exactLat;
    }

    /**
     * Getter for exact longitude
     */
    public double getExactLong() {
        return exactLong;
    }

    /**
     * Setter for exact latitude
     */
    public void setExactLat(double exactLat) {
        assert Math.abs(exactLat) < limit : "That is not a valid coordinate!";
        this.exactLat = exactLat;
        this.approxLat = (int) exactLat;
    }

    /**
     * Getter for exact longitude
     */
    public void setExactLong(double exactLong) {
        assert Math.abs(exactLong) < limit : "That is not a valid coordinate!";
        this.exactLong = exactLong;
        this.approxLong = (int) exactLong;
    }

    /**
     * Getter for approximate latitude
     */
    public int getApproxLat() {
        return approxLat;
    }

    /**
     * Getter for approximate longitude
     */
    public int getApproxLong() {
        return approxLong;
    }

    /**
     * Getter for location description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter for exact longitude
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Typecast this GeoLocation into a GeoPoint for OSMDroid tools
     * @return A GeoPoint object representing the same point as this GeoLocation
     */
    public GeoPoint toGeoPoint() {
        return new GeoPoint(exactLat, exactLong, 0);
    }

    /**
     * This method should round a Latitude or Longitude to an accuracy where moving only a couple meters will be considered the same GeoLocation
     * We want that scanning a QRCode a second time in a slightly different spot will not produce a new GeoLocation with nearly the same data as the first.
     * It is important to understand why this rounded coordinates is separate from the Approx coordinates already stored:
     *      The Approx coordinates stored in this class should round to a distance of around 20-70km, so that it becomes easy to access, from the
     *      database, all QRCodes within a long distance of the user, when they open the map activity. This is a separate need from the one stated above.
     * Perhaps ApproxLat can be changed away from the Integer Floor, which suffers from the below problems, to instead use this method, but with an optional parameter
     *      to round to a much larger margin, that results in a 5-10km distance rather than a 2-5 metre distance.
     *      Food for though: Does approxLat even need to be stored in a GeoLocation? It does not need to be used within the app, it only exists to simplify fetching from
     *      Firebase, which, BTW, locations are now stored in a separate location from the rest of QRCode data, thanks to Sean's work on the Map stuff, so does a QRCode even
     *      need to hold a list of GeoLocations in it right now? Anyways, this is beyond this scope of this method, and should probably be documented elsewhere. Back to the method!
     *
     * Rounding the Lats/Longs is more complicated than it sounds, I will document my current issue here:
     *      - What we want is to round to a degree of accuracy in the scale of metres, but what we have is Longitude and Latitude
     *      - We could round to an arbitrary decimal digit, but this has a glaring flaw:
     *              1° change in Latitude is a different distance depending on the Longitude, and a
     *              1° change in Longitude is a different distance depending on the Latitude
     *              At the equator, 1° change in Longitude is ~70km, which shrinks to 0m at the Poles
     *         -> The rounding distance would always be inconsistent, and would depend on the position of the user on the globe
     *      - The solution is to create a formula that rounds both Latitude and Longitude at the same time, and rounds them to various degrees of accuracy
     *              depending on the other given value. This method would round a significantly more accurate Longitude at the Equator,
     *              and would round Longitude to a larger margin in far North and far South locations
     *              The same system would be applied to rounding Latitude.
     * TODO: Think about the above documentation, find a solution to all the raised concerns, and then remove the unnecessary docs
     */
    private void round(double Lat, double Long) {
        this.exactLat = /* round( */ Lat;
        this.exactLong = /* round( */ Long;
    }

    /* - - - - - - Parcelable implementation - - - - - - - */
    protected GeoLocation(Parcel in) {
        exactLat = in.readDouble();
        exactLong = in.readDouble();
        approxLat = in.readInt();
        approxLong = in.readInt();
        description = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(exactLat);
        dest.writeDouble(exactLong);
        dest.writeInt(approxLat);
        dest.writeInt(approxLong);
        dest.writeString(description);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GeoLocation> CREATOR = new Creator<GeoLocation>() {
        @Override
        public GeoLocation createFromParcel(Parcel in) {
            return new GeoLocation(in);
        }

        @Override
        public GeoLocation[] newArray(int size) {
            return new GeoLocation[size];
        }
    };
}
