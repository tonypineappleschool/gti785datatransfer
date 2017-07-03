package tonyd.gti785datatransfer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by tonyd on 5/28/2017.
 */

public class RequestAsyncTask extends AsyncTask<String, Void, String> {

    private String baseUrl;
    private String ipAddress;
    private String port;

    public LocalBroadcastManager getBroadcaster() {
        return broadcaster;
    }

    private LocalBroadcastManager broadcaster;
    private Context context;

    public RequestAsyncTask(Context context, String ipAddress, String port) {
        this.context = context;
        broadcaster = LocalBroadcastManager.getInstance(context);
        this.ipAddress = ipAddress;
        this.port = port;
        baseUrl = "http://" + ipAddress + ":" + port + "/";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            URL url = new URL(baseUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(5000);
            String method = "GET";
            conn.setRequestMethod(method);
            int responseCode = conn.getResponseCode();
            Log.d("RESPONSE CODE", Integer.toString(responseCode));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    @Override
    protected void onPostExecute(String result) {
        sendRequest();
    }

    private void sendRequest() {
        new RequestAsyncTask(context, ipAddress, port).execute();
    }

}

