package com.zhuhc.www.grapplerobot;

/**
 * Created by Administrator on 2017/11/17.
 */

public class Robot {
    public byte[] Motor = new byte[4];

    public Robot() {

    }

    public byte GetMotor(int num) {
        if(num < 0 || num > 3)
            num = 0;

        if(Motor[num] > 100)
            return 100;
        else if(Motor[num] < -100)
            return -100;
        else
            return Motor[num];
    }
}
