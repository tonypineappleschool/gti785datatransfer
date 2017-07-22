package tonyd.gti785dataclient;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements WebServiceCallbacks {

    private static final int REQUEST_WRITE_STORAGE = 2;
    private static final int SORTNAME = 100;
    private static final int SORTDIST = 101;
    private static final int SORTDATE = 102;
    private List<Pair> pairs;
    private ViewGroup linearLayout;
    private PairsAdapter pairsAdapter;
    protected Location deviceLocation;
    private HashMap<Pair, ArrayList<FileSync>> fileSyncHashMap;

    private SharedPreferences sharedPreferences;

    private SharedPreferences.Editor editor;

    /* For sending requests to the server */
    private RequestAsyncTask request;

    /* For receiving broadcast messages from AsynctaskRequest */
    private BroadcastReceiver receiver;

    private static final int REQUEST_LOCATION = 1;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 2;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 3;
    private String ipAddress;
    private String port;

    /* Web Service */
    private WebService webService;
    public static int WHITE = 0xFFFFFFFF;
    public static int BLACK = 0xFF000000;
    private HashSet<String> mySet;
    private RequestAsyncTaskFolderContent folderRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = sharedPreferences.getString(Command.LISTPAIRS, null);
        linearLayout = (ViewGroup) findViewById(R.id.linear_layout_main);
        if (json == null){
            pairs = new ArrayList<>();
        } else {
            Type type = new TypeToken<ArrayList<Pair>>() {
            }.getType();
            pairs = gson.fromJson(json, type);
        }
        pairsAdapter = new PairsAdapter(this, pairs);
        pairsAdapter.setActivity(this);

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                deviceLocation = location;
                Toast.makeText(getApplicationContext(), "Location Updated", Toast.LENGTH_LONG).show();
                pairsAdapter.notifyDataSetChanged();
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
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
        deviceLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        checkAllPermissions();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            deviceLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }


    }

    @Override
    public void onStart(){
        super.onStart();
        mySet = new HashSet<>();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String id = intent.getStringExtra(Command.NOTIFID);
                if (mySet.contains(id))
                    return;
                mySet.add(id);
                String command = intent.getStringExtra(Command.COMMAND);
                switch (command) {
                    case Command.DISCONNECTED:
                        int pairID = intent.getIntExtra("pairID", -1);
                        Pair pair = findPairById(pairID);
                        if (pair != null) {
                            pair.setStatus(false);
                            pairsAdapter.notifyDataSetChanged();
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    sendRequest(pairID, pair.getIp(), Integer.toString(pair.getPort()), Command.POLL, "");
                                }
                            }, 2000);

                        }
                        break;
                    case Command.CONNECTED:
                        pairID = intent.getIntExtra("pairID", -1);
                        pair = findPairById(pairID);
                        if (pair != null) {
                            pair.setStatus(true);
                            pairsAdapter.notifyDataSetChanged();
                        }
                        break;
                    case Command.LOCATION:
                        pairID = intent.getIntExtra("pairID", -1);
                        pair = findPairById(pairID);
                        if (pair != null) {
                            Pair.Location location = intent.getParcelableExtra(Command.LOCATION);
                            pair.getLocation().setLatitude(location.getLatitude());
                            pair.getLocation().setLongitude(location.getLongitude());
                            pairsAdapter.notifyDataSetChanged();
                        }
                        break;
                    case Command.FOLDERCONTENT:
                        // Display files
                        pairID = intent.getIntExtra("pairID", -1);
                        pair = findPairById(pairID);
                        if (pair != null) {
                            FolderContent folderContent = intent.getParcelableExtra(Command.FOLDERCONTENT);
                            findPairById(pairID).setLastAccessed(new Date());
                            startFolderContentActivity(folderContent, pairID);
                        }
                        break;
                    case Command.SYNC:
                        pairID = intent.getIntExtra("pairID", -1);
                        pair = findPairById(pairID);
                        ArrayList<FileSync> fileSyncs = intent.getParcelableArrayListExtra(Command.SYNC);
                        Toast.makeText(getApplicationContext(), "Modifications detected", Toast.LENGTH_LONG).show();
                        fileSyncHashMap.get(pair).addAll(fileSyncs);
                }
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(getApplicationContext(), WebService.class);
                startService(intent);
            }
        }, 2000);

        RecyclerView rvPairs = (RecyclerView) findViewById(R.id.rvPairs);
        rvPairs.setAdapter(pairsAdapter);
        rvPairs.setLayoutManager(new LinearLayoutManager(this));
        rvPairs.postDelayed(() -> {
            for (int i = 0 ; i < pairsAdapter.getItemCount() ; i++){
                PairsAdapter.ViewHolder vh = (PairsAdapter.ViewHolder) rvPairs.findViewHolderForAdapterPosition(i);
                if (vh != null){
                    Pair pair = pairs.get(i);
                    vh.statusButton.setOnClickListener(v -> {
                        sendRequest(pair.getId(), pair.getIp(), "4000", Command.FILES, "");
                    });
                }
            }
        },2000);
        fileSyncHashMap = new HashMap<>();
        for (Pair p : pairs){
            ArrayList<FileSync> fileSyncs= new ArrayList<>();
            fileSyncHashMap.put(p, fileSyncs);
            sendRequest(p.getId(), p.getIp(), Integer.toString(p.getPort()), Command.POLL, "");
        }
        Timer time = new Timer(); // Instantiate Timer Object
        LocationTask st = new LocationTask(); // Instantiate SheduledTask class
        time.schedule(st, 0, 5000); // Create Repetitively task for every 1 secs
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Intent intent = new Intent(this, WebService.class);
        stopService(intent);
    }

    private void displayQRCode(String jsonString) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_qr_code_layout);
        ImageView QRCode = (ImageView) dialog.findViewById(R.id.qr_code);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        int imageWidth = (int) Math.round(width * 0.8);
        QRCode.setMaxHeight(imageWidth);
        QRCode.setMaxWidth(imageWidth);
        try {
            Bitmap bitmap = encodeAsBitmap(jsonString, imageWidth);
            QRCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.generate:
                displayDialogQRCode();
                return true;
            case R.id.register:
                capture();
                return true;
            case R.id.action_sortName:
                sort(SORTNAME);
                return true;
            case R.id.action_sortDistance:
                sort(SORTDIST);
                return true;
            case R.id.action_sortLastAccessed:
                sort(SORTDATE);
                return true;
            case R.id.add:
                addManually();
                return true;
            case R.id.updates:
                checkUpdates();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkUpdates() {
        Intent intent = new Intent(this, UpdatesActivity.class);
        intent.putExtra(Command.HASHMAPSYNC, fileSyncHashMap);
        startActivity(intent);
    }

    private void addManually() {
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_layout);
        dialog.setTitle("ADD A PAIR MANUALLY");

        final EditText id_edit_text = (EditText) dialog.findViewById(R.id.id_edit_text);
        final EditText name_edit_text = (EditText) dialog.findViewById(R.id.name_edit_text);
        final EditText ip_edit_text = (EditText) dialog.findViewById(R.id.ip_edit_text);
        final EditText port_edit_text = (EditText) dialog.findViewById(R.id.port_edit_text);

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        ipAddress = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        port = "5000";

        id_edit_text.setText("1");
        name_edit_text.setText(getDeviceName());

        ip_edit_text.setText(ipAddress);
        port_edit_text.setText(port);
        Button dialogButton = (Button) dialog.findViewById(R.id.ok_generate_button);
        dialogButton.setOnClickListener(v -> {
            String jsonString = jsonString(
                    id_edit_text.getText().toString(),
                    name_edit_text.getText().toString(),
                    ip_edit_text.getText().toString(),
                    port_edit_text.getText().toString()
            );
            Pair pair = parse(jsonString);
            if (!alreadyExist(pair)) {
                pairs.add(pair);
                ArrayList<FileSync> fileSyncs = new ArrayList<FileSync>();
                fileSyncHashMap.put(pair, fileSyncs);
                // Notify the adapter that an item was inserted at position 0
                pairsAdapter.notifyDataSetChanged();
                saveToPreferences(Command.LISTPAIRS, pairs);
                sendRequest(pair.getId(), pair.getIp(), Integer.toString(pair.getPort()), Command.POLL, "");

            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void sort(int comparator) {
        switch (comparator) {
            case SORTNAME:
                PairNameComparator comparatorName = new PairNameComparator();
                Collections.sort(pairs, comparatorName);
                break;
            case SORTDIST:
                PairDistanceComparator comparatorDistance = new PairDistanceComparator();
                Collections.sort(pairs, comparatorDistance);
                break;
            case SORTDATE:
                PairLastAccessedComparator comparatorDate = new PairLastAccessedComparator();
                Collections.sort(pairs, comparatorDate);
                break;
        }
        pairsAdapter.notifyDataSetChanged();
    }

    private void displayDialogQRCode() {
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_layout);
        dialog.setTitle("GENERATE QR CODE");

        final EditText id_edit_text = (EditText) dialog.findViewById(R.id.id_edit_text);
        final EditText name_edit_text = (EditText) dialog.findViewById(R.id.name_edit_text);
        final EditText ip_edit_text = (EditText) dialog.findViewById(R.id.ip_edit_text);
        final EditText port_edit_text = (EditText) dialog.findViewById(R.id.port_edit_text);

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        ipAddress = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        port = "5000";

        id_edit_text.setText("1");
        name_edit_text.setText(getDeviceName());

        ip_edit_text.setText(ipAddress);
        port_edit_text.setText(port);
        Button dialogButton = (Button) dialog.findViewById(R.id.ok_generate_button);
        dialogButton.setOnClickListener(v -> {
            String jsonString = jsonString(
                    id_edit_text.getText().toString(),
                    name_edit_text.getText().toString(),
                    ip_edit_text.getText().toString(),
                    port_edit_text.getText().toString()
            );
            displayQRCode(jsonString);
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Start the WebService to handle the server in a different thread

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            case REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
                    Log.d("Files", "Path: " + path);
                    File directory = new File(path);
                    File[] files = directory.listFiles();
                    String[] filesAndDir = directory.list();
                    Log.d("Files", "Size: "+ files.length);
                    for (int i = 0; i < files.length; i++)
                    {
                        Log.d("Files", "FileName:" + files[i].getName());
                    }
                    for (int i = 0; i < filesAndDir.length; i++)
                    {
                        Log.d("filesAndDir", "FileNameOrFolder:" + filesAndDir[i]);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private void startFolderContentActivity(FolderContent folderContent, int pairID) {
        Intent intentActivity = new Intent(this, FolderContentActivity.class);
        intentActivity.putExtra(Command.FOLDERCONTENT, folderContent);
        intentActivity.putExtra(Command.PAIR, findPairById(pairID));
        intentActivity.putExtra(Command.PAIRID, pairID);
        startActivity(intentActivity);
    }

    private Pair findPairById(int pairID) {
        for (Pair p : pairs){
            if (p.getId() == pairID){
                return p;
            }
        }
        return null;
    }

    private void capture() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                Pair pair = parse(result.getContents());
                if (!alreadyExist(pair)) {
                    pairs.add(pair);
                    ArrayList<FileSync> fileSyncs = new ArrayList<FileSync>();
                    fileSyncHashMap.put(pair, fileSyncs);
                    // Notify the adapter that an item was inserted at position 0
                    pairsAdapter.notifyDataSetChanged();
                    saveToPreferences(Command.LISTPAIRS, pairs);
                    sendRequest(pair.getId(), pair.getIp(), Integer.toString(pair.getPort()), Command.POLL, "");
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void sendRequest(int pairID, String ipAddress, String port, String command, String param) {
        request = new RequestAsyncTask(this, pairID, ipAddress, port);
        request.getBroadcaster().registerReceiver(receiver, new IntentFilter(Command.COMMAND));
        request.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, command, param);
    }

    public <T> void saveToPreferences(String key, List<T> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.commit();
    }

    private boolean alreadyExist(Pair pair) {
        for (Pair p : pairs){
            if (pair.getId() == p.getId()){
                return true;
            }
        }
        return false;
    }

    public Pair parse(String jsonLine) {
        JsonObject jsonObject = new Gson().fromJson(jsonLine, JsonObject.class);
        int id = jsonObject.get("id").getAsInt();
        String name = jsonObject.get("name").getAsString();
        String ip = jsonObject.get("ip").getAsString();
        int port = jsonObject.get("port").getAsInt();
        Date lastAccessedDate = new Date();

        Pair pair = new Pair(id,
                name,
                ip,
                port,
                lastAccessedDate);
        return pair;
    }


    private String jsonString(String id, String name, String ip,
                              String port){
        String jsonString = "{" +
                "'id':" + id  +", " +
                "'name': '" + name + "', " +
                "'ip':'" + ip + "', " +
                "'port':" + port + ", " +
                "'lastAccessed':'" + " " + "', " +
                "'location':'" + " " + "' " +
                "}";
        return jsonString;
    }

    Bitmap encodeAsBitmap(String str, int imageWidth) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, imageWidth, imageWidth, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, imageWidth, 0, 0, w, h);
        return bitmap;
    }

    private void checkAllPermissions() {
        int permissionAccessFineLocation = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionAccessCoarseLocation = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionReadExternalStorage = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWriteExternalStorage = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionAccessFineLocation != PackageManager.PERMISSION_GRANTED && permissionAccessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            // Start the WebService to handle the server in a different thread
            Intent intent = new Intent(this, WebService.class);
            startService(intent);
        }

        if (permissionReadExternalStorage != PackageManager.PERMISSION_GRANTED && permissionWriteExternalStorage != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ_EXTERNAL_STORAGE);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public float getDistance(Pair pair) {
        if (deviceLocation != null) {
            float distance[] =  new float[1];
            Location.distanceBetween(deviceLocation.getLatitude(), deviceLocation.getLongitude(),
                    pair.getLocation().getLatitude(), pair.getLocation().getLongitude(), distance);
            return distance[0];
        }
        return -1;
    }

    public void deletePair(Pair pair) {
        pairs.remove(pair);
        pairsAdapter.notifyDataSetChanged();
        saveToPreferences(Command.LISTPAIRS, pairs);
    }

    class PairNameComparator implements Comparator<Pair>{

        @Override
        public int compare(Pair p1, Pair p2) {
            return p1.getName().compareTo(p2.getName());
        }
    }

    class PairLastAccessedComparator implements Comparator<Pair>{

        @Override
        public int compare(Pair p1, Pair p2) {
            return p1.getLastAccessed().compareTo(p2.getLastAccessed());
        }
    }

    class PairDistanceComparator implements Comparator<Pair> {

        @Override
        public int compare(Pair p1, Pair p2) {
            float[] results1 = new float[1];
            float[] results2 = new float[1];
            Location.distanceBetween(deviceLocation.getLatitude(), deviceLocation.getLongitude(),
                    p1.getLocation().getLatitude(), p1.getLocation().getLongitude(), results1);
            Location.distanceBetween(deviceLocation.getLatitude(), deviceLocation.getLongitude(),
                    p2.getLocation().getLatitude(), p2.getLocation().getLongitude(), results2);
            return Float.compare(results1[0], results2[0]);
        }
    }

    public class LocationTask extends TimerTask{
        @Override
        public void run() {
            for (Pair p : pairs){
                sendRequest(p.getId(), p.getIp(), "4000", Command.LOCATION, "");
            }
        }
    }

}
