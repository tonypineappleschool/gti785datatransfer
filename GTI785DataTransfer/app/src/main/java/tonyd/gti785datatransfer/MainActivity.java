package tonyd.gti785datatransfer;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {

    private List<Pair> pairs;
    private List<PairUI> pairsUI;
    private ViewGroup linearLayout;
    private Location currentLocation;

    /* For sending requests to the server */
    private RequestAsyncTask request;

    /* For receiving broadcast messages from AsynctaskRequest */
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        linearLayout = (ViewGroup) findViewById(R.id.linear_layout_main);
        pairs = new ArrayList<>();
        pairsUI = new ArrayList<>();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getStringExtra(Command.LOCATION);
                switch (command) {
                    case Command.LOCATION:
                        int pairID = intent.getIntExtra("pairID", -1);
                        Location location = intent.getParcelableExtra("location");
                        pairs.get(pairID).getLocation().setLatitude(location.getLatitude());
                        pairs.get(pairID).getLocation().setLongitude(location.getLongitude());
                        // Update UI
                        float distance = currentLocation.distanceTo(location);
                        pairsUI.get(pairID).getTextViewDistance().setText(Float.toString(distance));
                        break;
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
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    private void capture() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setOrientationLocked(false);
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
                pairs.add(pair);
                int pairID = pairs.indexOf(pair);
                addPair(pair);
                sendRequest(pairID, pair.getIp(), Integer.toString(pair.getPort()));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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

    private void addPair(Pair pair) {
        LinearLayout LL = new LinearLayout(this);
        LL.setBackgroundColor(Color.CYAN);
        LL.setOrientation(LinearLayout.VERTICAL);
        LayoutParams LLParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        LL.setLayoutParams(LLParams);

        TextView textViewName = new TextView(this);
        textViewName.setText(pair.getName());
        LayoutParams TVParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
        textViewName.setLayoutParams(TVParams);
        LL.addView(textViewName);

        TextView textViewDistance = new TextView(this);
        textViewDistance.setText('0');
        textViewDistance.setLayoutParams(TVParams);
        LL.addView(textViewDistance);

        Button pairButton = new Button(this);
        pairButton.setText(pair.getName());
        pairButton.setLayoutParams(new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT));
        pairButton.setPadding(10,10,10,10);
        pairButton.setTextSize(13.0f);
        LL.addView(pairButton);

        PairUI pairUI = new PairUI(textViewName, textViewDistance, pairButton);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendRequest(int pairID, String ipAddress, String port) {
        request = new RequestAsyncTask(this, pairID, ipAddress, port);
        request.execute();
    }
}
