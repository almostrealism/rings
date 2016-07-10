package org.jflash;

/*  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License, version 2, as
 *  published by the Free Software Foundation.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License (the accompanying file named LGPL.txt)
 *  for more details.
 */

final class ColorTransform
{
static final int needA = 1;
static final int needB = 2;
int              flags,ra,rb,ga,gb,ba,bb;
byte             redMap[],greenMap[],blueMap[];
private FlashPane flash;

   ColorTransform(FlashPane flash)
      {
      this.flash=flash;
      }
   
   
    private static void BuildMapChannel(int i, int j, byte abyte0[])
    {
        int k = j << 8;
        int l = 256;
        int i1 = 0;
        while(l-- != 0) 
        {
            if((k & 0xffff0000) == 0)
                abyte0[i1++] = (byte)(k >> 8);
            else
            if(k > 0)
                abyte0[i1++] = -1;
            else
                abyte0[i1++] = 0;
            k += i;
        }
    }

    void BuildMapChannels()
    {
        redMap = new byte[256];
        greenMap = new byte[256];
        blueMap = new byte[256];
        if(redMap == null || greenMap == null || blueMap == null)
        {
            redMap = greenMap = blueMap = null;
            return;
        } else
        {
            BuildMapChannel(ra, rb, redMap);
            BuildMapChannel(ga, gb, greenMap);
            BuildMapChannel(ba, bb, blueMap);
            return;
        }
    }

    void Clear()
    {
        flags = 0;
        ra = ga = ba = 256;
        rb = gb = bb = 0;
    }

/*    private static int applyChannel(int i, int j, int k)
    {
        i = (i * j >> 8) + k;
        if((i & 0xff00) == 0)
            return i;
        return i <= 0 ? 0 : 255;
    }

    private static int applyChannelB(int i, int j, int k)
    {
        i += k;
        if((i & 0xff00) == 0)
            return i;
        return i <= 0 ? 0 : 255;
    }*/

    void ApplyColorMap(int ai[], int i)
    {
        if(flags == 0 || redMap == null)
            return;
        for(int j = 0; j < i; j++)
        {
            int k = ai[j];
            int l = redMap[k >> 16 & 0xff] & 0xff;
            int i1 = greenMap[k >> 8 & 0xff] & 0xff;
            int j1 = blueMap[k & 0xff] & 0xff;
            ai[j] = 0xff000000 | l << 16 | i1 << 8 | j1;
        }

    }

    int ApplyColorMap(int i)
    {
        if(flags == 0 || redMap == null)
        {
            return i;
        } else
        {
            int j = redMap[i >> 16 & 0xff] & 0xff;
            int k = greenMap[i >> 8 & 0xff] & 0xff;
            int l = blueMap[i & 0xff] & 0xff;
            return 0xff000000 | j << 16 | k << 8 | l;
        }
    }

    void Apply(RColor rcolor)
    {
        if(flags == 0)
        {
            return;
        } else
        {
            int i = rcolor.color;
            int i1 = i >> 16 & 0xff;
            int j1 = ra;
            int k1 = rb;
            i1 = (i1 * j1 >> 8) + k1;
            int j = (i1 & 0xff00) != 0 ? ((int) (i1 <= 0 ? 0 : 255)) : i1;
            i1 = i >> 8 & 0xff;
            j1 = ga;
            k1 = gb;
            i1 = (i1 * j1 >> 8) + k1;
            int k = (i1 & 0xff00) != 0 ? ((int) (i1 <= 0 ? 0 : 255)) : i1;
            i1 = i & 0xff;
            j1 = ba;
            k1 = bb;
            i1 = (i1 * j1 >> 8) + k1;
            int l = (i1 & 0xff00) != 0 ? ((int) (i1 <= 0 ? 0 : 255)) : i1;
            i = 0xff000000 | j << 16 | k << 8 | l;
            rcolor.RecalcSolid(flash.display, i);
            return;
        }
    }

    void ApplyGradient(RColor rcolor)
    {
        if(flags == 0 || rcolor.gcolorRamp == null)
            return;
        int ai[] = rcolor.gcolorRamp;
        for(int l = ai.length - 1; l >= 0; l--)
        {
            int i1 = ai[l] >> 16 & 0xff;
            int j1 = ra;
            int k1 = rb;
            i1 = (i1 * j1 >> 8) + k1;
            int i = (i1 & 0xff00) != 0 ? ((int) (i1 <= 0 ? 0 : 255)) : i1;
            i1 = ai[l] >> 8 & 0xff;
            j1 = ga;
            k1 = gb;
            i1 = (i1 * j1 >> 8) + k1;
            int j = (i1 & 0xff00) != 0 ? ((int) (i1 <= 0 ? 0 : 255)) : i1;
            i1 = ai[l] & 0xff;
            j1 = ba;
            k1 = bb;
            i1 = (i1 * j1 >> 8) + k1;
            int k = (i1 & 0xff00) != 0 ? ((int) (i1 <= 0 ? 0 : 255)) : i1;
            ai[l] = 0xff000000 | i << 16 | j << 8 | k;
        }

    }

}
