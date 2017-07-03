package tonyd.gti785dataclient;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;
import android.os.Debug;
import android.util.Log;

import com.google.gson.Gson;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by tonyd on 5/25/2017.
 */

public class Server extends NanoHTTPD {

    private static final String ipadress = "192.168.43.182";
    private static final int port = 5000;

    private WebService service;
    private Context context;

    public Server(WebService wb, Context context) {
        super(ipadress, port);
        service = wb;
        this.context = context;
    }

    @Override
    public Response serve(IHTTPSession session) {
        LinkedBlockingQueue lbq = new LinkedBlockingQueue();
        Object o = null;
        try {
            o = lbq.poll(1000, TimeUnit.MILLISECONDS);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (o == null){
            Log.d("LBQ", "Poll returns null");

        } else {

        }
        return Response.newFixedLengthResponse("8000");
    }

}
