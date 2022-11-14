package com.example.myapplication;

import static android.Manifest.permission.BLUETOOTH;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ClientBluetooth extends Thread {
    private MainActivity main;
    public SendReceive sendReceive;

    public ClientBluetooth(MainActivity main) {
        this.main = main;
        BluetoothSocket tmp = null;

        try {

                tmp = main.mDevice.createRfcommSocketToServiceRecord(main.uuid);



        } catch (IOException e) {
            e.printStackTrace();
        }
        main.socket = tmp;
    }

    @Override
    public void run() {
        super.run();
        try {

                main.socket.connect();


            main.isConnected = true;
            sendReceive = new SendReceive(main,main.socket);
            sendReceive.start();
        }catch (IOException e){
            e.printStackTrace();
            try{
                main.socket.close();
                main.isConnected = false;
            }catch(IOException e1){
                e1.printStackTrace();
            }
        }
    }

    public void cancel(){
        try{
            if(main.socket != null)
                main.socket.close();
            main.isConnected = false;
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void sendText(String text){
        try{
            main.socket.getOutputStream().write(text.getBytes());
        }catch (IOException e){
            e.printStackTrace();
        }
    }


}
