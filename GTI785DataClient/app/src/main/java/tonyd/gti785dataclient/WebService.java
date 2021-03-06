package tonyd.gti785dataclient;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Formatter;
import android.widget.Toast;

import java.io.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by tonyd on 5/28/2017.
 */

public class WebService extends Service {

    private static final int REQUEST_LOCATION = 1;
    private final IBinder mBinder = new MyBinder();
    private ServerLocation serverLocation;
    private ServerFiles serverFiles;
    private WebServiceCallbacks webServiceCallbacks;
    private LocalBroadcastManager broadcaster;
    private LocationManager locationManager;
    private LocationListener locationListener;
    protected ArrayList<FileSync> fileSyncs;
    private  ArrayList<File> allFiles;
    public Location getDeviceLocation() {
        return deviceLocation;
    }

    public void setDeviceLocation(Location deviceLocation) {
        this.deviceLocation = deviceLocation;
    }

    private Location deviceLocation;

    @Override
    public void onCreate() {
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        serverLocation = new ServerLocation(this, getApplicationContext(), ipAddress);
        serverFiles = new ServerFiles(this, getApplicationContext(), ipAddress);
        try {
            serverLocation.start();
            serverFiles.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Service listening on " + ipAddress + ":" + serverLocation.getListeningPort(), Toast.LENGTH_LONG).show();

        broadcaster = LocalBroadcastManager.getInstance(this);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                    deviceLocation = location;
                    Toast.makeText(getApplicationContext(), "Location Web Service Updated", Toast.LENGTH_LONG).show();
                    Pair.Location customLocation = new Pair.Location(location.getLongitude(), location.getLatitude());
//                    if (serverLocation.getLbq() != null) {
//                        try {
//                            serverLocation.getLbq().put(customLocation);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        int permissionAccessFineLocation = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionAccessCoarseLocation = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        // Register the listener with the Location Manager to receive location updates
        if ( permissionAccessFineLocation != PackageManager.PERMISSION_GRANTED && permissionAccessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

        deviceLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        fileSyncs = new ArrayList<>();
        allFiles = (ArrayList<File>) getListFiles(new File(Environment.getExternalStorageDirectory().getAbsolutePath()));
        Timer time = new Timer();
        SyncTask st = new SyncTask(); // Instantiate SheduledTask class
        time.schedule(st, 0, 5000); // Create Repetitively task for every 1 secs

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serverLocation.closeAllConnections();
        serverLocation.stop();
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

    public class SyncTask extends TimerTask {
        public void run() {
            fileSyncs = new ArrayList<>();
            ArrayList<File> currentFiles = (ArrayList<File>) getListFiles(new File(Environment.getExternalStorageDirectory().getAbsolutePath()));
            for (File f : currentFiles){
                if (!allFiles.contains(f)){
                    fileSyncs.add(new FileSync(f.getAbsolutePath(), new Date(f.lastModified()), (int) f.length(), FileSync.Status.ADDED));
                }
            }
            for (File f : allFiles){
                if (!currentFiles.contains(f)){
                    fileSyncs.add(new FileSync(f.getAbsolutePath(), new Date(f.lastModified()), (int) f.length(), FileSync.Status.DELETED));
                }
            }
            allFiles.clear();
            allFiles.addAll(currentFiles);
            if (fileSyncs.size() > 0 ){
                try {
                    if (serverLocation.getLbq() != null) {
                        serverLocation.getLbq().put(fileSyncs);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
                inFiles.add(file);
            }
        }
        return inFiles;
    }
}
