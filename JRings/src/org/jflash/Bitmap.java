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

import java.awt.*;
import java.awt.image.*;
import java.util.Hashtable;

final class Bitmap implements ImageConsumer
{
   int width;
   int height;
   int bpp;
   byte pixels8[];
   int pixels32[];
   ColorModel colorModel;
   DisplayList display;
   private boolean loaded;
   private boolean grabbing;
   private int flags;
   private ImageProducer producer;
   private static int PixCoverage[][][] = new int[8][8][4];
/*   private static final int RED = 0;
   private static final int GREEN = 1;
   private static final int BLUE = 2;
   private static final int RGBSlabChunkSize = 256;*/
   private static int pixBuf[] = new int[256];
   private int n;
   private int ce[];

    Bitmap(ImageProducer imageproducer, DisplayList displaylist)
    {
        ce = new int[4];
        producer = imageproducer;
        display = displaylist;
        imageproducer.startProduction(this);
        grabBits();
    }

    Bitmap(Image image, DisplayList displaylist)
    {
        this(image.getSource(), displaylist);
        image.flush();
    }

/*    Bitmap(int ai[], int i, int j, DisplayList displaylist)
    {
        ce = new int[4];
        pixels32 = ai;
        width = i;
        height = j;
        display = displaylist;
        bpp = 32;
    }

    Bitmap(byte abyte0[], int i, int j, DisplayList displaylist)
    {
        ce = new int[4];
        pixels8 = abyte0;
        width = i;
        height = j;
        display = displaylist;
        bpp = 8;
    }*/

    synchronized void grabBits()
    {
        grabbing = true;
        try
        {
            while(grabbing && producer != null) 
                wait();
        }
        catch(InterruptedException _ex)
        {
            pixels8 = null;
            pixels32 = null;
            width = height = 0;
            loaded = false;
        }
        finally
        {
            grabbing = false;
            producer = null;
            if(loaded && pixels32 != null)
            {
                for(int i = width * height; i > 0;)
                {
                    i--;
                    pixels32[i] |= 0xff000000;
                }

            }
        }
    }

    synchronized int status()
    {
        return flags;
    }

    public synchronized void imageComplete(int i)
    {
        loaded = i == 3;
        grabbing = false;
        switch(i)
        {
        case 1: // '\001'
        default:
            flags |= 0xc0;
            break;

        case 4: // '\004'
            flags |= 0x80;
            break;

        case 3: // '\003'
            flags |= 0x20;
            break;

        case 2: // '\002'
            flags |= 0x10;
            break;
        }
        if(producer != null)
            producer.removeConsumer(this);
        producer = null;
        notify();
    }

    public void setColorModel(ColorModel colormodel)
    {
        colorModel = colormodel;
    }

    public void setDimensions(int i, int j)
    {
        width = i;
        height = j;
    }

    public void setHints(int i)
    {
    }

    public void setPixels(int i, int j, int k, int l, ColorModel colormodel, byte abyte0[], int i1, 
            int j1)
    {
        if(pixels8 == null)
        {
            pixels8 = new byte[width * height];
            bpp = 8;
        }
        System.arraycopy(abyte0, i1, pixels8, width * j + i, j1);
    }

    public void setPixels(int i, int j, int k, int l, ColorModel colormodel, int ai[], int i1, 
            int j1)
    {
        if(pixels32 == null)
        {
            pixels32 = new int[width * height];
            bpp = 32;
        }
        System.arraycopy(ai, i1, pixels32, width * j + i, j1);
    }

    public void setProperties(Hashtable hashtable)
    {
    }

/*    private static void buildCoverageTable()
    {
        for(int i = 0; i < 8; i++)
        {
            for(int j = 0; j < 8; j++)
            {
                PixCoverage[i][j][0] = (8 - i) * (8 - j);
                PixCoverage[i][j][1] = i * (8 - j);
                PixCoverage[i][j][2] = (8 - i) * j;
                PixCoverage[i][j][3] = i * j;
                int k = 0;
                int l = 0;
                for(int i1 = 0; i1 <= 3; i1++)
                {
                    PixCoverage[i][j][i1] = (PixCoverage[i][j][i1] + 4) / 8;
                    l += PixCoverage[i][j][i1];
                    if(PixCoverage[i][j][i1] > PixCoverage[i][j][k])
                        k = i1;
                }

                int j1 = 8 - l;
                PixCoverage[i][j][k] += j1;
            }

        }

    }*/

    static int LimitAbs(int i, int j)
    {
        int k = i / j;
        if(i < 0)
            k--;
        return i - k * j;
    }

    static int LimitAbsI(int i, int j)
    {
        int k = i / j;
        if(i < 0)
            k--;
        return i - k * j;
    }

