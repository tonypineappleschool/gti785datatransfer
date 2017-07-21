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

public class ServerLocation extends NanoHTTPD {

    private static final String ipaddress = "192.168.43.182";
    private static final int port = 5000;

    private WebService service;
    private Context context;
    private LinkedBlockingQueue lbq;

    public ServerLocation(WebService wb, Context context, String ipaddress) {
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
            case Command.STATUS:
                return Response.newFixedLengthResponse("OK");
            case Command.POLL:
                Gson gson = new Gson();
                lbq = new LinkedBlockingQueue();
                Object o = null;
                try {
                    // wait for 60 s for there to be an object in the queue
                    o = lbq.poll(5, TimeUnit.SECONDS);
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
                return Response.newFixedLengthResponse(Status.REQUEST_TIMEOUT, "text/html", "timeout");
        }
        return Response.newFixedLengthResponse(Status.REQUEST_TIMEOUT, "text/html", "timeout");
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
