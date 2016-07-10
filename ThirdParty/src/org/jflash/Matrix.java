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

class Matrix
{
int                      a=0,b=0,c=0,d=0,tx=0,ty=0;
static final int         one = 0x10000;
static final int         sqrt2 = 0x16a0a;
private static final int lengthTable[] = 
   {
   0x40000000, 0x4001fff8, 0x4007ff80, 0x4011fd79, 0x401ff804, 0x4031ec87, 0x4047d7ad, 0x4061b56a, 0x407f80fe, 0x40a134f9, 
   0x40c6cb42, 0x40f03d1b, 0x411d8325, 0x414e956c, 0x41836b64, 0x41bbfbfc, 0x41f83d9b, 0x4238262d, 0x427bab2b, 0x42c2c19f, 
   0x430d5e30, 0x435b7529, 0x43acfa7f, 0x4401e1db, 0x445a1ea3, 0x44b5a3fe, 0x451464df, 0x4576540c, 0x45db6424, 0x464387a8, 
   0x46aeb0fe, 0x471cd27d, 0x478dde6e, 0x4801c717, 0x48787ebb, 0x48f1f7a3, 0x496e2425, 0x49ecf6a2, 0x4a6e6191, 0x4af25781, 
   0x4b78cb1a, 0x4c01af24, 0x4c8cf689, 0x4d1a9459, 0x4daa7bca, 0x4e3ca03c, 0x4ed0f53c, 0x4f676e85, 0x50000000, 0x509a9dc9, 
   0x51373c2e, 0x51d5cfaf, 0x52764d01, 0x5318a90f, 0x53bcd8f8, 0x5462d210, 0x550a89e3, 0x55b3f633, 0x565f0cf6, 0x570bc45b, 
   0x57ba12c3, 0x5869eec9, 0x591b4f3a, 0x59ce2b18, 0x5a82799a, 0x5a82799a
   };

    Matrix()
    {
        a = 0x10000;
        d = 0x10000;
    }

    Matrix(Matrix matrix)
    {
        a = 0x10000;
        d = 0x10000;
        a = matrix.a;
        b = matrix.b;
        c = matrix.c;
        d = matrix.d;
        tx = matrix.tx;
        ty = matrix.ty;
    }

    final void transform(Point point, Point point1)
    {
        int k = a;
        int i1 = point.x;
        int i = (int)((long)k * (long)i1 + 32768L >> 16) + tx;
        if(c != 0)
        {
            k = c;
            i1 = point.y;
            i += (int)((long)k * (long)i1 + 32768L >> 16);
        }
        k = d;
        i1 = point.y;
        int j = (int)((long)k * (long)i1 + 32768L >> 16) + ty;
        if(b != 0)
        {
            int l = b;
            int j1 = point.x;
            j += (int)((long)l * (long)j1 + 32768L >> 16);
        }
        point1.x = i;
        point1.y = j;
    }

    final void transform(Point point)
    {
        int i = a;
        int j = point.x;
        i = (int)((long)i * (long)j + 32768L >> 16) + tx;
        if(c != 0)
        {
            j = c;
            int k = point.y;
            i += (int)((long)j * (long)k + 32768L >> 16);
        }
        j = d;
        int l = point.y;
        j = (int)((long)j * (long)l + 32768L >> 16) + ty;
        if(b != 0)
        {
            int i1 = b;
            int j1 = point.x;
            j += (int)((long)i1 * (long)j1 + 32768L >> 16);
        }
        point.x = i;
        point.y = j;
    }

    final void scale(int i, int j)
    {
        a = i;
        d = j;
        b = c = 0;
        tx = ty = 0;
    }

    final void deltaTransform(Point point)
    {
        int i = a;
        int j = point.x;
        i = (int)((long)i * (long)j + 32768L >> 16);
        if(c != 0)
        {
            j = c;
            int k = point.y;
            i += (int)((long)j * (long)k + 32768L >> 16);
        }
        j = d;
        int l = point.y;
        j = (int)((long)j * (long)l + 32768L >> 16);
        if(b != 0)
        {
            int i1 = b;
            int j1 = point.x;
            j += (int)((long)i1 * (long)j1 + 32768L >> 16);
        }
        point.x = i;
        point.y = j;
    }

   final Rect transform(Rect rect)
       {
       Rect rect1 = new Rect();
       if (rect.xmin != 0x80000000)
          {
          Point point = new Point(rect.xmin, rect.ymin);
          Point point1 = new Point(0, 0);
          transform(point, point1);
          rect1.union(point1);
          point.x = rect.xmax;
          transform(point, point1);
          rect1.union(point1);
          point.y = rect.ymax;
          transform(point, point1);
          rect1.union(point1);
          point.x = rect.xmin;
          transform(point, point1);
          rect1.union(point1);
          }
       return rect1;
       }

   
   
