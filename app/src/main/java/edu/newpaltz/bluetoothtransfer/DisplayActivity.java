package edu.newpaltz.bluetoothtransfer;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.util.UUID;

/**
 * Created by Bryan R Martinez on 11/3/2016.
 */
public class DisplayActivity extends AppCompatActivity {
    public static final UUID APP_UUID = UUID.fromString("f1239387-98b2-4dce-a7d5-635ce03572a0");
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    // "Enable Bluetooth" button's action
    public void enableBluetooth(View view) {
        if (bluetoothAdapter == null) {
            showNoBluetoothSupportDialog();
        }
        else {
            if (!bluetoothAdapter.isEnabled())
                bluetoothAdapter.enable();
                Toast.makeText(getApplicationContext(),
                        "Bluetooth is enabled",
                        Toast.LENGTH_SHORT)
                        .show();
        }
    }

    // "Receive Text" button's action
    public void receiveClicked(View view) {
        if (bluetoothAdapter == null) {
            showNoBluetoothSupportDialog();
        }
        else {
            Intent intent = new Intent(this, ReceiveActivity.class);
            startActivity(intent);
        }
    }

    // Displays alert dialog indicating lack of Bluetooth support on device
    private void showNoBluetoothSupportDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.error_title))
                .setMessage(getResources().getString(R.string.no_bt_adapter))
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
