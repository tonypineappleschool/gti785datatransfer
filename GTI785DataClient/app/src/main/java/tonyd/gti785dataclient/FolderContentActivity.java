package tonyd.gti785dataclient;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.net.MalformedURLException;
import java.util.ArrayList;

/**
 * Created by tonyd on 7/14/2017.
 */

public class FolderContentActivity extends Activity {

    LinearLayout linearLayout;
    FolderContent folderContent;
    ArrayList<Folder> folders;
    ArrayList<File> files;
    Pair pair;
    int pairID;
    String path;
    FoldersAdapter foldersAdapter;
    FilesAdapter filesAdapter;
    RecyclerView rvFolders;
    RecyclerView rvFiles;

    /* For sending requests to the server */
    private RequestAsyncTaskFolderContent request;

    /* For receiving broadcast messages from AsynctaskRequest */
    private BroadcastReceiver receiver;
    private String currentFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_content);


        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        folderContent = intent.getParcelableExtra(Command.FOLDERCONTENT);
        pair = intent.getParcelableExtra(Command.PAIR);
        pairID = intent.getIntExtra(Command.PAIRID, -1);
        path = "";
        folders = (ArrayList<Folder>) folderContent.getFolders();
        files = (ArrayList<File>) folderContent.getFiles();
        displayFolderContent();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getStringExtra(Command.COMMAND);
                switch (command) {
                    case Command.FOLDERCONTENT_SUB:
                        // Display files
                        FolderContent folderContent = intent.getParcelableExtra(Command.FOLDERCONTENT_SUB);
                        updateLayout(folderContent);

                }
            }
        };
    }

    private void updateLayout(FolderContent folderContent) {
        this.folderContent = folderContent;
        files = (ArrayList<File>) folderContent.getFiles();
        folders = (ArrayList<Folder>) folderContent.getFolders();
        filesAdapter.swapItems(files);
        foldersAdapter.swapItems(folders);
        foldersAdapter.notifyDataSetChanged();
        filesAdapter.notifyDataSetChanged();
        setListeners(rvFolders, rvFiles);
    }

    private void displayFolderContent() {
        rvFolders = (RecyclerView) findViewById(R.id.rvFolders);
        foldersAdapter = new FoldersAdapter(this, folders);
        foldersAdapter.setFolderContentActivity(this);
        rvFolders.setAdapter(foldersAdapter);
        rvFolders.setLayoutManager(new LinearLayoutManager(this));

        rvFiles = (RecyclerView) findViewById(R.id.rvFiles);
        filesAdapter = new FilesAdapter(this, files);
        filesAdapter.setFolderContentActivity(this);
        rvFiles.setAdapter(filesAdapter);
        rvFiles.setLayoutManager(new LinearLayoutManager(this));

        setListeners(rvFolders, rvFiles);

    }

    public void updateListeners(){
        setListeners(rvFolders, rvFiles);
    }

    private void setListeners(final RecyclerView rvFolders, final RecyclerView rvFiles) {
        rvFolders.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0 ; i < foldersAdapter.getItemCount() ; i++){
                    FoldersAdapter.ViewHolder vh = (FoldersAdapter.ViewHolder) rvFolders.findViewHolderForAdapterPosition(i);
                    if (vh != null){
                        Folder folder = folders.get(i);
                        vh.statusButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                currentFolder = folder.getName();
                                if (path == ""){
                                    path = currentFolder;
                                    sendRequest(pairID, pair.getIp(), "4000", Command.FILES, folder.getName());
                                } else {
                                    path += "/" + currentFolder;
                                    sendRequest(pairID, pair.getIp(), "4000", Command.FILES, path + "/" + folder.getName());
                                }
                            }
                        });
                    }
                }
            }
        }, 50);

        rvFiles.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < filesAdapter.getItemCount(); i++) {
                    FilesAdapter.ViewHolder vh = (FilesAdapter.ViewHolder) rvFiles.findViewHolderForAdapterPosition(i);
                    if (vh != null) {
                        File file = files.get(i);
                        vh.statusButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (path == "") {
                                    sendDownloadRequest(pairID, pair.getIp(), "4000", Command.FILES, Uri.encode(file.getName()), file.getName());

                                } else {
                                    sendDownloadRequest(pairID, pair.getIp(), "4000", Command.FILES, path + "/" + Uri.encode(file.getName()), file.getName());
                                }
                            }
                        });
                    }
                }
            }
        }, 50);
    }

    private void sendRequest(int pairID, String ipAddress, String port, String command, String param) {
        request = new RequestAsyncTaskFolderContent(this, pairID, ipAddress, port);
        request.getBroadcaster().registerReceiver(receiver, new IntentFilter(Command.COMMAND));
        request.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, command, param);
    }

    private void sendDownloadRequest(int pairID, String ipAddress, String port, String command, String param, String filename) {
        String[] params = new String[]{command, param};
        Uri url = null;
        try {
            url = URLbuilt(ipAddress, port, params);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(url);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle(filename);
        request.setDescription(filename);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

        long downloadID = downloadManager.enqueue(request);

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadID);
        downloadManager.query(query);
    }

    private Uri URLbuilt(String ipAddress, String port, String[] params) throws MalformedURLException {
        String baseUrl = "http://" + ipAddress + ":" + port;
        String relURL = "";
        for (String s : params){
            relURL += "/" + s;
        }
        return Uri.parse(baseUrl + relURL);
    }

    @Override
    public void onBackPressed() {
        // save data first
        if (path == ""){

        } else {
            int lastIndex = path.lastIndexOf("/");
            if (lastIndex == -1){
                path = "";
            } else {
                path = path.substring(0, lastIndex);
            }
            sendRequest(pairID, pair.getIp(), "4000", Command.FILES, path);
        }
    }
}
