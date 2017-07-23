package tonyd.gti785datatransfer;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static tonyd.gti785datatransfer.Command.POLL;

public class MainActivity extends Activity {
    private static final int REQUEST_LOCATION = 1;
    private List<Pair> pairs;
    private List<PairUI> pairsUI;
    private ViewGroup linearLayout;
    private Location currentLocation;

    private SharedPreferences sharedPreferences;

    private SharedPreferences.Editor editor;


    /* For sending requests to the server */
    private RequestAsyncTask request;

    /* For receiving broadcast messages from AsynctaskRequest */
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = sharedPreferences.getString(Command.LISTPAIRS, null);
        linearLayout = (ViewGroup) findViewById(R.id.linear_layout_main);
        pairsUI = new ArrayList<>();
        if (json == null){
            pairs = new ArrayList<>();
        } else {
            Type type = new TypeToken<ArrayList<Pair>>() {
            }.getType();
            pairs = gson.fromJson(json, type);
            for (Pair p : pairs){
                addPairUI(p);
            }
        }


        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getStringExtra(Command.COMMAND);
                switch (command) {
                    case Command.DISCONNECTED:
                        int pairID = intent.getIntExtra("pairID", -1);
                        pairsUI.get(pairID).getButton().setBackgroundColor(Color.RED);
                        break;
                    case Command.CONNECTED:
                        pairID = intent.getIntExtra("pairID", -1);
                        pairsUI.get(pairID).getButton().setBackgroundColor(Color.CYAN);
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
                            pairsUI.get(pairID).getTextViewDistance().setText(Float.toString(distance[0]));
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
                for (PairUI pairUI : pairsUI){
                    float distance[] =  new float[1];
                    Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                            pairUI.getPair().getLocation().getLatitude(), pairUI.getPair().getLocation().getLongitude(), distance);
                    pairUI.getTextViewDistance().setText(Float.toString(distance[0]));
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
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


        RecyclerView recycler = (RecyclerView) findViewById(R.id.pair);
        PairAdapter adapter = new PairAdapter(pairs);
        recycler.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(linearLayoutManager);

        recycler.addItemDecoration(new DividerItemDecoration(recycler.getContext(),
                linearLayoutManager.getOrientation()));


        pairs.add(new Pair(123456,
                "tony 1",
                //"192.168.43.182",
                "192.168.43.84",
                8080,
                //5000,
                new Date()));

        pairs.add(new Pair(123456,
                "tony 2",
                //"192.168.43.182",
                "192.168.43.84",
                8080,
                //5000,
                new Date()));

        pairs.add(new Pair(123456,
                "tony 3d",
                //"192.168.43.182",
                "192.168.43.84",
                8080,
                //5000,
                new Date()));


    }

    private void startFolderContentActivity(FolderContent folderContent, int pairID) {
        Intent intentActivity = new Intent(this, FolderContentActivity.class);
        intentActivity.putExtra(Command.FOLDERCONTENT, folderContent);
        intentActivity.putExtra(Command.PAIR, pairs.get(pairID));
        intentActivity.putExtra(Command.PAIRID, pairID);
        startActivity(intentActivity);
    }

    private void capture() {
//        IntentIntegrator integrator = new IntentIntegrator(this);
//        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
//        integrator.initiateScan();

        // TEMP
        DateFormat df = new SimpleDateFormat("mm/dd/yyyy");
        Date lastAccessedDate = new Date();
        try {
            Pair pair = new Pair(123456,
                    "tony",
                    //"192.168.43.182",
                    "192.168.43.84",
                    8080,
                    //5000,
                    df.parse("09/09/1999"));
            if (!alreadyExist(pair)) {
                pairs.add(pair);
                int pairID = pairs.indexOf(pair);
                addPairUI(pair);
                saveToPreferences(Command.LISTPAIRS, pairs);
                sendRequest(pairID, pair.getIp(), Integer.toString(pair.getPort()), POLL, "");

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

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
                    addPairUI(pair);
                    saveToPreferences(Command.LISTPAIRS, pairs);
                    sendRequest(pairID, pair.getIp(), Integer.toString(pair.getPort()), POLL, "");

                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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

    private void addPairUI(final Pair pair) {

        LinearLayout LL = new LinearLayout(this);
        LL.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams LLParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        LL.setLayoutParams(LLParams);

        TextView textViewName = new TextView(this);
        textViewName.setText(pair.getName());
        LayoutParams TVParams = new ViewGroup.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT);
        textViewName.setLayoutParams(TVParams);
        textViewName.setPadding(10,10,10,10);

        LL.addView(textViewName);

        TextView textViewDistance = new TextView(this);
        textViewDistance.setText("0");
        textViewDistance.setLayoutParams(TVParams);
        textViewDistance.setPadding(10,10,10,10);
        LL.addView(textViewDistance);

        Button pairButton = new Button(this);
        pairButton.setText(pair.getName());
        pairButton.setLayoutParams(new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT));
        pairButton.setPadding(10,10,10,10);
        pairButton.setTextSize(13.0f);
        pairButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pairID = pairs.indexOf(pair);
                sendRequest(pairID, pair.getIp(), "4000", Command.FILES, "");
            }
        });
        LL.addView(pairButton);

        PairUI pairUI = new PairUI(pair, textViewName, textViewDistance, pairButton);
        pairsUI.add(pairUI);
        linearLayout.addView(LL);
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
            case R.id.register:
                capture();
                return true;
            case R.id.sortByName:
                sortByName();
                return true;
            case R.id.sortByDistance:
                sortByDistance();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sortByName() {
        Collections.sort(pairsUI, PairUI.COMPARE_BY_NAME);
        linearLayout.removeAllViews();
        for (PairUI pairUI : pairsUI){
            LinearLayout LL = new LinearLayout(this);
            LL.setOrientation(LinearLayout.HORIZONTAL);
            LayoutParams LLParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
            LL.setLayoutParams(LLParams);
            LL.addView(pairUI.getTextViewName());
            LL.addView(pairUI.getTextViewDistance());
            LL.addView(pairUI.getButton());
            linearLayout.addView(LL);
        }
    }

    private void sortByDistance() {
        Collections.sort(pairsUI, PairUI.COMPARE_BY_DISTANCE);
        linearLayout.removeAllViews();
        for (PairUI pairUI : pairsUI){
            LinearLayout LL = new LinearLayout(this);
            LL.setOrientation(LinearLayout.HORIZONTAL);
            LayoutParams LLParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
            LL.setLayoutParams(LLParams);
            LL.addView(pairUI.getTextViewName());
            LL.addView(pairUI.getTextViewDistance());
            LL.addView(pairUI.getButton());
            linearLayout.addView(LL);
        }
    }

    private void sendRequest(int pairID, String ipAddress, String port, String command, String param) {
        request = new RequestAsyncTask(this, pairID, ipAddress, port);
        request.getBroadcaster().registerReceiver(receiver, new IntentFilter(Command.COMMAND));
        request.execute(command, param);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Start the WebService to handle the server in a different thread
                    Toast.makeText(getApplicationContext(), "Permission Location granted",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    public <T> void saveToPreferences(String key, List<T> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.commit();
    }
}
