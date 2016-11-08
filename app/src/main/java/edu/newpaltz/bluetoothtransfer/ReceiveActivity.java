package edu.newpaltz.bluetoothtransfer;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Bryan R Martinez on 11/3/2016.
 */
public class ReceiveActivity extends AppCompatActivity {
    public static final int DISABLED_BLUETOOTH = 0;
    public static final int MESSAGE_READ = 1;
    public static final int CONNECTION_FINISHED = 2;
    public static final int CONNECTION_FAILED = 3;
    public static final int CONNECTION_FAILED_SERVER = 4;
    public static final int CONNECTION_FAILED_TIMEOUT = 5;
    private static final int REQUEST_READ_AND_WRITE_EXTERNAL_STORAGE = 9;
    private static final String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private CountDownTimer timer;
    private ServerConnect serverConnect;
    private AlertDialog alertDialog;
    private Button sendButton;
    private TextView textView;
    private Drawable background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        sendButton = (Button) findViewById(R.id.saveButton);
        background = sendButton.getBackground();
        sendButton.setBackgroundColor(Button.INVISIBLE);

        checkBluetoothStatus();
    }

    // Checks state of Bluetooth adapter
    private void checkBluetoothStatus() {
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled())
            handler.obtainMessage(ReceiveActivity.DISABLED_BLUETOOTH).sendToTarget();
        else {
            initComponents();
        }
    }

    // Initialize components if Bluetooth adapter is enabled
    private void initComponents() {
        textView = (TextView) findViewById(R.id.textView);
        textView.setText("");
        textView.setTypeface(Typeface.MONOSPACE);

        serverConnect = new ServerConnect(handler);
        serverConnect.start();

        // Dialog allows user to see time until server timeout
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getResources().getString(R.string.connection_title));
        alertDialog.setMessage("00:30");
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();

        // Timer keeps track of time until server timeout
        timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                alertDialog.setMessage("00:" + String.format(
                                            Locale.getDefault(), "%02d", millisUntilFinished/1000));
            }

            @Override
            public void onFinish() {
                if (alertDialog.isShowing())
                    alertDialog.dismiss();
                handler.obtainMessage(ReceiveActivity.CONNECTION_FAILED_TIMEOUT).sendToTarget();
            }
        }.start();
    }

    // Handler used to communicate with threads
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ReceiveActivity.DISABLED_BLUETOOTH:
                    showErrorDialog(getResources().getString(R.string.error_message_00));
                    break;
                case ReceiveActivity.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    textView.append(readMessage);
                    break;
                case ReceiveActivity.CONNECTION_FINISHED:
                    if (alertDialog.isShowing())
                        alertDialog.dismiss();
                    timer.cancel();
                    sendButton.setText(getResources().getString(R.string.button_save));
                    sendButton.setBackground(background);
                    sendButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!sendButton.getText().toString().equals("")) {
                                int check1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                                        Manifest.permission.READ_EXTERNAL_STORAGE);
                                int check2 = ContextCompat.checkSelfPermission(getApplicationContext(),
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE);

                                if (check1 == PackageManager.PERMISSION_GRANTED &&
                                        check2 == PackageManager.PERMISSION_GRANTED) {
                                    writeToFile();
                                }
                                else {
                                    Toast.makeText(getApplicationContext(),
                                            "This app requires file access permissions.", Toast.LENGTH_LONG).show();
                                    ActivityCompat.requestPermissions(ReceiveActivity.this, PERMISSIONS,
                                            REQUEST_READ_AND_WRITE_EXTERNAL_STORAGE);
                                }
                            }
                        }
                    });
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.message_success_toast),
                            Toast.LENGTH_LONG).show();
                    break;
                case ReceiveActivity.CONNECTION_FAILED:
                    showErrorDialog(getResources().getString(R.string.error_message_01));
                    break;
                case ReceiveActivity.CONNECTION_FAILED_SERVER:
                    showErrorDialog(getResources().getString(R.string.error_message_02));
                    break;
                case ReceiveActivity.CONNECTION_FAILED_TIMEOUT:
                    showErrorDialog(getResources().getString(R.string.error_message_03));
                    break;
                default:
                    break;
            }
        }
    };

    // Displays alert dialog with custom error message
    private void showErrorDialog(String msg) {
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.error_title))
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void writeToFile() {
        File file;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "BluetoothTransfer");
            if (!dir.exists())
                dir.mkdirs();

            String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String name = "file_" + date + ".txt";

            file = new File(dir.getPath() + "/" + name);

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) { }
            }

            try {
                FileWriter writer = new FileWriter(file);
                writer.write(textView.getText().toString());
                writer.close();
                Toast.makeText(getApplicationContext(),
                        "Saved to \""+file.getPath()+"\"", Toast.LENGTH_LONG).show();
            } catch (IOException e) { }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_AND_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    writeToFile();
                } else {
                    new android.app.AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("Permission to read files not granted.")
                            .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    System.exit(0);
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
        else {
            handler.removeCallbacksAndMessages(null);
            if (timer != null)
                timer.cancel();
            if (serverConnect != null)
                serverConnect.cancel();
            this.finish();
        }
    }
}
