package edu.newpaltz.bluetoothtransfer;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Bryan R Martinez on 11/3/2016.
 */
public class ManageConnect extends Thread {
    private BluetoothSocket socket;
    private InputStream inStream;
    private OutputStream outStream;
    private Handler handler;

    public ManageConnect(BluetoothSocket socket, Handler handler) {
        this.handler = handler;
        this.socket = socket;
        inStream = null;
        outStream = null;

        try {
            if (socket != null) {
                inStream = socket.getInputStream();
                outStream = socket.getOutputStream();
            }
        } catch (IOException e) {
            handler.obtainMessage(ReceiveActivity.CONNECTION_FAILED).sendToTarget();
        }
    }

    @Override
    public void run() { }

    public void write(byte[] data) {
        if (outStream != null) {
            try {
                outStream.write(data);
            } catch (IOException e) {
                cancel();
            }
        }
        cancel();
    }

    public void read() {
        byte[] buffer = new byte[1024];
        int bytes;

        while (true) {
            try {
                bytes = inStream.read(buffer);
                handler.obtainMessage(ReceiveActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
            } catch (IOException e) {
                break;
            }
        }
        handler.obtainMessage(ReceiveActivity.CONNECTION_FINISHED).sendToTarget();
        cancel();
    }

    public void cancel() {
        try {
            socket.close();
        } catch (IOException e) { }
    }
}
