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

final class Rect
{
   int              xmin,ymin,xmax,ymax;
   static final int rectEmptyFlag = 0x80000000;

   Rect()
      {
      xmin = ymin = xmax = ymax = 0x80000000;
      }

   Rect(Rect rect)
      {
      xmin = rect.xmin;
      ymin = rect.ymin;
      xmax = rect.xmax;
      ymax = rect.ymax;
      }

   Rect(int i, int j, int k, int l)
      {
      if(i < k)
        {
            xmin = i;
            xmax = k;
        } else
        {
            xmin = k;
            xmax = i;
        }
        if(j < l)
        {
            ymin = j;
            ymax = l;
            return;
        } else
        {
            ymin = l;
            ymax = j;
            return;
        }
    }

    final void setEmpty()
    {
        xmin = xmax = ymin = ymax = 0x80000000;
    }

    final boolean isEmpty()
    {
        return xmin == 0x80000000;
    }

    final void offset(int i, int j)
    {
        if(xmin != 0x80000000)
        {
            xmin += i;
            xmax += i;
            ymin += j;
            ymax += j;
        }
    }

    final void union(Rect rect)
    {
        if(rect.xmin != 0x80000000)
        {
            if(xmin == 0x80000000)
            {
                xmin = rect.xmin;
                xmax = rect.xmax;
                ymin = rect.ymin;
                ymax = rect.ymax;
                return;
            }
            xmin = Math.min(xmin, rect.xmin);
            xmax = Math.max(xmax, rect.xmax);
            ymin = Math.min(ymin, rect.ymin);
            ymax = Math.max(ymax, rect.ymax);
        }
    }

    final void union(Point point)
    {
        if(xmin == 0x80000000)
        {
            xmin = xmax = point.x;
            ymin = ymax = point.y;
            return;
        }
        if(point.x < xmin)
            xmin = point.x;
        else
        if(point.x > xmax)
            xmax = point.x;
        if(point.y < ymin)
        {
            ymin = point.y;
            return;
        }
        if(point.y > ymax)
            ymax = point.y;
    }

    final boolean testIntersect(Rect rect)
    {
        return xmin <= rect.xmax && rect.xmin <= xmax && ymin <= rect.ymax && rect.ymin <= ymax;
    }

    final boolean pointIn(Point point)
    {
        return xmin <= point.x && point.x <= xmax && ymin <= point.y && point.y <= ymax;
    }

}
