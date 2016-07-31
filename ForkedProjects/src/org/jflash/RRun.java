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

import java.awt.Point;

class RRun
{
   RRun next;
   int xmin;
   int xmax;
   RColor colors[];
   int nColors;
   boolean isComplex;
   boolean isPure;

   
   RRun()
      {
      colors = new RColor[4];
      isPure = true;
      }
   
   
    final void AddColor(RColor rcolor)
    {
        colors[nColors] = rcolor;
        nColors++;
        if(rcolor != colors[0])
            isPure = false;
        if(rcolor.fillType != 0)
            isComplex = true;
    }

    final long CalcColor(int i, int j)
    {
        if(isComplex)
        {
            long l = 0L;
            RColor rcolor = null;
            long l2 = 0L;
            for(int i1 = 0; i1 < nColors; i1++)
            {
                RColor rcolor1 = colors[i1];
                if(rcolor1.fillType != 0)
                {
                    if(rcolor != rcolor1)
                        switch(rcolor1.fillType)
                        {
                        default:
                            break;

                        case 64: // '@'
                        case 65: // 'A'
                        case 66: // 'B'
                            Bitmap bitmap = rcolor1.bitmap;
                            Point point2 = new Point(i << 16, j << 16);
                            rcolor1.bmInvMat.transform(point2);
                            if(rcolor1.fillType == 65)
                            {
                                if(rcolor1.bmSmooth)
                                {
                                    int i2 = bitmap.GetSSRGBPixel(point2.x - 32768, point2.y - 32768);
                                    l2 = (i2 & 0xff000000L) << 24 | (i2 & 0xff0000L) << 16 | (i2 & 65280L) << 8 | i2 & 255L;
                                } else
                                {
                                    int j2 = bitmap.GetRGBPixel(point2.x >> 16, point2.y >> 16);
                                    l2 = (j2 & 0xff000000L) << 24 | (j2 & 0xff0000L) << 16 | (j2 & 65280L) << 8 | j2 & 255L;
                                }
                            } else
                            {
                                int k2 = bitmap.GetRGBPixel(Bitmap.LimitAbsI(point2.x >> 16, bitmap.width), Bitmap.LimitAbsI(point2.y >> 16, bitmap.height));
                                l2 = (k2 & 0xff000000L) << 24 | (k2 & 0xff0000L) << 16 | (k2 & 65280L) << 8 | k2 & 255L;
                            }
                            rcolor = rcolor1;
                            break;

                        case 16: // '\020'
                            Point point = new Point(i << 8, j << 8);
                            rcolor1.ginvMat.transform(point);
                            int j1 = (point.x >> 15) + 128;
                            if(j1 > 256)
                                j1 = 256;
                            else
                            if(j1 < 0)
                                j1 = 0;
                            int i3 = rcolor1.gcolorRamp[j1];
                            l2 = (i3 & 0xff000000L) << 24 | (i3 & 0xff0000L) << 16 | (i3 & 65280L) << 8 | i3 & 255L;
                            rcolor = rcolor1;
                            break;

                        case 18: // '\022'
                            Point point1 = new Point(i << 8, j << 8);
                            rcolor1.ginvMat.transform(point1);
                            int k1 = Matrix.length(point1.x, point1.y) >> 14;
                            if(k1 > 256)
                                k1 = 256;
                            int j3 = rcolor1.gcolorRamp[k1];
                            l2 = (j3 & 0xff000000L) << 24 | (j3 & 0xff0000L) << 16 | (j3 & 65280L) << 8 | j3 & 255L;
                            rcolor = rcolor1;
                            break;
                        }
                    l += l2;
                } else
                {
                    l += rcolor1.wideColor;
                }
            }

            return l;
        }
        if(isPure)
            if(nColors == 4)
                return 4L * colors[0].wideColor;
            else
                return nColors * colors[0].wideColor;
        long l1 = 0L;
        for(int k = 0; k < nColors; k++)
            l1 += colors[k].wideColor;

        return l1;
    }

    final RRun Split(DisplayList displaylist, int i)
    {
        RRun rrun = displaylist.runPool;
        if(rrun != null)
            displaylist.runPool = rrun.next;
        else
            rrun = new RRun();
        rrun.xmin = i;
        rrun.xmax = xmax;
        xmax = i;
        rrun.next = next;
        next = rrun;
        rrun.colors[0] = colors[0];
        rrun.colors[1] = colors[1];
        rrun.colors[2] = colors[2];
        rrun.colors[3] = colors[3];
        rrun.nColors = nColors;
        rrun.isComplex = isComplex;
        rrun.isPure = isPure;
        return rrun;
    }


}
