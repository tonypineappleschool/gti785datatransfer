package tonyd.gti785dataclient;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class MainActivity extends AppCompatActivity implements WebServiceCallbacks {

    private String ipAddress;
    private String port;

    /* Web Service */
    private WebService webService;
    private boolean bound;
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebService.MyBinder myBinder = (WebService.MyBinder) service;
            webService = myBinder.getService();
            bound = true;
            webService.setCallbacks(MainActivity.this); // register
            Toast.makeText(MainActivity.this, "Connected to WebService", Toast.LENGTH_SHORT).show();
        }
    };
    public static int WHITE = 0xFFFFFFFF;
    public static int BLACK = 0xFF000000;
    public static String jsonString = "{" +
            "'id':123456, " +
            "'name':'Tony', " +
            "'ip':'192.168.1.1', " +
            "'port':1994, " +
            "'lastAccessed':'04/09/1994', " +
            "'location':'Montreal' " +
            "}";

    private int imageWidth;
    ImageView QRCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        QRCode = (ImageView) findViewById(R.id.qr_code);
        displayQRCode();
        // Start the WebService to handle the server in a different thread
        Intent intent = new Intent(this, WebService.class);
        boolean result = bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Toast.makeText(getApplicationContext(), Boolean.toString(result),
                Toast.LENGTH_SHORT).show();
    }

    private void displayQRCode() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        imageWidth = (int)Math.round(width*0.8);
        QRCode.setMaxHeight(imageWidth);
        QRCode.setMaxWidth(imageWidth);
        try {
            Bitmap bitmap = encodeAsBitmap(jsonString);
            QRCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, imageWidth, imageWidth, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, imageWidth, 0, 0, w, h);
        return bitmap;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.generate:
                displayDialogQRCode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayDialogQRCode() {
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_layout);
        dialog.setTitle("GENERATE QR CODE");

        final EditText id_edit_text = (EditText) dialog.findViewById(R.id.id_edit_text);
        final EditText name_edit_text = (EditText) dialog.findViewById(R.id.name_edit_text);
        final EditText ip_edit_text = (EditText) dialog.findViewById(R.id.ip_edit_text);
        final EditText port_edit_text = (EditText) dialog.findViewById(R.id.port_edit_text);
        final EditText last_accessed_edit_text = (EditText) dialog.findViewById(R.id.last_accessed_edit_text);
        final EditText location_edit_text = (EditText) dialog.findViewById(R.id.location_edit_text);

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        ipAddress = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        port = "5000";

        ip_edit_text.setText(ipAddress);
        port_edit_text.setText(port);
        Button dialogButton = (Button) dialog.findViewById(R.id.ok_generate_button);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jsonString = jsonString(
                        id_edit_text.getText().toString(),
                        name_edit_text.getText().toString(),
                        ip_edit_text.getText().toString(),
                        port_edit_text.getText().toString(),
                        last_accessed_edit_text.getText().toString(),
                        location_edit_text.getText().toString()
                );
                displayQRCode();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private String jsonString(String id, String name, String ip,
                              String port, String lastAccessed, String location){
        String jsonString = "{" +
                "'id':" + id  +", " +
                "'name': '" + name + "', " +
                "'ip':'" + ip + "', " +
                "'port':" + port + ", " +
                "'lastAccessed':'" + lastAccessed + "', " +
                "'location':'" + location + "' " +
                "}";
        return jsonString;
    }

}
