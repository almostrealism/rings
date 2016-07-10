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

final class REdge extends Curve
{
   static final int fillEdgeRule = 0;
   static final int fillEvenOddRule = 1;
   static final int fillWindingRule = 2;
   int fillRule;
   int dir;
   REdge nextObj;
   REdge nextActive;
   RColor color1;
   RColor color2;
   int dx;
   int dy;
   int d2x;
   int d2y;
   int x;
   int xl;
   int yl;
   int stepLimit;

   
   REdge()
      {
      dir = 1;
      }
   
   
    final void initStep(int i)
    {
        if(super.isLine)
        {
            dx = (super.anchor2x - super.anchor1x << 16) / (super.anchor2y - super.anchor1y);
            xl = super.anchor1x << 16;
            int j = i - super.anchor1y;
            if(j != 0)
                xl += dx * j;
            x = xl + 32768 >> 16;
            return;
        }
        int k = (super.anchor1x - 2 * super.controlx) + super.anchor2x;
        int l = 2 * (super.controlx - super.anchor1x);
        int i1 = (super.anchor1y - 2 * super.controly) + super.anchor2y;
        int j1 = 2 * (super.controly - super.anchor1y);
        stepLimit = 2 * (super.anchor2y - super.anchor1y);
        int k1 = 0x1000000 / stepLimit;
        int l1 = (int)((long)k1 * (long)k1 >> 24);
        dx = l * k1;
        d2x = 2 * k * l1;
        dy = j1 * k1;
        d2y = 2 * i1 * l1;
        xl = super.anchor1x << 16;
        yl = super.anchor1y << 16;
        x = super.anchor1x;
        if(i > super.anchor1y)
            Step(i);
    }

    final void Step(int i)
    {
        if(super.isLine)
        {
            xl += dx;
            x = xl + 32768 >> 16;
            return;
        }
        for(int j = i << 16; yl < j && stepLimit >= 0; stepLimit--)
        {
            yl += dy >> 8;
            dy += d2y;
            xl += dx >> 8;
            dx += d2x;
        }

        x = xl + 32768 >> 16;
    }


}
