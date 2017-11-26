package com.zhuhc.www.grapplerobot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private WifiManager wifiManager;
    // 服务器管理器
    private DhcpInfo dhcpInfo;
    // Robot ip
    private String IPRobot;
    private boolean isWiFiConnect;
    private WiFiSocket wifiSocket;
    private boolean isGeneralMessage = false;

    private RockerView rocker;
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WiFi_Init();
        Rocker_Init();
        SendGeneralMessage();
    }

    public void ButtonClick(View view) {
        if(isWiFiConnect == true) {
            wifiSocket.SendData();
        }
    }

    private void WiFi_Init() {
        // 得到服务器的IP地址
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(wifiNetworkInfo.isConnected()) {
            isWiFiConnect = true;
            WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
            wifiManager.getWifiState();
            dhcpInfo = wifiManager.getDhcpInfo();
            IPRobot = Formatter.formatIpAddress(dhcpInfo.gateway);
            Toast.makeText(MainActivity.this, "" + IPRobot, Toast.LENGTH_SHORT).show();
            Log.e("Debug", "IPRobot:" + IPRobot);

            wifiSocket = new WiFiSocket();
            wifiSocket.Connect(reHandler, IPRobot);
        } else {
            isWiFiConnect = false;
            Log.e("Debug", "Wifi No Connect!");
        }
    }

    private void Rocker_Init() {
        rocker = findViewById(R.id.rockerView);
        textView = findViewById(R.id.textView);
        if (rocker != null) {
            rocker.setCallBackMode(RockerView.CallBackMode.CALL_BACK_MODE_STATE_CHANGE);
            rocker.setOnShakeListener(RockerView.DirectionMode.DIRECTION_8, new RockerView.OnShakeListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void direction(RockerView.Direction direction) {

                }

                @Override
                public void onFinish() {


                }
            });

            rocker.setOnAngleChangeListener(new RockerView.OnAngleChangeListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void angle(double angle) {
                    textView.setText("角度: " + Double.toString(angle));
                    if(angle >= 225 && angle < 315) {
                        //上
                        wifiSocket.robot.Motor[0] = 100;
                        wifiSocket.robot.Motor[1] = 0;
                        wifiSocket.robot.Motor[2] = 100;
                        wifiSocket.robot.Motor[3] = 0;
                    } else if (angle >= 45 && angle < 135) {
                        //下
                        wifiSocket.robot.Motor[0] = -100;
                        wifiSocket.robot.Motor[1] = 0;
                        wifiSocket.robot.Motor[2] = -100;
                        wifiSocket.robot.Motor[3] = 0;
                    } else if (angle >= 135 && angle < 225) {
                        //左
                        wifiSocket.robot.Motor[0] = 0;
                        wifiSocket.robot.Motor[1] = 100;
                        wifiSocket.robot.Motor[2] = 0;
                        wifiSocket.robot.Motor[3] = 100;
                    } else{
                        //右
                        wifiSocket.robot.Motor[0] = 0;
                        wifiSocket.robot.Motor[1] = -100;
                        wifiSocket.robot.Motor[2] = 0;
                        wifiSocket.robot.Motor[3] = -100;
                    }
                }

                @Override
                public void onFinish() {
                    textView.setText("角度: " + Double.toString(0.0));
                    wifiSocket.robot.Motor[0] = 0;
                    wifiSocket.robot.Motor[1] = 0;
                    wifiSocket.robot.Motor[2] = 0;
                    wifiSocket.robot.Motor[3] = 0;
                }
            });
        }
    }

    public void SendGeneralMessage() {
        isGeneralMessage = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(isGeneralMessage) {
                    try {
                        Thread.sleep(50);
                        if(isWiFiConnect)
                             wifiSocket.SendData();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    // 接受显示Robot发送的数据
    @SuppressLint("HandlerLeak")
    private Handler reHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == WiFiSocket.HANDLER_RETURN_DATA) {
                Toast.makeText(MainActivity.this, "收到数据", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
