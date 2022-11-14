package com.example.myapplication;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity {

    public String temp ="", humdAir="";


    public TextView state, modeOn;
    public Button connect;
    public RelativeLayout ventilateur, servo, pompe;

    public ImageView ventImgOn, pompeImgOn, servoImgOn;
    public TextView ventOn, pompeOn, servoOn;

    public boolean isVentOn = false, isPompeOn = false, isServoOn = false;

    public TextView temperatureAir, humidityAir;


    public boolean isConnected = false;
    private byte[] buffer;
    private boolean stopThread;
    public final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothAdapter bluetoothAdapter = null;
    BluetoothSocket socket = null;
    public BluetoothDevice mDevice = null;
    ClientBluetooth clientBluetooth;
    public String received;
    private Runnable visualThread;
    public Handler handler, handler2;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ventilateur = (RelativeLayout) findViewById(R.id.ventilateur);
        servo = (RelativeLayout) findViewById(R.id.servo);
        pompe = (RelativeLayout) findViewById(R.id.pompe);

        connect = (Button) findViewById(R.id.connect);

        state = (TextView) findViewById(R.id.state);
        modeOn = (TextView) findViewById(R.id.textMode);

        pompeOn = (TextView) findViewById(R.id.pompeOn);
        pompeImgOn = (ImageView) findViewById(R.id.pompeImgOn);

        ventOn = (TextView) findViewById(R.id.ventOn);
        ventImgOn = (ImageView) findViewById(R.id.ventImgOn);

        servoOn = (TextView) findViewById(R.id.servoOn);
        servoImgOn = (ImageView) findViewById(R.id.servoImgOn);

        humidityAir = (TextView) findViewById(R.id.humidity_air);
        temperatureAir = (TextView) findViewById(R.id.temperature_air);


        handler = new Handler();
        visualThread = new Runnable() {
            @Override
            public void run() {
                if (isConnected) {

                    humidityAir.setText(humdAir);

                    temperatureAir.setText(temp);

                    state.setText("connected");
                    if (isVentOn) {
                        ventOn.setText("on");
                        ventImgOn.setColorFilter(Color.parseColor("#04F40E"));
                    } else {
                        ventOn.setText("off");
                        ventImgOn.setColorFilter(Color.parseColor("#FF000000"));
                    }
                    if (isPompeOn) {
                        pompeOn.setText("on");
                        pompeImgOn.setColorFilter(Color.parseColor("#04F40E"));
                    } else {
                        pompeOn.setText("off");
                        pompeImgOn.setColorFilter(Color.parseColor("#FF000000"));
                    }
                    if (isServoOn) {
                        servoOn.setText("on");
                        servoImgOn.setColorFilter(Color.parseColor("#04F40E"));
                    } else {
                        servoOn.setText("off");
                        servoImgOn.setColorFilter(Color.parseColor("#FF000000"));
                    }


                } else {
                    state.setText("disconnected");
                }

                handler.postDelayed(this, 100);
            }
        };
        handler.postDelayed(visualThread, 100);


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        checkPermissions();
        checkBluetooth();


        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                        bluetoothConnect();



                }

        });


        ventilateur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    if (isConnected) {
                        if (!isVentOn) {
                            clientBluetooth.sendText("0");
                            isVentOn = true;
                        } else {
                            clientBluetooth.sendText("1");
                            isVentOn = false;
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "your not connected", Toast.LENGTH_SHORT).show();
                    }

            }
        });

        servo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    if (isConnected) {
                        if (!isServoOn) {
                            clientBluetooth.sendText("4");
                            isServoOn = true;
                        } else {
                            clientBluetooth.sendText("5");
                            isServoOn = false;
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "your not connected", Toast.LENGTH_SHORT).show();
                    }


            }
        });

        pompe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    if (isConnected) {
                        if (!isPompeOn) {
                            clientBluetooth.sendText("2");
                            isPompeOn = true;
                        } else {
                            clientBluetooth.sendText("3");
                            isPompeOn = false;
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "your not connected", Toast.LENGTH_SHORT).show();
                    }


            }
        });

    }

    public void checkBluetooth() {
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "bluetooth device not available", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED){
                    Intent turnBlueOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(turnBlueOn, 1);
                }



            }
        }
    }

    private void bluetoothConnect(){
        if (!bluetoothAdapter.isEnabled()) {
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED){
                Intent turnBlueOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBlueOn, 1);
            }

        } else {
            if (!isConnected) {

                mDevice = pairedDeviceMethod();
                if(mDevice != null){
                    clientBluetooth = new ClientBluetooth(MainActivity.this);
                    Toast.makeText(getApplicationContext(), "connecting...", Toast.LENGTH_SHORT).show();

                    clientBluetooth.start();
                }else{
                    Toast.makeText(getApplicationContext(), "there is no paired device", Toast.LENGTH_SHORT).show();
                }


            } else {
                clientBluetooth.cancel();
            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "permission granted3", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        if (grantResults.length > 0) {
            boolean bluetoothPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (bluetoothPermission) {
                Toast.makeText(this, "permissions Granted...", Toast.LENGTH_SHORT).show();
            }
        }


    }


    public void checkPermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(getApplicationContext().checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(MainActivity.this, "Permission granted 1", Toast.LENGTH_SHORT).show();

            }else{
                ActivityCompat.requestPermissions(
                        this,
                        new String[]
                                {
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                                        Manifest.permission.BLUETOOTH,
                                        Manifest.permission.BLUETOOTH_CONNECT,
                                        Manifest.permission.BLUETOOTH_ADMIN,
                                        Manifest.permission.BLUETOOTH_SCAN
                                }, 0);
            }
        }else{
            Toast.makeText(MainActivity.this, "permission granted 2", Toast.LENGTH_SHORT).show();
        }
    }



    public BluetoothDevice pairedDeviceMethod() {
        BluetoothDevice mdevice = null;
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED){
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                if(pairedDevices.size()>0){
                    for(BluetoothDevice device:pairedDevices){
                        mdevice=device;
                    }
                }
        }






        return mdevice;

    }

   















}