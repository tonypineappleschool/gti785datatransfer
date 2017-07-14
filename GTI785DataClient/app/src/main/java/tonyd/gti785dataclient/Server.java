package tonyd.gti785dataclient;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by tonyd on 5/25/2017.
 */

public class Server extends NanoHTTPD {

    private static final String ipaddress = "192.168.43.182";
    private static final int port = 5000;

    private WebService service;
    private Context context;
    private LinkedBlockingQueue lbq;

    public Server(WebService wb, Context context, String ipaddress) {
        super(ipaddress, port);
        service = wb;
        this.context = context;

    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        String[] separated = mySplit(uri, "/");
        String command = separated[0];
        switch (command) {
            case Command.FILES:
                String path = "";
                for (int i = 1 ; i > separated.length ; i++){
                    path += separated[i] + "/";
                }
                // Remove the last slash
                path = path.substring(0, path.length()-1);
                String finalPath = Environment.getExternalStorageDirectory().getAbsolutePath() + path;
                File pathFile = new File(finalPath);
                if (pathFile.isFile()){
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(pathFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    return Response.newChunkedResponse(Status.OK, "application/octet-stream",fis);
                } else if (pathFile.isDirectory()){
                    File[] filesFolders = pathFile.listFiles();
                    ArrayList<Folder> folders = new ArrayList<>();
                    ArrayList<tonyd.gti785dataclient.File> files = new ArrayList<>();
                    for (File f : filesFolders){
                        if (f.isDirectory()){
                            Date lastModDate = new Date(f.lastModified());
                            Folder folder = new Folder(f.getName(), lastModDate);
                            folders.add(folder);
                        } else if (f.isFile()){
                            int bytes = (int) f.length();
                            int kilobytes = (bytes / 1024);
                            int megabytes = (kilobytes / 1024);
                            Date lastModDate = new Date(f.lastModified());
                            tonyd.gti785dataclient.File file = new tonyd.gti785dataclient.File(f.getName(), lastModDate, megabytes);
                            files.add(file);
                        }
                    }
                    FolderContent folderContent = new FolderContent(folders, files);
                    Gson gson = new Gson();
                    String serializedFolderContent = gson.toJson(folderContent);
                    return Response.newFixedLengthResponse(serializedFolderContent);
                }
            case Command.POLL:
                Gson gson = new Gson();
                lbq = new LinkedBlockingQueue();
                Object o = null;
                try {
                    // wait for 60 s for there to be an object in the queue
                    o = lbq.poll(60, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (o == null){
                    Log.d("LBQ", "Poll returns null");
                } else {
                    String serializedObject = gson.toJson(o);
                    lbq = null;
                    return Response.newFixedLengthResponse(serializedObject);
                }
                lbq = null;
                return Response.newFixedLengthResponse("8000");
        }
        return Response.newFixedLengthResponse("8000");
    }

    public LinkedBlockingQueue getLbq() {
        return lbq;
    }

    public void setLbq(LinkedBlockingQueue lbq) {
        this.lbq = lbq;
    }

    /* Utilities */
    public String[] mySplit(final String input, final String delim) {
        return input.replaceFirst("^" + delim, "").split(delim);
    }
}
