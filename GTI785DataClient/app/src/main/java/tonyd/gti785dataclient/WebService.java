package tonyd.gti785dataclient;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by tonyd on 5/28/2017.
 */

public class WebService extends Service {

    private final IBinder mBinder = new MyBinder();
    private Server server;
    private WebServiceCallbacks webServiceCallbacks;
    private LocalBroadcastManager broadcaster;

    @Override
    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        server = new Server(this, getApplicationContext());
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Service listening on port " + server.getListeningPort(), Toast.LENGTH_LONG).show();
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        server.closeAllConnections();
        server.stop();
    }

    public LocalBroadcastManager getBroadcaster() {
        return broadcaster;
    }

    public class MyBinder extends Binder {
        WebService getService() {
            return WebService.this;
        }
    }

    public WebServiceCallbacks getCallbacks(){
        return webServiceCallbacks;
    }

    public void setCallbacks(WebServiceCallbacks callbacks) {
        webServiceCallbacks = callbacks;
    }

}
