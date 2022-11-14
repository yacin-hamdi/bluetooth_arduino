package com.example.myapplication;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by yacin socketSearch 5/14/2020.
 */

public class SendReceive extends Thread {
    private BluetoothSocket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    int count_bytes = 0;
    private MainActivity main;


    public SendReceive(MainActivity main,BluetoothSocket socket){
        this.socket = socket;
        this.main = main;

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        inputStream = tmpIn;
        outputStream = tmpOut;
    }

    @Override
    public void run() {

        while(true){
            try {
                int byteCount = inputStream.available();
                if (byteCount > 0) {
                    byte[] rawBytes = new byte[1];
                    inputStream.read(rawBytes);
                    final String string = new String(rawBytes, "UTF-8");


                    main.handler.post(new Runnable() {
                        public void run()
                        {
                            main.received += string.toString();
                            if(string.toString().equals("#")){
                                main.temp = main.received.split(":")[0]+" °C";
                                main.humdAir = main.received.split(":")[1].split("#")[0]+" %";
                                main.received = "";
                            }


                        }
                    });


                }

                } catch(IOException e){
                    e.printStackTrace();
                }
            }

    }

    public void write(byte[] bytes){
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancel(){
        inputStream = null;
        outputStream = null;
    }
}
