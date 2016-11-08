package edu.newpaltz.bluetoothtransfer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;

/**
 * Created by Bryan R Martinez on 11/3/2016.
 */
public class ServerConnect extends Thread {
    private BluetoothServerSocket serverSocket;
    private Handler handler;

    public ServerConnect(Handler handler) {
        this.handler = handler;
        serverSocket = null;

        try {
            serverSocket = BluetoothAdapter.getDefaultAdapter()
                .listenUsingRfcommWithServiceRecord("Bluetooth Transfer", DisplayActivity.APP_UUID);
        } catch (IOException e) {
            handler.obtainMessage(ReceiveActivity.CONNECTION_FAILED_SERVER).sendToTarget();
        }
    }

    @Override
    public void run() {
        BluetoothSocket socket = null;
        if (serverSocket != null) {
            try {
                socket = serverSocket.accept(30000); // 30 seconds to establish connection
            } catch (IOException e) {
                cancel();
            }
            if (socket != null) {
                ManageConnect manageConnect = new ManageConnect(socket, handler);
                manageConnect.start();
                manageConnect.read();
                cancel();
            }
        }
    }

    public void cancel() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) { }
        }
    }
}
