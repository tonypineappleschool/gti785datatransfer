package tonyd.gti785dataclient;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by tonyd on 5/28/2017.
 */

public class RequestAsyncTask extends AsyncTask<String, Void, String> {

    private String baseUrl;
    private String ipAddress;
    private String port;
    private int pairID;
    private String command;

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
        baseUrl = "http://" + ipAddress + ":" + port;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            command = params[0];
            URL url = URLbuilt(params);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(60 * 1000);
            conn.setConnectTimeout(10000);
            String method = "GET";
            conn.setRequestMethod(method);
            int responseCode = conn.getResponseCode();
            Log.d("RESPONSE CODE", Integer.toString(responseCode));
            setConnected(pairID, true);
            if (responseCode == 200){
                if (command.equals(Command.LOCATION)) {
                    String responseMessage = conn.getResponseMessage();

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    Gson gson = new Gson();
                    Pair.Location location = gson.fromJson(in, Pair.Location.class);
                    updateLocation(location, pairID);
                } else if (command.equals(Command.POLL)){
                    String responseMessage = conn.getResponseMessage();

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<FileSync>>() {
                    }.getType();
                    ArrayList<FileSync> fileSyncs = gson.fromJson(in, type);
                    sendNotifications(fileSyncs, pairID);
                }
                else if (command.equals(Command.FILES)) {
                    String contentType = conn.getContentType();
                    if (contentType.equals("application/octet-stream")) {
                        // it is a file
                    } else {
                        // it is a directory
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(conn.getInputStream()));
                        Gson gson = new Gson();
                        FolderContent folderContent = gson.fromJson(in, FolderContent.class);
                        updateView(folderContent, pairID);
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            setConnected(pairID, false);

        }
        return null;
    }

    private void sendNotifications(ArrayList<FileSync> fileSyncs, int pairID) {
        Intent intent = new Intent(Command.COMMAND);
        intent.putExtra(Command.COMMAND, Command.SYNC);
        intent.putExtra(Command.SYNC, fileSyncs);
        intent.putExtra(Command.PAIRID, pairID);
        intent.putExtra(Command.NOTIFID, getSaltString());
        broadcaster.sendBroadcast(intent);
    }

    private void setConnected(int pairID, boolean connected) {
        if (!connected){
            Intent intent = new Intent(Command.COMMAND);
            intent.putExtra(Command.COMMAND, Command.DISCONNECTED);
            intent.putExtra(Command.PAIRID, pairID);
            intent.putExtra(Command.NOTIFID, getSaltString());
            broadcaster.sendBroadcast(intent);
        } else {
            Intent intent = new Intent(Command.COMMAND);
            intent.putExtra(Command.COMMAND, Command.CONNECTED);
            intent.putExtra(Command.PAIRID, pairID);
            intent.putExtra(Command.NOTIFID, getSaltString());
            broadcaster.sendBroadcast(intent);
        }
    }

    private void updateView(FolderContent folderContent, int pairID) {
        Intent intent = new Intent(Command.COMMAND);
        intent.putExtra(Command.COMMAND, Command.FOLDERCONTENT);
        intent.putExtra(Command.FOLDERCONTENT, folderContent);
        intent.putExtra(Command.PAIRID, pairID);
        intent.putExtra(Command.NOTIFID, getSaltString());
        broadcaster.sendBroadcast(intent);
    }

    private URL URLbuilt(String[] params) throws MalformedURLException {
        String command = params[0];
        URL url = new URL(baseUrl);
        String relURL = "";
        for (String s : params){
            relURL += "/" + s;
        }
        return new URL(url, relURL);
    }

    private void updateLocation(Pair.Location location, int pairID) {
        Intent intent = new Intent(Command.COMMAND);
        intent.putExtra(Command.COMMAND, Command.LOCATION);
        intent.putExtra("location", location);
        intent.putExtra(Command.PAIRID, pairID);
        intent.putExtra(Command.NOTIFID, getSaltString());

        broadcaster.sendBroadcast(intent);
    }

    @Override
    protected void onPostExecute(String result) {
    }

    protected String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
}

