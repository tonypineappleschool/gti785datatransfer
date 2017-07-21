package tonyd.gti785datatransfer;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by tonyd on 5/28/2017.
 */

public class RequestAsyncTaskFile extends AsyncTask<String, Void, String> {

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

    public RequestAsyncTaskFile(Context context, int pairID, String ipAddress, String port) {
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
            Uri url = URLbuilt(params);
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            command = params[0];
            DownloadManager.Request request = new DownloadManager.Request(url);

            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "");
            long downloadID = downloadManager.enqueue(request);

            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadID);
            downloadManager.query(query);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }


        return null;

    }

    private void updateView(FolderContent folderContent, int pairID) {
        Intent intent = new Intent(Command.COMMAND);
        intent.putExtra(Command.COMMAND, Command.FOLDERCONTENT_SUB);
        intent.putExtra(Command.FOLDERCONTENT_SUB, folderContent);
        intent.putExtra(Command.PAIRID, pairID);
        broadcaster.sendBroadcast(intent);
    }

    private Uri URLbuilt(String[] params) throws MalformedURLException {
        String relURL = "";
        for (String s : params){
            relURL += "/" + s;
        }
        return Uri.parse(baseUrl + relURL);
    }

    private void updateLocation(Location location, int pairID) {
        Intent intent = new Intent(Command.COMMAND);
        intent.putExtra(Command.COMMAND, Command.LOCATION);
        intent.putExtra("location", location);
        intent.putExtra("pairID", pairID);
        broadcaster.sendBroadcast(intent);
    }

    @Override
    protected void onPostExecute(String result) {
    }

}

