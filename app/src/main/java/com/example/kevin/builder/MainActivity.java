package com.example.kevin.builder;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView txtInfo;
    private EditText edtx;
    private EditText edty;
    private Button addx, addy, subx, suby;
    private Button btnWrite, btnSample;
    private AP[] APList; // set the AP's Info, including BSSID, SSID
    private int AP_NUMBER = 6; //Number of APs
    private ArrayList<FingerPrint> fps; //record a fingerprinting data, according to AP number
    private FingerPrint wrtfp; //fingerprinting write to database

    //wifi objects
    private final int SCAN_TIME = 20; //How many times to scan at a fixed location
    private final int SIG_LOSS = -100;//determine by your device
    private int counter = 0;
    private WifiManager wifi;
    private WifiReceiver wifiReceiver;
    private List<ScanResult> scanResult;

    //database objects
    static final String db_name = "WirelessLocation.db";
    static final String tb_name = "data";
    SQLiteDatabase db;

    //coordinate value
    private int x, y; //(x,y)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create a database and make it world_readable (can be access by another APP)
        db = openOrCreateDatabase(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + db_name, Context.MODE_WORLD_READABLE, null);

        //SQL command for creating table form, once you change AP_NUMBER, this must be updated
        String createTable = "CREATE TABLE IF NOT EXISTS " + tb_name +
                "(X INTEGER, " +
                "Y INTEGER, " +
                "AP1 INTEGER, " +
                "AP2 INTEGER, " +
                "AP3 INTEGER, " +
                "AP4 INTEGER, " +
                "AP5 INTEGER, " +
                "AP6 INTEGER)";

        db.execSQL(createTable);

        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (!wifi.isWifiEnabled()) {
            //enable wifi module
            wifi.setWifiEnabled(true);
        }

        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifi.startScan();

        APList = new AP[AP_NUMBER];
        for (int i = 0; i < AP_NUMBER; i++) {
            APList[i] = new AP();
        }

        txtInfo = (TextView) findViewById(R.id.info);

        edtx = (EditText) findViewById(R.id.edtx);
        edty = (EditText) findViewById(R.id.edty);
        addx = (Button) findViewById(R.id.add_x);
        subx = (Button) findViewById(R.id.sub_x);
        addy = (Button) findViewById(R.id.add_y);
        suby = (Button) findViewById(R.id.sub_y);
        btnSample = (Button) findViewById(R.id.btnSample);
        btnWrite = (Button) findViewById(R.id.btnWrite);
        btnSample = (Button) findViewById(R.id.btnSample);

        addx.setOnClickListener(this);
        subx.setOnClickListener(this);
        addy.setOnClickListener(this);
        suby.setOnClickListener(this);
        btnSample.setOnClickListener(this);
        btnWrite.setOnClickListener(this);

        //initial (x,y) to (0,0)
        x = Integer.parseInt(edtx.getText().toString());
        y = Integer.parseInt(edty.getText().toString());

        AP_Setting(); //set AP's MAC、SSID Information

        fps = new ArrayList<FingerPrint>();
    }

    @Override
    public void onResume() {
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.add_x:
                x++;
                edtx.setText(String.valueOf(x));
                break;

            case R.id.sub_x:
                if (x > 0) x--;
                edtx.setText(String.valueOf(x));
                break;

            case R.id.add_y:
                y++;
                edty.setText(String.valueOf(y));
                break;

            case R.id.sub_y:
                if (y > 0) y--;
                edty.setText(String.valueOf(y));
                break;

            case R.id.btnSample:
                fps.clear();
                wrtfp.setLocation(Integer.parseInt(edtx.getText().toString()), Integer.parseInt(edty.getText().toString()));
                wifi.startScan();
                break;

            case R.id.btnWrite:
                write();
                break;
        }

    }


    public class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            /*
            BSSID format looks like this(all in lower case)
            a0:f3:c1:6b:bd:de
            */

            scanResult = wifi.getScanResults();
            FingerPrint fp = new FingerPrint(AP_NUMBER);


            for (int i = 0; i < scanResult.size(); i++) {
                for (int j = 0; j < AP_NUMBER; j++) {

                    if (scanResult.get(i).BSSID.equals(APList[j].getMAC())) {
                        //MAC is the same
                        fp.signals[j] = scanResult.get(i).level;
                    }
                }
            }

            //correct the lost signal strength
            for (int i = 0; i < AP_NUMBER; i++) {
                if (fp.signals[i] == 0) {
                    fp.signals[i] = SIG_LOSS;
                }
            }

            fps.add(fp);
            counter++;

            if (counter < SCAN_TIME) {
                //have not achieved scanning time defined by user
                wifi.startScan();
            } else {
                wrtfp = cal_avg();
                showInfo();
                counter = 0;
                //finished. show hint on screen
                Toast.makeText(MainActivity.this, "Finish Scanning.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    void AP_Setting() {

        //add elements into APList[] according to the amount you need
        APList[0].setMAC("a0:f3:c1:6b:bd:de");
        APList[1].setMAC("");
        APList[2].setMAC("");
        APList[3].setMAC("");
        APList[4].setMAC("");
        APList[5].setMAC("");
    }

    void showInfo() {

        for (int i = 0; i < AP_NUMBER; i++) {
            txtInfo.append("\n" + "AP" + (i + 1) + ": " + wrtfp.signals[i] + " db");
        }

    }

    void write() {

        //object includes x, y, and every AP's signal information
        ContentValues cv = new ContentValues(AP_NUMBER + 2);
        cv.put("X", wrtfp.getX());
        cv.put("Y", wrtfp.getY());

        for (int i = 0; i < AP_NUMBER; i++) {

            cv.put("AP" + String.valueOf(i + 1), wrtfp.signals[i]);
        }

        db.insert(tb_name, null, cv);

        Toast.makeText(MainActivity.this, "Write Success.", Toast.LENGTH_SHORT).show();

    }

    FingerPrint cal_avg() {
        //calculate average fingerprinting values of elements in fps
        FingerPrint avg = new FingerPrint(AP_NUMBER);
        for (int i = 0; i < fps.size(); i++) {
            //fps's size is SCAN_TIME
            for (int j = 0; j < AP_NUMBER; j++) {
                //sum
                avg.signals[j] += fps.get(i).signals[j];
            }
        }

        for (int i = 0; i < AP_NUMBER; i++) {
            //avg
            avg.signals[i] /= fps.size();
        }

        return avg;
    }

}