    int transform(int i)
    {
        Point point = new Point(i, i);
        deltaTransform(point);
        int j = length(point.x, point.y);
        j = (int)(46341L *j + 32768L >> 16);
        if(i > 0)
            j = Math.max(1, j);
        return j;
    }

   final Matrix invert()
      {
      try
         {
         Matrix matrix = new Matrix();
         if (b == 0 && c == 0)
            {
            int i;
            matrix.a = (int)(0x100000000L /(i = a));
            matrix.d = (int)(0x100000000L /(i = d));
            i = matrix.a;
            matrix.tx = -(int)((long)i *tx + 32768L >> 16);
            i = matrix.d;
            matrix.ty = -(int)((long)i *ty + 32768L >> 16);
            } 
         else
            {
            double d1 =a * 1.52587890625E-005D;
            double d2 =b * 1.52587890625E-005D;
            double d3 =c * 1.52587890625E-005D;
            double d4 =d * 1.52587890625E-005D;
            double d5 = d1 * d4 - d2 * d3;
            if(d5 != 0.0D)
               {
               d5 = 1.0D / d5;
               matrix.a = (int)(d4 * d5 * 65536D);
               matrix.b = -(int)(d2 * d5 * 65536D);
               matrix.c = -(int)(d3 * d5 * 65536D);
               matrix.d = (int)(d1 * d5 * 65536D);
               Point point = new Point(tx, ty);
               matrix.deltaTransform(point);
               matrix.tx = -point.x;
               matrix.ty = -point.y;
               }
            }
         return matrix;
         }
      catch (Exception e)
         {
         System.err.println(e+" at Matrix.invert()");
         }
      return null;
      }

    static final Matrix concat(Matrix matrix, Matrix matrix1)
    {
        Matrix matrix2 = new Matrix();
        int i = matrix.a;
        matrix2.a = (int)((long)i *matrix1.a+ 32768L >> 16);
        i = matrix.d;
        matrix2.d = (int)((long)i *matrix1.d+ 32768L >> 16);
        i = matrix.tx;
        matrix2.tx = (int)(i *matrix1.a+ 32768L >> 16) + matrix1.tx;
        i = matrix.ty;
        matrix2.ty = (int)(i *matrix1.d+ 32768L >> 16) + matrix1.ty;
        if(matrix.b != 0 || matrix.c != 0 || matrix1.b != 0 || matrix1.c != 0)
        {
            int j = matrix.b;
            int l = matrix1.c;
            matrix2.a += (int)((long)j * (long)l + 32768L >> 16);
            j = matrix.c;
            l = matrix1.b;
            matrix2.d += (int)((long)j * (long)l + 32768L >> 16);
            j = matrix.a;
            l = matrix1.b;
            matrix2.b += (int)((long)j * (long)l + 32768L >> 16) + (int)((long)(j = matrix.b) * (long)(l = matrix1.d) + 32768L >> 16);
            j = matrix.c;
            l = matrix1.a;
            matrix2.c += (int)((long)j * (long)l + 32768L >> 16) + (int)((long)(j = matrix.d) * (long)(l = matrix1.c) + 32768L >> 16);
            j = matrix.ty;
            l = matrix1.c;
            matrix2.tx += (int)((long)j * (long)l + 32768L >> 16);
            j = matrix.tx;
            l = matrix1.b;
            matrix2.ty += (int)((long)j * (long)l + 32768L >> 16);
        }
        return matrix2;
    }

    static final int mul(int i, int j)
    {
        return (int)((long)i * (long)j + 32768L >> 16);
    }

    static final int div(int i, int j)
    {
        return (int)(((long)i << 16) /j);
    }

    static final int abs(int i)
    {
        if(i > 0)
            return i;
        else
            return -i;
    }

    static final int fastLength(int i, int j)
    {
        int k = i <= 0 ? -i : i;
        int l = j <= 0 ? -j : j;
        return (k + l) - (Math.min(k, l) >> 1);
    }

    static final int length(int i, int j)
    {
        int k = i <= 0 ? -i : i;
        int l = j <= 0 ? -j : j;
        if(k > l)
        {
            int i1 = k;
            k = l;
            l = i1;
        }
        if(l == 0)
        {
            return 0;
        } else
        {
            int j1 = (int)(((long)k << 16) /l);
            int k1 = j1 >> 10;
            int l1 = (j1 & 0x3ff) << 6;
            int i2 = 0x10000 - l1;
            int j2 = lengthTable[k1];
            i2 = (int)((long)i2 * (long)j2 + 32768L >> 16) + (int)((long)l1 * (long)(i2 = lengthTable[k1 + 1]) + 32768L >> 16);
            i2 >>= 14;
            return (int)((long)l * (long)i2 + 32768L >> 16);
        }
    }


}
