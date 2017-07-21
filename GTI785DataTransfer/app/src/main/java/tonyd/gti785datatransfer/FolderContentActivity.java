package tonyd.gti785datatransfer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by tonyd on 7/14/2017.
 */

public class FolderContentActivity extends Activity {

    LinearLayout linearLayout;
    FolderContent folderContent;
    Pair pair;
    int pairID;
    String path;

    /* For sending requests to the server */
    private RequestAsyncTaskFolderContent request;
    private RequestAsyncTaskFile downloadRequest;

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
        displayFolderContent();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getStringExtra(Command.COMMAND);
                switch (command) {
                    case Command.FOLDERCONTENT_SUB:
                        // Display files
                        FolderContent folderContent = intent.getParcelableExtra(Command.FOLDERCONTENT_SUB);
                        if (path == "") {
                            path = currentFolder;
                        } else {
                            path += "/" + currentFolder;
                        }
                        updateLayout(folderContent);

                }
            }
        };
    }

    private void updateLayout(FolderContent folderContent) {
        linearLayout.removeAllViews();
        this.folderContent = folderContent;
        displayFolderContent();
    }

    private void displayFolderContent() {
        final ArrayList<Folder> folders = (ArrayList<Folder>) folderContent.getFolders();
        ArrayList<File> files = (ArrayList<File>) folderContent.getFiles();

        linearLayout = (LinearLayout) findViewById(R.id.linear_layout_main_folder_content);
        for (final Folder folder : folders){
            String name = folder.getName();
            Button button = new Button(this);
            button.setText(name);
            button.setPadding(10,10,10,10);
            button.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentFolder = folder.getName();
                    if (path == ""){
                        sendRequest(pairID, pair.getIp(), "4000", Command.FILES, folder.getName());

                    } else {
                        sendRequest(pairID, pair.getIp(), "4000", Command.FILES, path + "/" + folder.getName());
                    }
                }
            });
            linearLayout.addView(button);
        }
        for (final File file : files){
            String name = file.getName();
            Button button = new Button(this);
            button.setText(name);
            button.setPadding(10,10,10,10);
            button.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (path == ""){
                        sendDownloadRequest(pairID, pair.getIp(), "4000", Command.FILES, file.getName());

                    } else {
                        sendDownloadRequest(pairID, pair.getIp(), "4000", Command.FILES, path + "/" + file.getName());
                    }
                }
            });
            linearLayout.addView(button);
        }
    }

    private void sendRequest(int pairID, String ipAddress, String port, String command, String param) {
        request = new RequestAsyncTaskFolderContent(this, pairID, ipAddress, port);
        request.getBroadcaster().registerReceiver(receiver, new IntentFilter(Command.COMMAND));
        request.execute(command, param);
    }

    private void sendDownloadRequest(int pairID, String ipAddress, String port, String command, String param) {
        downloadRequest = new RequestAsyncTaskFile(this, pairID, ipAddress, port);
        downloadRequest.getBroadcaster().registerReceiver(receiver, new IntentFilter(Command.COMMAND));
        downloadRequest.execute(command, param);
    }
}
