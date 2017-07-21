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
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements WebServiceCallbacks {

    private static final int REQUEST_WRITE_STORAGE = 2;
    private List<Pair> pairs;
    private ViewGroup linearLayout;
    private Location currentLocation;
    private PairsAdapter pairsAdapter;

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
    private boolean bound;
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebService.MyBinder myBinder = (WebService.MyBinder) service;
            webService = myBinder.getService();
            bound = true;
            webService.setCallbacks(MainActivity.this); // register
            Toast.makeText(MainActivity.this, "Connected to WebService", Toast.LENGTH_SHORT).show();
        }
    };
    public static int WHITE = 0xFFFFFFFF;
    public static int BLACK = 0xFF000000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView rvPairs = (RecyclerView) findViewById(R.id.rvPairs);

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
        rvPairs.setAdapter(pairsAdapter);
        rvPairs.setLayoutManager(new LinearLayoutManager(this));
        rvPairs.postDelayed(() -> {
            for (int i = 0 ; i < pairsAdapter.getItemCount() ; i++){
                PairsAdapter.ViewHolder vh = (PairsAdapter.ViewHolder) rvPairs.findViewHolderForAdapterPosition(i);
                if (vh != null){
                    Pair pair = pairs.get(i);
                    vh.statusButton.setOnClickListener(v -> {
                        int pairID = pairs.indexOf(pair);
                        sendRequest(pairID, pair.getIp(), "4000", Command.FILES, "");
                    });
                }
            }
        },50);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getStringExtra(Command.COMMAND);
                switch (command) {
                    case Command.DISCONNECTED:
                        int pairID = intent.getIntExtra("pairID", -1);
                        break;
                    case Command.CONNECTED:
                        pairID = intent.getIntExtra("pairID", -1);
                        break;
                    case Command.LOCATION:
                        pairID = intent.getIntExtra("pairID", -1);
                        Location location = intent.getParcelableExtra(Command.LOCATION);
                        pairs.get(pairID).getLocation().setLatitude(location.getLatitude());
                        pairs.get(pairID).getLocation().setLongitude(location.getLongitude());
                        // Update UI
                        if (currentLocation != null) {
                            float distance[] =  new float[1];
                            Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                                    location.getLatitude(), location.getLongitude(), distance);
                        }
                        break;
                    case Command.FOLDERCONTENT:
                        // Display files
                        pairID = intent.getIntExtra("pairID", -1);
                        FolderContent folderContent = intent.getParcelableExtra(Command.FOLDERCONTENT);
                        startFolderContentActivity(folderContent, pairID);

                }
            }
        };

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                currentLocation = location;
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
        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        for (Pair p : pairs){
            int pairID = pairs.indexOf(p);
            sendRequest(pairID, p.getIp(), Integer.toString(p.getPort()), Command.POLL, "");
        }
        checkAllPermissions();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location deviceLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

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
            case R.id.sortByName:
                return true;
            case R.id.sortByDistance:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                    Intent intent = new Intent(this, WebService.class);
                    boolean result = bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
                    Toast.makeText(getApplicationContext(), Boolean.toString(result),
                            Toast.LENGTH_SHORT).show();
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
        intentActivity.putExtra(Command.PAIR, pairs.get(pairID));
        intentActivity.putExtra(Command.PAIRID, pairID);
        startActivity(intentActivity);
    }

    private void capture() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.initiateScan();

//        // TEMP
//        DateFormat df = new SimpleDateFormat("mm/dd/yyyy");
//        Date lastAccessedDate = new Date();
//        try {
//            Pair pair = new Pair(123456,
//                    "tony",
//                    "172.20.10.6",
//                    5000,
//                    df.parse("09/09/1999"));
//            if (!alreadyExist(pair)) {
//                pairs.add(pair);
//                int pairID = pairs.indexOf(pair);
//                // Notify the adapter that an item was inserted at position 0
//                pairsAdapter.notifyItemInserted(0);
//                saveToPreferences(Command.LISTPAIRS, pairs);
//                sendRequest(pairID, pair.getIp(), Integer.toString(pair.getPort()), Command.POLL, "");
//
//            }
//
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

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
                    int pairID = pairs.indexOf(pair);
                    // Notify the adapter that an item was inserted at position 0
                    pairsAdapter.notifyItemInserted(0);
                    saveToPreferences(Command.LISTPAIRS, pairs);
                    sendRequest(pairID, pair.getIp(), Integer.toString(pair.getPort()), Command.POLL, "");
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
        String lastAccessed = jsonObject.get("lastAccessed").getAsString();
        String location = jsonObject.get("location").getAsString();

        DateFormat df = new SimpleDateFormat("mm/dd/yyyy");
        Date lastAccessedDate = new Date();
        try {
            lastAccessedDate = df.parse(lastAccessed);
        } catch (ParseException e) {
            e.printStackTrace();
        }

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
            boolean result = bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            Toast.makeText(getApplicationContext(), Boolean.toString(result),
                    Toast.LENGTH_SHORT).show();
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

}
