package com.zhuhc.www.grapplerobot;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Zhuhc on 2017/11/15.
 */

public class WiFiSocket {
    public static final int HANDLER_RETURN_DATA = 1;
    public final byte PacketHeader = (byte)0xAA;
    public final byte PacketTail = (byte)0xBB;
    public final byte PacketLength = 19;
    public byte CheckValue;

    public Robot robot;
    private final int Port = 54321;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Socket socket;
    private Handler reHandler;
    private byte[] returnData = new byte[20];
    private String RobotIP = "";

    public WiFiSocket() {
        robot = new Robot();
    }

    public void Connect(Handler handler, String IP) {
        this.reHandler = handler;
        this.RobotIP = IP;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(RobotIP, Port);
                    Log.e("Debug", "Socket Connect!");
                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();
                    reThread.start();
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private Thread reThread = new Thread(new Runnable() {
        @Override
        public void run() {
            // TODO Auto1-generated method stub
            while (socket != null && !socket.isClosed()) {
                try {
                    inputStream.read(returnData);
                    Message msg = new Message();
                    msg.what = HANDLER_RETURN_DATA;
                    msg.obj = returnData;
                    reHandler.sendMessage(msg);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    });

    public void SendData() {
        // 发送数据字节数组
        final byte[] sbyte = new byte[PacketLength];
        sbyte[0] = PacketHeader;
        sbyte[1] = robot.GetMotor(0);
        sbyte[2] = robot.GetMotor(1);
        sbyte[3] = robot.GetMotor(2);
        sbyte[4] = robot.GetMotor(3);
        sbyte[5] = 1;    //舵机0
        sbyte[6] = 2;    //舵机1
        sbyte[7] = 3;    //舵机2
        sbyte[8] = 4;    //舵机3
        sbyte[9] = 5;    //舵机4
        sbyte[10] = 6;   //舵机5
        sbyte[11] = 7;   //舵机6
        sbyte[12] = 8;   //舵机7
        sbyte[13] = 9;   //舵机8
        sbyte[14] = 0;   //红外
        sbyte[15] = 0;   //LED
        sbyte[16] = 0;   //Clear
        sbyte[17] = (byte)(sbyte[0]  ^ sbyte[1]  ^ sbyte[2]  ^ sbyte[3]  ^ sbyte[4]  ^ sbyte[5]  ^ sbyte[6]
                         ^ sbyte[7]  ^ sbyte[8]  ^ sbyte[9]  ^ sbyte[10] ^ sbyte[11] ^ sbyte[12] ^ sbyte[13]
                         ^ sbyte[14] ^ sbyte[15] ^ sbyte[16] ^ sbyte[17] ^ sbyte[18]);
        sbyte[18] = PacketTail;

        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    if(socket != null && !socket.isClosed()) {
                        outputStream.write(sbyte, 0, sbyte.length);
                        outputStream.flush();

                        //Log.e("Debug", Byte.toString(sbyte[1]) + "-" + Byte.toString(sbyte[2]) + "-" +
                        //                         Byte.toString(sbyte[3]) + "-" + Byte.toString(sbyte[4]));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
