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

class Curve
{
   int     anchor1x,anchor1y,controlx,controly,anchor2x,anchor2y;
   boolean isLine;

   Curve()
      {
      isLine = false;
      }

   Curve(Curve curve)
      {
      isLine = false;
      anchor1x = curve.anchor1x;
      anchor1y = curve.anchor1y;
      anchor2x = curve.anchor2x;
      anchor2y = curve.anchor2y;
      controlx = curve.controlx;
      controly = curve.controly;
      isLine = curve.isLine;
      }

    final void set(Point point, Point point1)
    {
        anchor1x = point.x;
        anchor1y = point.y;
        anchor2x = point1.x;
        anchor2y = point1.y;
        controlx = (anchor1x + anchor2x) / 2;
        controly = (anchor1y + anchor2y) / 2;
        isLine = true;
    }

    final void set(Point point, Point point1, Point point2)
    {
        anchor1x = point.x;
        anchor1y = point.y;
        anchor2x = point2.x;
        anchor2y = point2.y;
        controlx = point1.x;
        controly = point1.y;
        isLine = false;
    }

    final Curve divide(int i)
    {
        Curve curve = new Curve();
        curve.anchor2x = anchor2x;
        curve.anchor2y = anchor2y;
        curve.controlx = (int)((long)i * (long)(anchor2x - controlx) + 32768L >> 16) + controlx;
        curve.controly = (int)((long)i * (long)(anchor2y - controly) + 32768L >> 16) + controly;
        controlx = (int)((long)i * (long)(controlx - anchor1x) + 32768L >> 16) + anchor1x;
        controly = (int)((long)i * (long)(controly - anchor1y) + 32768L >> 16) + anchor1y;
        anchor2x = curve.anchor1x = (int)(i * (long)(curve.controlx - controlx) + 32768L >> 16) + controlx;
        anchor2y = curve.anchor1y = (int)(i * (long)(curve.controly - controly) + 32768L >> 16) + controly;
        curve.isLine = isLine;
        return curve;
    }

    final int flatness()
    {
        return Matrix.fastLength(controlx - (anchor1x + anchor2x) / 2, controly - (anchor1y + anchor2y) / 2);
    }

    final int XRaySect(Point point, int i)
    {
        Rect rect = new Rect(anchor1x, anchor1y, anchor2x, anchor2y);
        if(controlx < rect.xmin)
            rect.xmin = controlx;
        else
        if(controlx > rect.xmax)
            rect.xmax = controlx;
        if(controly < rect.ymin)
            rect.ymin = controly;
        else
        if(controly > rect.ymax)
            rect.ymax = controly;
        if(rect.xmax < point.x || rect.ymin > point.y || rect.ymax <= point.y)
            return 0;
        if(rect.pointIn(point) && i < 12 && Matrix.fastLength(rect.xmax - rect.xmin, rect.ymax - rect.ymin) > 4)
        {
            Curve curve = new Curve(this);
            Curve curve1 = curve.divide(32768);
            i++;
            return curve.XRaySect(point, i) + curve1.XRaySect(point, i);
        }
        int j = anchor1y;
        int k = anchor2y;
        if(j == k)
            return 0;
        if(j > k)
        {
            int l = j;
            j = k;
            k = l;
        }
        return point.y < j || point.y >= k ? 0 : 1;
    }

}
