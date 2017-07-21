package tonyd.gti785dataclient;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

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

public class ServerFiles extends NanoHTTPD {

    private static final String ipaddress = "192.168.43.182";
    private static final int port = 4000;

    private WebService service;
    private Context context;

    public ServerFiles(WebService wb, Context context, String ipaddress) {
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
                for (int i = 1 ; i < separated.length ; i++){
                    path += "/" + separated[i];
                }

                String finalPath = Environment.getExternalStorageDirectory().getAbsolutePath() + path;
                File pathFile = new File(finalPath);
                if (pathFile.isFile()){
                    FileInputStream fis = null;
                    String extension = MimeTypeMap.getFileExtensionFromUrl(uri);
                    String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                    try {
                        fis = new FileInputStream(pathFile);
                        return Response.newFixedLengthResponse(Status.OK, type, fis, (int)pathFile.length());

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
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
        }
        return Response.newFixedLengthResponse(Status.REQUEST_TIMEOUT, "text/html", "timeout");
    }

    /* Utilities */
    public String[] mySplit(final String input, final String delim) {
        return input.replaceFirst("^" + delim, "").split(delim);
    }
}