    private int CalcLimit(int i, int j, int k)
    {
        if(j > 0)
        {
            for(; i > k; i -= k);
            int l = (((k - i) + j) - 1) / j;
            if(n > l)
                n = l;
        } else
        if(j < 0)
        {
            for(; i < 0; i += k);
            int i1 = (i - j - 1) / -j;
            if(n > i1)
                n = i1;
        }
        return i;
    }

/*    private void unpackPix32(int i, int ai[])
    {
        ai[0] = i >>> 16 & 0xff;
        ai[1] = i >>> 8 & 0xff;
        ai[2] = i & 0xff;
    }

    private static int pix32ToWide(int i)
    {
        return (i & 0xff0000) << 5 | (i & 0xff00) << 2 | (i & 0xff) >>> 1;
    }

    private void wideToRGBI(int i, int ai[])
    {
        ai[0] = i >>> 24;
        ai[1] = i >>> 13 & 0xff;
        ai[2] = i >>> 2 & 0xff;
    }*/

    int GetRGBPixel(int i, int j)
    {
        if(i < 0)
            i = 0;
        if(j < 0)
            j = 0;
        if(j >= height)
            j = height - 1;
        if(i >= width)
            i = width - 1;
        int k = j * width;
        if(bpp == 8)
            return display.IndexToRGB(pixels8[k + i]);
        else
            return pixels32[k + i];
    }

    int GetSSRGBPixel(int i, int j)
    {
        int k = i >> 16;
        int l = j >> 16;
        int i1 = (i & 0xffff) >> 13;
        int j1 = (j & 0xffff) >> 13;
        ce[0] = PixCoverage[i1][j1][0];
        ce[1] = PixCoverage[i1][j1][1];
        ce[2] = PixCoverage[i1][j1][2];
        ce[3] = PixCoverage[i1][j1][3];
        if(k < 0)
        {
            k = 0;
            ce[0] += ce[1];
            ce[1] = 0;
            ce[2] += ce[3];
            ce[3] = 0;
        } else
        if(k >= width - 1)
        {
            k = width - 2;
            ce[1] += ce[0];
            ce[0] = 0;
            ce[3] += ce[2];
            ce[2] = 0;
        }
        if(l < 0)
        {
            l = 0;
            ce[0] += ce[2];
            ce[2] = 0;
            ce[1] += ce[3];
            ce[3] = 0;
        } else
        if(l >= height - 1)
        {
            l = height - 2;
            ce[2] += ce[0];
            ce[0] = 0;
            ce[3] += ce[1];
            ce[1] = 0;
        }
        long l1 = 0L;
        int k1 = l * width;
        if(bpp != 8)
        {
            int i2 = k1 + k;
            int k2 = pixels32[i2];
            l1 = (long)((k2 & 0xff0000) << 5 | (k2 & 0xff00) << 2 | (k2 & 0xff) >>> 1) * (long)ce[0];
            k2 = pixels32[i2 + 1];
            l1 += (long)((k2 & 0xff0000) << 5 | (k2 & 0xff00) << 2 | (k2 & 0xff) >>> 1) * (long)ce[1];
            i2 += width;
            k2 = pixels32[i2];
            l1 += (long)((k2 & 0xff0000) << 5 | (k2 & 0xff00) << 2 | (k2 & 0xff) >>> 1) * (long)ce[2];
            k2 = pixels32[i2 + 1];
            l1 += (long)((k2 & 0xff0000) << 5 | (k2 & 0xff00) << 2 | (k2 & 0xff) >>> 1) * (long)ce[3];
        }
        int j2 = (int)l1;
        return j2 >>> 8 & 0xff0000 | j2 >>> 5 & 0xff00 | j2 >>> 2 & 0xff;
    }

    void Blt32to8(RColor rcolor, Point point, int i, byte abyte0[], int j)
    {
        if(rcolor.bmDy == 0)
        {
            int k = (point.y >> 16) * width;
            if(Math.abs(rcolor.bmDx - 0x10000) < 256)
            {
                k += point.x >> 16;
                point.x += i * rcolor.bmDx;
                while(i-- > 0) 
                    abyte0[j++] = (byte)display.RGBToIndex(pixels32[k++]);
                return;
            }
            while(i-- > 0) 
            {
                abyte0[j++] = (byte)display.RGBToIndex(pixels32[k + (point.x >> 16)]);
                point.x += rcolor.bmDx;
            }
            return;
        }
        while(i-- > 0) 
        {
            abyte0[j++] = (byte)display.RGBToIndex(pixels32[(point.y >> 16) * width + (point.x >> 16)]);
            point.x += rcolor.bmDx;
            point.y += rcolor.bmDy;
        }
    }

    void Blt32to32(RColor rcolor, Point point, int i, int ai[], int j)
    {
        if(rcolor.bmDy == 0)
        {
            int k = (point.y >> 16) * width;
            if(Math.abs(rcolor.bmDx - 0x10000) < 256)
            {
                k += point.x >> 16;
                point.x += i * rcolor.bmDx;
                while(i-- > 0) 
                    ai[j++] = pixels32[k++];
                return;
            }
            while(i-- > 0) 
            {
                ai[j++] = pixels32[k + (point.x >> 16)];
                point.x += rcolor.bmDx;
            }
            return;
        }
        while(i-- > 0) 
        {
            ai[j++] = pixels32[(point.y >> 16) * width + (point.x >> 16)];
            point.x += rcolor.bmDx;
            point.y += rcolor.bmDy;
        }
    }

