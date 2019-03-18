package com.ciptagrafika.ratesetter.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ciptagrafika.ratesetter.App.CheckNetwork;
import com.ciptagrafika.ratesetter.App.OfflineActivity;
import com.ciptagrafika.ratesetter.R;
import com.ciptagrafika.ratesetter.helper.RequestHandler;
import com.ciptagrafika.ratesetter.printer.BluetoothHandler;
import com.ciptagrafika.ratesetter.printer.PrinterCommands;
import com.zj.btsdk.BluetoothService;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.ciptagrafika.ratesetter.App.AppConfig.URL_GET_FILEJADI;
import static com.ciptagrafika.ratesetter.App.AppConfig.URL_GET_SETTING;
import static com.ciptagrafika.ratesetter.App.AppConfig.URL_SET_NOJADI;
import static com.ciptagrafika.ratesetter.App.AppConfig.URL_SET_NOSETTING;
import static com.ciptagrafika.ratesetter.App.AppConfig.URL_SET_VISITOR_FILEJADI;
import static com.ciptagrafika.ratesetter.App.AppConfig.URL_SET_VISITOR_SETTING;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, BluetoothHandler.HandlerInterfce {

    //@BindView(R.id.et_text)
    //EditText etText;
    @BindView(R.id.tv_status)
    TextView tvStatus;

    private final String TAG = MainActivity.class.getSimpleName();
    public static final int RC_BLUETOOTH = 0;
    public static final int RC_CONNECT_DEVICE = 1;
    public static final int RC_ENABLE_BLUETOOTH = 2;

    private BluetoothService mService = null;
    private BluetoothDevice mDevice = null;
    private boolean isPrinterReady = false;

    private ProgressDialog pDialog;
    private AnimationDrawable animationDrawable;
    private RelativeLayout relativeLayout;
    private Button btnFileJadi, btnSetting, btnRating;
    private String JSON_STRING, text = "Test Print", currentDate, noSetting, noJadi, varPrint;
    private int nosetting, nojadi;
    Intent intent;
    ProgressDialog loading;
    JSONObject jsonobject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        animationDrawable = (AnimationDrawable) relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(5000);
        animationDrawable.setExitFadeDuration(2000);
        btnFileJadi = (Button) findViewById(R.id.btn_siap_print);
        btnSetting = (Button) findViewById(R.id.btn_setting);
        btnRating = (Button) findViewById(R.id.btn_rating);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        ButterKnife.bind(this);
        setupBluetooth();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        currentDate = sdf.format(new Date());

        btnFileJadi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printFileJadi();
            }
        });

        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printSetting();
            }
        });

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(MainActivity.this, RateActivity.class);
                startActivity(intent);
            }
        });
    }

    @AfterPermissionGranted(RC_BLUETOOTH)
    private void setupBluetooth() {
        String[] params = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN};
        if (!EasyPermissions.hasPermissions(this, params)) {
            EasyPermissions.requestPermissions(this, "You need bluetooth permission",
                    RC_BLUETOOTH, params);
            return;
        }
        mService = new BluetoothService(this, new BluetoothHandler(this, this));
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        // TODO: 10/11/17 do something
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // TODO: 10/11/17 do something
    }

    @Override
    public void onDeviceConnected() {
        isPrinterReady = true;
        tvStatus.setText("Terhubung dengan perangkat");
    }

    @Override
    public void onDeviceConnecting() {
        tvStatus.setText("Sedang menghubungkan...");
    }

    @Override
    public void onDeviceConnectionLost() {
        isPrinterReady = false;
        tvStatus.setText("Koneksi perangkat terputus");
    }

    @Override
    public void onDeviceUnableToConnect() {
        tvStatus.setText("Tidak dapat terhubung ke perangkat");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_ENABLE_BLUETOOTH:
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "onActivityResult: bluetooth aktif");
                } else
                    Log.i(TAG, "onActivityResult: bluetooth harus aktif untuk menggunakan fitur ini");
                break;
            case RC_CONNECT_DEVICE:
                if (resultCode == RESULT_OK) {
                    String address = data.getExtras().getString(DeviceActivity.EXTRA_DEVICE_ADDRESS);
                    mDevice = mService.getDevByMac(address);
                    mService.connect(mDevice);
                }
                break;
        }
    }

    public void printSetting() {
        if (!mService.isAvailable()) {
            Log.i(TAG, "printText: perangkat tidak support bluetooth");
            return;
        }
        if (isPrinterReady) {
            if (text.equals("")) {
                Toast.makeText(this, "Cant print null text", Toast.LENGTH_SHORT).show();
                return;
            }

            dataSetting();
        } else {
            if (mService.isBTopen())
                startActivityForResult(new Intent(this, DeviceActivity.class), RC_CONNECT_DEVICE);
            else
                requestBluetooth();
        }
    }

    public void printFileJadi() {
        if (!mService.isAvailable()) {
            Log.i(TAG, "printText: perangkat tidak support bluetooth");
            return;
        }
        if (isPrinterReady) {
            if (text.equals("")) {
                Toast.makeText(this, "Cant print null text", Toast.LENGTH_SHORT).show();
                return;
            }

            fileJadi();
        } else {
            if (mService.isBTopen())
                startActivityForResult(new Intent(this, DeviceActivity.class), RC_CONNECT_DEVICE);
            else
                requestBluetooth();
        }
    }

    private void requestBluetooth() {
        if (mService != null) {
            if (!mService.isBTopen()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, RC_ENABLE_BLUETOOTH);
            }
        }
    }

    private void dataSetting() {
        if (CheckNetwork.isInternetAvailable(MainActivity.this)) {
            class getData extends AsyncTask<Void, Void, String> {
                //ProgressDialog loading;
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    loading = ProgressDialog.show(MainActivity.this, "Get Data Antrian", "Wait...", false, false);
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    loading.dismiss();
                    JSON_STRING = s;
                    // Method Show Data
                    showDataSetting();
                }

                @Override
                protected String doInBackground(Void... params) {
                    RequestHandler rh = new RequestHandler();
                    String s = rh.sendGetRequest(URL_GET_SETTING);
                    return s;
                }
            }
            getData ge = new getData();
            ge.execute();
        } else {
            Intent intent = new Intent(MainActivity.this,
                    OfflineActivity.class);
            startActivity(intent);
        }
    }

    // Menampilkan Data Antrian
    private void showDataSetting() {
        jsonobject = null;
        try {
            jsonobject = new JSONObject(JSON_STRING);
            Log.d(TAG, "json respon: " + jsonobject);
            String value = jsonobject.getString("setting");
            Log.d(TAG, "json respon: " + value);

            nosetting = Integer.parseInt(value);
            nosetting++;
            noSetting = String.valueOf(nosetting);

            String no = noSetting;
            StringBuilder sb = new StringBuilder(3).append("B").append(noSetting);
            noSetting = sb.toString();

            varPrint = "B";
            printNomor(no, noSetting, varPrint);

            //Toast.makeText(getApplicationContext(), "No Sebelum: " + value, Toast.LENGTH_SHORT).show();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "json respon: error get data ");
        }
    }

    private void fileJadi() {
        if (CheckNetwork.isInternetAvailable(MainActivity.this)) {
            class getData extends AsyncTask<Void, Void, String> {
                //ProgressDialog loading;
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    loading = ProgressDialog.show(MainActivity.this, "Get Data Antrian", "Wait...", false, false);
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    loading.dismiss();
                    JSON_STRING = s;
                    // Method Show Data
                    showDataFile();
                }

                @Override
                protected String doInBackground(Void... params) {
                    RequestHandler rh = new RequestHandler();
                    String s = rh.sendGetRequest(URL_GET_FILEJADI);
                    return s;
                }
            }
            getData ge = new getData();
            ge.execute();
        } else {
            Intent intent = new Intent(MainActivity.this,
                    OfflineActivity.class);
            startActivity(intent);
        }
    }

    // Menampilkan Data Antrian
    private void showDataFile() {
        jsonobject = null;
        try {
            jsonobject = new JSONObject(JSON_STRING);
            Log.d(TAG, "json respon: " + jsonobject);
            String value = jsonobject.getString("jadi");
            Log.d(TAG, "json respon: " + value);

            nojadi = Integer.parseInt(value);
            nojadi++;
            noJadi = String.valueOf(nojadi);

            String no = noJadi;
            StringBuilder sb = new StringBuilder(3).append("A").append(noJadi);
            noJadi = sb.toString();

            varPrint = "A";
            printNomor(no, noJadi, varPrint);

            //Toast.makeText(getApplicationContext(), "No Sebelum: " + value, Toast.LENGTH_SHORT).show();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "json respon: error get data ");
        }
    }

    private void printNomor(String no, String nomor, String varprint) {

        mService.write(PrinterCommands.ESC_ALIGN_CENTER);
        mService.write(PrinterCommands.ESC_CANCEL_BOLD);
        mService.write(PrinterCommands.ESC_ENTER);
        mService.sendMessage("CIPTA GRAFIKA", "");
        mService.write(PrinterCommands.ESC_ENTER20);
        mService.sendMessage("No Antrian Anda", "");
        mService.write(PrinterCommands.ESC_ENTER3);
        mService.sendMessage(nomor, "");
        mService.write(PrinterCommands.ESC_ENTER3);
        mService.sendMessage("Waktu Antrian Anda", "");
        mService.sendMessage(currentDate, "");
        mService.write(PrinterCommands.ESC_ENTER);
        mService.sendMessage("''Terimakasih Telah Menunggu''", "");
        mService.write(PrinterCommands.ESC_ENTER50);

        if (varprint.equals("A")) {
            setDataJadi(no);
            setVisitorJadi();
        } else if (varprint.equals("B")) {
            setDataSetting(no);
            setVisitorSetting();
        } else {
            Log.d(TAG, "varprint null");
        }
    }

    private void setDataSetting(final String no) {
        if (CheckNetwork.isInternetAvailable(MainActivity.this)) {
            class getData extends AsyncTask<Void, Void, String> {
                //ProgressDialog loading;
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    loading = ProgressDialog.show(MainActivity.this, "Set Data Antrian", "Wait...", false, false);
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    loading.dismiss();
                    JSON_STRING = s;
                    // Method Show Data
                    showResultSetting();
                }

                @Override
                protected String doInBackground(Void... params) {
                    RequestHandler rh = new RequestHandler();
                    String s = rh.sendGetRequestParam(URL_SET_NOSETTING, no);
                    return s;
                }
            }
            getData ge = new getData();
            ge.execute();
        } else {
            Intent intent = new Intent(MainActivity.this,
                    OfflineActivity.class);
            startActivity(intent);
        }
    }

    private void setDataJadi(final String no) {
        if (CheckNetwork.isInternetAvailable(MainActivity.this)) {
            class getData extends AsyncTask<Void, Void, String> {
                //ProgressDialog loading;
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    loading = ProgressDialog.show(MainActivity.this, "Set Data Antrian", "Wait...", false, false);
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    loading.dismiss();
                    JSON_STRING = s;
                    // Method Show Data
                    showResultJadi();
                }

                @Override
                protected String doInBackground(Void... params) {
                    RequestHandler rh = new RequestHandler();
                    String s = rh.sendGetRequestParam(URL_SET_NOJADI, no);
                    return s;
                }
            }
            getData ge = new getData();
            ge.execute();
        } else {
            Intent intent = new Intent(MainActivity.this,
                    OfflineActivity.class);
            startActivity(intent);
        }
    }

    private void showResultSetting() {
        jsonobject = null;
        try {
            jsonobject = new JSONObject(JSON_STRING);
            //Log.d(TAG, "json respon: "+jsonobject);
            String status = jsonobject.getString("status");
            Log.d(TAG, "json respon: " + status);

            if (status.equals("setting succes")) {
                //Toast.makeText(getApplicationContext(), "JSON RESPON: " + status, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "JSON ERROR", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "json respon: error get data ");
        }
    }

    private void showResultJadi() {
        jsonobject = null;
        try {
            jsonobject = new JSONObject(JSON_STRING);
            //Log.d(TAG, "json respon: "+jsonobject);
            String status = jsonobject.getString("status");
            Log.d(TAG, "json respon: " + status);

            if (status.equals("jadi succes")) {
                //Toast.makeText(getApplicationContext(), "JSON RESPON: " + status, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "JSON ERROR", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "json respon: error get data ");
        }
    }

    private void setVisitorJadi() {
        if (CheckNetwork.isInternetAvailable(MainActivity.this)) {
            class getData extends AsyncTask<Void, Void, String> {
                //ProgressDialog loading;
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    //loading = ProgressDialog.show(MainActivity.this, "Set Data Pengunjung", "Wait...", false, false);
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    loading.dismiss();
                    JSON_STRING = s;
                    // Method Show Data
                    showDataVisitorFile();
                }

                @Override
                protected String doInBackground(Void... params) {
                    RequestHandler rh = new RequestHandler();
                    String s = rh.sendGetRequestParam(URL_SET_VISITOR_FILEJADI, "A");
                    return s;
                }
            }
            getData ge = new getData();
            ge.execute();
        } else {
            Intent intent = new Intent(MainActivity.this,
                    OfflineActivity.class);
            startActivity(intent);
        }
    }

    private void setVisitorSetting() {
        if (CheckNetwork.isInternetAvailable(MainActivity.this)) {
            class getData extends AsyncTask<Void, Void, String> {
                //ProgressDialog loading;
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    //loading = ProgressDialog.show(MainActivity.this, "Set Data Pengunjung", "Wait...", false, false);
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    loading.dismiss();
                    JSON_STRING = s;
                    // Method Show Data
                    showDataVisitorSetting();
                }

                @Override
                protected String doInBackground(Void... params) {
                    RequestHandler rh = new RequestHandler();
                    String s = rh.sendGetRequestParam(URL_SET_VISITOR_SETTING, "A");
                    return s;
                }
            }
            getData ge = new getData();
            ge.execute();
        } else {
            Intent intent = new Intent(MainActivity.this,
                    OfflineActivity.class);
            startActivity(intent);
        }
    }

    // Menampilkan Data Antrian
    private void showDataVisitorFile() {
        jsonobject = null;
        try {
            jsonobject = new JSONObject(JSON_STRING);
            Log.d(TAG, "set visitor respon: " + jsonobject);

            //Toast.makeText(getApplicationContext(), "No Sebelum: " + value, Toast.LENGTH_SHORT).show();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "set visitor respon: error set data ");
        }
    }

    private void showDataVisitorSetting() {
        jsonobject = null;
        try {
            jsonobject = new JSONObject(JSON_STRING);
            Log.d(TAG, "set visitor respon: " + jsonobject);

            //Toast.makeText(getApplicationContext(), "No Sebelum: " + value, Toast.LENGTH_SHORT).show();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "set visitor respon: error set data ");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (animationDrawable != null && !animationDrawable.isRunning())
            animationDrawable.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (animationDrawable != null && animationDrawable.isRunning())
            animationDrawable.stop();
    }

}
