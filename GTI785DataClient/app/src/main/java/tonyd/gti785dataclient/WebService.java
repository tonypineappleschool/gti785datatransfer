package tonyd.gti785dataclient;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by tonyd on 5/28/2017.
 */

public class WebService extends Service {

    private final IBinder mBinder = new MyBinder();
    private Server server;
    private WebServiceCallbacks webServiceCallbacks;
    private LocalBroadcastManager broadcaster;

    private Location deviceLocation;

    @Override
    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                deviceLocation = location;
                if (server.getLbq() != null){
                    try {
                        server.getLbq().put(deviceLocation);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        server = new Server(this, getApplicationContext());
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Service listening on port " + server.getListeningPort(), Toast.LENGTH_LONG).show();
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        server.closeAllConnections();
        server.stop();
    }

    public LocalBroadcastManager getBroadcaster() {
        return broadcaster;
    }

    public class MyBinder extends Binder {
        WebService getService() {
            return WebService.this;
        }
    }

    public WebServiceCallbacks getCallbacks(){
        return webServiceCallbacks;
    }

    public void setCallbacks(WebServiceCallbacks callbacks) {
        webServiceCallbacks = callbacks;
    }

}
