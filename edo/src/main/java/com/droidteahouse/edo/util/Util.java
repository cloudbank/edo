package com.droidteahouse.edo.util;

public class Util {


    public static int countBits(int v) {
        //long startTime = System.nanoTime();

        int tmp = 0;
        tmp = v - ((v >> 1) & 033333333333) - ((v >> 2) & 011111111111);
        // System.out.println(((tmp + (tmp >> 3)) & 030707070707) % 63);
        // System.out.println(System.nanoTime() - startTime);
        return ((tmp + (tmp >> 3)) & 030707070707) % 63;
    }

    /*(
        v = v - ((v >> 1) & 0x55555555);                    // reuse input as temporary
        v = (v & 0x33333333) + ((v >> 2) & 0x33333333);     // temp
        c = ((v + (v >> 4) & 0xF0F0F0F) * 0x1010101) >> 24;


        long v = v - ((v >> 1) & ~0L / 3);                           // temp
        v =(v &~0L/15*3)+((v >>2)&~0L/15*3);      // temp
        v =(v +(v >>4))&~0L/255*15;                      // temp
        long c = (v * (~0L / 255)) >> (64 - 1) * 4; // count
    */
    public static int bitCount(long i) {
        i = i - ((i >>> 1) & 0x5555555555555555L);
        i = (i & 0x3333333333333333L) + ((i >>> 2) & 0x3333333333333333L);
        i = (i + (i >>> 4)) & 0x0f0f0f0f0f0f0f0fL;
        i = i + (i >>> 8);
        i = i + (i >>> 16);
        i = i + (i >>> 32);
        return (int) i & 0x7f;
    }
}
