package tonyd.gti785datatransfer;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private int pairID;

    public LocalBroadcastManager getBroadcaster() {
        return broadcaster;
    }

    private LocalBroadcastManager broadcaster;
    private Context context;

    public RequestAsyncTask(Context context, int pairID, String ipAddress, String port) {
        this.context = context;
        broadcaster = LocalBroadcastManager.getInstance(context);
        this.pairID = pairID;
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
            String command = params[0];
            URL url = URLbuilt(params);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(60 * 1000);
            conn.setConnectTimeout(5000);
            String method = "GET";
            conn.setRequestMethod(method);
            int responseCode = conn.getResponseCode();
            Log.d("RESPONSE CODE", Integer.toString(responseCode));

            if (responseCode == 200){
                if (command.equals(Command.POLL)){
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    Gson gson = new Gson();
                    Location location = gson.fromJson(in, Location.class);
                    updateLocation(location, pairID);
                } else if (command.equals(Command.FILE)) {
                    String contentType = conn.getContentType();
                    if (contentType.equals("application/octet-stream")) {
                        // it is a file
                    } else {
                        // it is a directory
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(conn.getInputStream()));
                        Gson gson = new Gson();
                        FolderContent folderContent = gson.fromJson(in, FolderContent.class);
                        updateView(folderContent);
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    private void updateView(FolderContent folderContent) {
        Intent intent = new Intent(Command.COMMAND);
        intent.putExtra(Command.COMMAND, Command.FOLDERCONTENT);
        intent.putExtra("folderContent", folderContent);
        broadcaster.sendBroadcast(intent);
    }

    private URL URLbuilt(String[] params) throws MalformedURLException {
        String command = params[0];
        URL url = new URL(baseUrl);
        String relURL;
        relURL = "/" + command;
        return new URL(url, relURL);
    }

    private void updateLocation(Location location, int pairID) {
        Intent intent = new Intent(Command.LOCATION);
        intent.putExtra(Command.LOCATION, Command.LOCATION);
        intent.putExtra("location", location);
        intent.putExtra("pairID", pairID);
        broadcaster.sendBroadcast(intent);
    }

    @Override
    protected void onPostExecute(String result) {
        sendRequest();
    }

    private void sendRequest() {
        new RequestAsyncTask(context, pairID, ipAddress, port).execute();
    }

}

