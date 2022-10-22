package com.weto.huduino;

import android.location.Location;
import android.util.Log;

public class CLocation extends Location {
    private boolean useMetricUnits;

    public CLocation(Location location, boolean useMetricUnits){
        super(location);
        this.useMetricUnits = useMetricUnits;
    }
    public boolean getUseMetricUnits() {
        return this.useMetricUnits;
    }
    public void setUseMetricUnits(boolean useMetricUnits) {
        this.useMetricUnits = useMetricUnits;
    }

    @Override
    public float distanceTo(Location dest) {
        float nDistance = super.distanceTo(dest);

        if(this.getUseMetricUnits()){

            nDistance *= 3.28083989501312f;
        }
        return nDistance;
    }
    @Override
    public double getAltitude() {
        double nAltitude = super.getAltitude();
        if(this.getUseMetricUnits()){
            nAltitude *= 3.28083989501312d;
        }
        return nAltitude;
    }
    @Override
    public float getSpeed() {
        float nSpeed = super.getSpeed() * 3.6f;
        if(!this.getUseMetricUnits()){
            //Conversion from m/s to M/h
            nSpeed =super.getSpeed() * 2.23693629f;
        }
        return nSpeed;
    }

    @Override
    public float getAccuracy() {
        double nAccuracy = super.getAccuracy();
        if(this.getUseMetricUnits()){
            nAccuracy *= 3.28083989501312d;
        }
        return (float) nAccuracy;
    }
}
