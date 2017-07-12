package tonyd.gti785dataclient;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;

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

    public Server(WebService wb, Context context) {
        super(ipaddress, port);
        service = wb;
        this.context = context;

    }

    @Override
    public Response serve(IHTTPSession session) {
        Gson gson = new Gson();
        lbq = new LinkedBlockingQueue();
        Object o = null;
        try {
            // wait for 10000 ms for there to be an object in the queue
            o = lbq.poll(10000, TimeUnit.MILLISECONDS);
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

    public LinkedBlockingQueue getLbq() {
        return lbq;
    }

    public void setLbq(LinkedBlockingQueue lbq) {
        this.lbq = lbq;
    }

}
