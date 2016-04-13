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

final class RColor
   {
   RColor nextActive;
   int order;
   int fillType;
   int visible;
   boolean onActiveList;
   byte index;
   long wideColor;
   int color;
   Matrix ginvMat;
   int gcolorRamp[];
   Matrix bmInvMat;
   Bitmap bitmap;
   ColorTransform cx;
   boolean cacheValid;
   boolean bmSmooth;
   boolean bmFast;
   int bmDx;
   int bmDy;

   
   
   RColor(DisplayList displaylist, int i)
      {
      fillType = 0;
      color = i;
      if(displaylist.indexedColor) index = (byte)displaylist.RGBToIndex(color);
      int j;
      wideColor = ((j = color) & 0xff000000L) << 24 | (j & 0xff0000L) << 16 | (j & 65280L) << 8 | j & 255L;
      }

   
   
   RColor(DisplayList displaylist, int i, int j, int ai[], int ai1[], Matrix matrix, Matrix matrix1)
      {
      fillType = i;
      Matrix matrix2 = new Matrix(matrix1);
      if (displaylist.antialias)
         {
         matrix2.a /= 4;
         matrix2.d /= 4;
         matrix2.b /= 4;
         matrix2.c /= 4;
         matrix2.tx /= 4;
         matrix2.ty /= 4;
         }
      Matrix matrix3 = new Matrix(matrix);
      matrix2.tx <<= 8;
      matrix2.ty <<= 8;
      matrix3.tx <<= 8;
      matrix3.ty <<= 8;
      ginvMat = Matrix.concat(matrix3, matrix2).invert();
      gcolorRamp = new int[257];
      int i1 = 0;
      int j1 = ai1[0];
      int l;
      int k = l = ai[0];
      int j2 = 1;
      for (int k2 = 0; k2 <= 256; k2++)
          {
          if(k2 > j1)
             {
             i1 = j1;
             k = l;
             if (j2 < j)
                {
                j1 = ai1[j2];
                l = ai[j2];
                j2++;
                } 
             else j1 = 256;                
            }
            int k1 = (j1 - k2) / 8;
            int l1 = (k2 - i1) / 8;
            int i2 = k1 + l1;
            if(i2 > 0)
            {
                int l2 = ((k >> 16 & 0xff) * k1 + (l >> 16 & 0xff) * l1) / i2;
                int i3 = ((k >> 8 & 0xff) * k1 + (l >> 8 & 0xff) * l1) / i2;
                int j3 = ((k & 0xff) * k1 + (l & 0xff) * l1) / i2;
                gcolorRamp[k2] = 0xff000000 | l2 << 16 | i3 << 8 | j3;
            } else
            {
                gcolorRamp[k2] = k;
            }
        }

    }

    void RecalcSolid(DisplayList displaylist, int i)
    {
        fillType = 0;
        color = i;
        if(displaylist.indexedColor)
            index = (byte)displaylist.RGBToIndex(color);
        int j;
        wideColor = ((j = color) & 0xff000000L) << 24 | (j & 0xff0000L) << 16 | (j & 65280L) << 8 | j & 255L;
    }

}
