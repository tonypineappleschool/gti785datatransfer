package tonyd.gti785datatransfer;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
    private ViewGroup linearLayout;

    /* For sending requests to the server */
    private RequestAsyncTask request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        linearLayout = (ViewGroup) findViewById(R.id.linear_layout_main);
        pairs = new ArrayList<>();
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
                addPair(pair);
                sendRequest(pair.getIp(), Integer.toString(pair.getPort()));
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
        Button pairButton = new Button(this);
        pairButton.setText(pair.getName());
        pairButton.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        pairButton.setPadding(10,10,10,10);
        pairButton.setTextSize(13.0f);
        linearLayout.addView(pairButton);
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

    private void sendRequest(String ipAddress, String port) {
        request = new RequestAsyncTask(this, ipAddress, port);
        request.execute();
    }
}