    private void Blt32toI(RColor rcolor, Point point, int i, int ai[])
    {
        int j = 0;
        if(rcolor.bmDy == 0)
        {
            int k = (point.y >> 16) * width;
            for(int i1 = i; i1 > 0; i1--)
            {
                ai[j] = pixels32[k + (point.x >> 16)];
                j++;
                point.x += rcolor.bmDx;
            }

            return;
        }
        for(int l = i; l > 0; l--)
        {
            ai[j] = pixels32[(point.y >> 16) * width + (point.x >> 16)];
            j++;
            point.x += rcolor.bmDx;
            point.y += rcolor.bmDy;
        }

    }

    void DrawSlab(int i, int j, int k, RColor rcolor)
    {
        Point point = new Point(0, 0);
        byte abyte0[] = display.pixels8;
        int ai[] = display.pixels32;
        point.x = j << 16;
        point.y = display.bitY << 16;
        rcolor.bmInvMat.transform(point, point);
        int l = width << 16;
        int i1 = height << 16;
        if(rcolor.fillType == 65)
        {
            int j4 = width;
            int k4 = height;
            if(rcolor.bmSmooth)
            {
                k4--;
                j4--;
            }
            Point point1 = new Point(0, 0);
            int l4 = k - j;
            point1.x = point.x + rcolor.bmDx * l4;
            point1.y = point.y + rcolor.bmDy * l4;
            do
            {
                int j1 = point.x >> 16;
                int j2 = point.y >> 16;
                if(j1 >= 0 && j2 >= 0 && j1 < j4 && j2 < k4 || j >= k)
                    break;
                int j3;
                if(rcolor.bmSmooth)
                    j3 = GetSSRGBPixel(point.x, point.y);
                else
                    j3 = GetRGBPixel(j1, j2);
                if(rcolor.cx != null)
                    j3 = rcolor.cx.ApplyColorMap(j3);
                if(abyte0 != null)
                    abyte0[j + i] = (byte)display.RGBToIndex(j3);
                else
                    ai[j + i] = j3;
                point.x += rcolor.bmDx;
                point.y += rcolor.bmDy;
                j++;
            } while(true);
            do
            {
                int k1 = point1.x >> 16;
                int k2 = point1.y >> 16;
                if(k1 >= 0 && k2 >= 0 && k1 < j4 && k2 < k4 || j >= k)
                    break;
                int k3;
                if(rcolor.bmSmooth)
                    k3 = GetSSRGBPixel(point1.x, point1.y);
                else
                    k3 = GetRGBPixel(k1, k2);
                k--;
                if(rcolor.cx != null)
                    k3 = rcolor.cx.ApplyColorMap(k3);
                if(abyte0 != null)
                    abyte0[k + i] = (byte)k3;
                else
                    ai[k + i] = k3;
                point1.x -= rcolor.bmDx;
                point1.y -= rcolor.bmDy;
            } while(true);
        } else
        {
            point.x = LimitAbs(point.x, l);
            point.y = LimitAbs(point.y, i1);
        }
        if(rcolor.bmFast)
        {
            for(; j < k; j += n)
            {
                n = Math.min(k - j, 256);
                point.x = CalcLimit(point.x, rcolor.bmDx, l);
                point.y = CalcLimit(point.y, rcolor.bmDy, i1);
                if(abyte0 != null)
                    Blt32to8(rcolor, point, n, abyte0, j + i);
                else
                    Blt32to32(rcolor, point, n, ai, j + i);
            }

            return;
        }
        for(; j < k; j += n)
        {
            n = Math.min(k - j, 256);
            point.x = CalcLimit(point.x, rcolor.bmDx, l);
            point.y = CalcLimit(point.y, rcolor.bmDy, i1);
            if(abyte0 != null)
                Blt32toI(rcolor, point, n, pixBuf);
            else
                Blt32toI(rcolor, point, n, pixBuf);
            if(rcolor.cx != null)
                rcolor.cx.ApplyColorMap(pixBuf, n);
            if(abyte0 != null)
            {
                int l1 = j + i;
                int l2 = 0;
                for(int l3 = n; l3 > 0;)
                {
                    abyte0[l1] = (byte)display.RGBToIndex(pixBuf[l2]);
                    l3--;
                    l1++;
                    l2++;
                }

            } else
            {
                int i2 = j + i;
                int i3 = 0;
                for(int i4 = n; i4 > 0;)
                {
                    ai[i2] = pixBuf[i3];
                    i4--;
                    i2++;
                    i3++;
                }

            }
        }

    }

}
