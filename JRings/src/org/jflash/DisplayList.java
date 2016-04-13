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
import java.awt.image.*;

final class DisplayList implements ImageProducer
{
   private Matrix cameraMat;
   private int backgroundColor;
   private int backgroundColorPriority;
   boolean antialias;
   private Rect devDirtyRgn;
   private SObject objects;
   private FlashPane flash;
   SObject button;
   int buttonState;
   private int buttonId;
   static final int showAll = 0;
   static final int noBorder = 1;
   static final int exactFit = 2;
   static final int scaleMask = 15;
   static final int alignLeft = 16;
   static final int alignRight = 32;
   static final int alignTop = 64;
   static final int alignBottom = 128;
   boolean indexedColor;
   ColorModel model;
   private int width;
   private int height;
   private Rect bitClip;
   private Rect edgeClip;
   private int pixelSize;
   private int pixelOrg;
   byte pixels8[];
   int pixels32[];
   private int topColorXleft;
   private RColor topColor;
   private RColor activeColors;
   private REdge activeEdges[];
   private int nActive;
   private int maxActive;
   private REdge yindex[];
   private int ylines;
   private int y;
   int bitY;
   private RRun firstRun;
   private RRun curRun;
   RRun runPool;
   private long rgb;
   private int pixelX;
   private int pixelW;
   private int pbufWidth;
   private int pbufTop;
   private int pbufNLines;
   private int pbufMaxLines;
   private int nColors;
   private int ctab[];
   private byte itab[];
   private int nEmpty;
   private int error[];
   boolean forceCompleteScanlines;
   ImageConsumer theConsumer;
   boolean newConsumer;

   DisplayList(FlashPane flash)
       {
       cameraMat = new Matrix();
       backgroundColor = -1;
       antialias = false;
       devDirtyRgn = new Rect();
       forceCompleteScanlines = false;
       this.flash=flash;
       }

    void FreeAll()
    {
        for(SObject sobject = objects; sobject != null; sobject = sobject.next)
            devDirtyRgn.union(sobject.devBounds);

        objects = null;
        button = null;
        if(backgroundColorPriority <= 1)
        {
            backgroundColor = -1;
            backgroundColorPriority = 0;
        }
    }

   void setCamera(Rect rect,int quality, int scaleMode, boolean flag1)
      {
      Rect rect1 = new Rect(0, 0, width, height);
      if (quality==FlashPane.NICEST)
         {
         rect1.xmax *= 4;
         rect1.ymax *= 4;
         }
      Matrix matrix = new Matrix();
      int j = Math.max(rect1.xmax - rect1.xmin, 16);
      int k;
      matrix.a = (int)(((long)j << 16) /(k = Math.max(rect.xmax - rect.xmin, 16)));
      j = Math.max(rect1.ymax - rect1.ymin, 16);
      matrix.d = (int)(((long)j << 16) /(k = Math.max(rect.ymax - rect.ymin, 16)));
      switch(scaleMode & 0xf)
         {
         case 0: // '\0'
         default:
            matrix.a = matrix.d = Math.min(matrix.a, matrix.d);
            break;
         case 1: // '\001'
            matrix.a = matrix.d = Math.max(matrix.a, matrix.d);
            break;
         }
      int l;
      if ((scaleMode & 0x10) != 0)
         {
         j = rect.xmin;
         l = rect1.xmin;
         } 
      else
      if((scaleMode & 0x20) != 0)
         {
         j = rect.xmax;
         l = rect1.xmax;
         } 
      else
         {
         j = (rect.xmin + rect.xmax) / 2;
         l = (rect1.xmin + rect1.xmax) / 2;
         }
      int i1;
      if((scaleMode & 0x40) != 0)
         {
         k = rect.ymin;
         i1 = rect1.ymin;
         } 
      else
      if ((scaleMode & 0x80) != 0)
         {
         k = rect.ymax;
         i1 = rect1.ymax;
         } 
      else
         {
         k = (rect.ymin + rect.ymax) / 2;
         i1 = (rect1.ymin + rect1.ymax) / 2;
         }
      matrix.tx = l - (int)((long)j *matrix.a+ 32768L >> 16);
      matrix.ty = i1 - (int)((long)k *matrix.a+ 32768L >> 16);
      if(matrix.a != cameraMat.a || matrix.b != cameraMat.b || matrix.c != cameraMat.c || matrix.d != cameraMat.d || matrix.tx != cameraMat.tx || matrix.ty != cameraMat.ty || (antialias!=(quality==FlashPane.NICEST)))
         {
         if (quality==FlashPane.NICEST) antialias=true;
         cameraMat = matrix;
         modifyCamera();
         }
      if (flag1)
         {
         Rect rect2 = devDirtyRgn;
         if (rect2.xmin != 0x80000000 && true)
            {
            produce(true);
            return;
            }
         }
      }

   
   
    private void invalidate()
    {
        devDirtyRgn.xmin = devDirtyRgn.ymin = 0;
        devDirtyRgn.xmax = width;
        devDirtyRgn.ymax = height;
        if(antialias)
        {
            devDirtyRgn.xmax *= 4;
            devDirtyRgn.ymax *= 4;
        }
    }

    private void modifyCamera()
    {
        invalidate();
        for(SObject sobject = objects; sobject != null; sobject = sobject.next)
        {
            sobject.edges = null;
            sobject.devMat = Matrix.concat(sobject.mat, cameraMat);
            sobject.devBounds = sobject.devMat.transform(sobject.character.bounds);
        }

    }

    void setBackgroundColor(int i, int j)
    {
        if(j >= backgroundColorPriority)
        {
            if(backgroundColor == i && j == backgroundColorPriority)
                return;
            invalidate();
            backgroundColor = i;
            backgroundColorPriority = j;
        }
    }

    
    
   void placeObject(SCharacter scharacter, int ID,int depth,Matrix matrix, ColorTransform colortransform)
      {
      SObject sobject = new SObject();
      sobject.next = objects;
      objects = sobject;
      sobject.display = this;
      sobject.character = scharacter;
      sobject.ID=ID;
      sobject.depth=depth;
      if (matrix!=null) sobject.mat=matrix;
      else sobject.mat=new Matrix();
      sobject.cx = colortransform;
      if(scharacter.type==SCharacter.SCHAR_TYPE_BUTTON)
         {
         int j = 1;
         if (ID==buttonId)
            {
            button = sobject;
            j = buttonState;
            }
         updateButton(sobject, j);
         sobject.devBounds = new Rect();
         return;
         } 
      else
         {
         sobject.devMat = Matrix.concat(sobject.mat, cameraMat);
         sobject.devBounds = sobject.devMat.transform(scharacter.bounds);
         devDirtyRgn.union(sobject.devBounds);
         }
      }

/*    private SObject findObject(int i)
    {
        for(SObject sobject = objects; sobject != null; sobject = sobject.next)
            if(sobject.id == i)
                return sobject;

        return null;
    }*/

    void removeObject(int ID,int depth)
    {
        SObject sobject = null;
        for(SObject sobject1 = objects; sobject1 != null; sobject1 = sobject1.next)
        {
            if(sobject1.ID==ID)
            {
                if(sobject1.buttonState != 0)
                {
                    updateButton(sobject1, 0);
                    removeObject(ID,depth);
                    if(button == sobject1)
                    {
                        button = null;
                        return;
                    }
                } else
                {
                    if(sobject1.drawn)
                        devDirtyRgn.union(sobject1.devBounds);
                    if(sobject != null)
                    {
                        sobject.next = sobject1.next;
                        return;
                    }
                    objects = sobject1.next;
                }
                return;
            }
            sobject = sobject1;
        }

    }

    
    void removeObject2(int depth)
    {
        SObject sobject = null;
        for(SObject sobject1 = objects; sobject1 != null; sobject1 = sobject1.next)
        {
            if(sobject1.depth ==depth)
            {
                if(sobject1.buttonState != 0)
                {
                    updateButton(sobject1, 0);
                    removeObject(sobject1.ID,sobject1.depth);
                    if(button == sobject1)
                    {
                        button = null;
                        return;
                    }
                } else
                {
                    if(sobject1.drawn)
                        devDirtyRgn.union(sobject1.devBounds);
                    if(sobject != null)
                    {
                        sobject.next = sobject1.next;
                        return;
                    }
                    objects = sobject1.next;
                }
                return;
            }
            sobject = sobject1;
        }

    }
    
    
    private void updateButton(SObject sobject, int i)
    {
        int j = sobject.buttonState;
        if(j == i)
            return;
        ScriptPlayer scriptplayer = sobject.character.player;
        SParser sparser = new SParser();
        int k = sobject.character.cxformPos;
        ColorTransform colortransform = null;
        SParser sparser1 = null;
        if(k > 0)
            sparser1 = new SParser();
        for(int l = 0; l < 2; l++)
        {
            byte abyte0[] = ((SParser) (scriptplayer)).script;
            int j1 = sobject.character.dataPos;
            sparser.script = abyte0;
            sparser.pos = j1;
            if(sparser1 != null)
            {
                byte abyte1[] = ((SParser) (scriptplayer)).script;
                sparser1.script = abyte1;
                sparser1.pos = k;
            }
            do
            {
                int i1 = sparser.script[sparser.pos++] & 0xff;
                if(i1 == 0)
                    break;
                int k1 = sparser.GetWord();
//                int l1 = (sobject.ID & 0xffff) + sparser.GetWord();
                Matrix matrix = sparser.GetMatrix();
//                int i2 = k1 << 16 | l1;
                if(k > 0)
                {
                    colortransform = new ColorTransform(flash);
                    sparser1.GetColorTransform(colortransform);
                }
                boolean flag = (i1 & i) != 0;
                boolean flag1 = (i1 & j) != 0;
                if(l == 1 && flag && !flag1)
                {
                    SCharacter scharacter = scriptplayer.findCharacter(k1);
                    if(scharacter != null) placeObject(scharacter,sobject.ID,k1,Matrix.concat(matrix, sobject.mat), colortransform);
                } else
                if(l == 0 && !flag && flag1)
                    removeObject(sobject.ID,k1);
            } while(true);
            sobject.buttonState = i;
        }

    }

    boolean setButtonState(SObject sobject, int i)
    {
        boolean flag = false;
        if(sobject != button)
        {
            if(button != null)
            {
                button.character.player.PlayButtonSound(button, 1);
                updateButton(button, 1);
                flag = true;
            }
            button = sobject;
            if(button != null)
            {
                button.character.player.PlayButtonSound(button, i);
                updateButton(button, i);
                flag = true;
            }
        } else
        if(button != null && button.buttonState != i)
        {
            button.character.player.PlayButtonSound(button, i);
            updateButton(button, i);
            flag = true;
        }
        if(button != null)
        {
            buttonState = i;
            buttonId = button.ID;
        } else
        {
            buttonId = 0;
        }
        return flag;
    }

    SObject hitButton(int i, int j)
    {
        SObject sobject = null;
        int k = 0;
        for(SObject sobject1 = objects; sobject1 != null; sobject1 = sobject1.next)
            if ((sobject1.character.type == 2) && (sobject1.ID>k) && (hitOneButton(sobject1, i, j)))
            {
                sobject = sobject1;
                k = sobject1.ID;
            }

        return sobject;
    }

    boolean hitOneButton(SObject sobject, int i, int j)
    {
        Point point = new Point(i, j);
        if(antialias)
        {
            point.x *= 4;
            point.y *= 4;
        }
        ScriptPlayer scriptplayer = sobject.character.player;
        SParser sparser = new SParser();
        byte abyte0[] = ((SParser) (scriptplayer)).script;
        int l = sobject.character.dataPos;
        sparser.script = abyte0;
        sparser.pos = l;
        do
        {
            int k = sparser.script[sparser.pos++] & 0xff;
            if(k != 0)
            {
                int i1 = sparser.GetWord();
//                int j1 = sobject.id + sparser.GetWord();
                int depth=sparser.GetWord();
                Matrix matrix = sparser.GetMatrix();
//                int k1 = i1 << 16 | j1;
                if((k & 8) != 0)
                {
                    SCharacter scharacter = scriptplayer.findCharacter(i1);
                    if(scharacter != null)
                    {
                        SObject sobject1 = new SObject();
                        sobject1.display = this;
                        sobject1.character = scharacter;
                        sobject1.ID=i1;
                        sobject1.depth=depth;
                        sobject1.mat = Matrix.concat(matrix, sobject.mat);
                        sobject1.devMat = Matrix.concat(sobject1.mat, cameraMat);
                        sobject1.devBounds = sobject1.devMat.transform(scharacter.bounds);
                        if(sobject1.HitTest(point))
                            return true;
                    }
                }
            } else
            {
                return false;
            }
        } while(true);
    }

/*    private final void pixelInit()
    {
        rgb = 0L;
        pixelW = 0;
        pixelX = -32000;
    }

    private final void pixelPaint()
    {
        if(pixelW > 0)
        {
            int i = pixelX;
            long l = rgb;
            long l1;
            SetPixel(i, (int)((l1 = l / 16L) >> 24) & 0xff000000 | (int)(l1 >> 16) & 0xff0000 | (int)(l1 >> 8) & 0xff00 | (int)l1 & 0xff);
            rgb = 0L;
            pixelW = 0;
        }
    }*/

    private final void pixelAdd(long l, int i, int j, int k)
    {
        if(pixelX != j)
        {
            if(pixelW > 0)
            {
                int i1 = pixelX;
                long l1 = rgb;
                long l2;
                setPixel(i1, (int)((l2 = l1 / 16L) >> 24) & 0xff000000 | (int)(l2 >> 16) & 0xff0000 | (int)(l2 >> 8) & 0xff00 | (int)l2 & 0xff);
                rgb = 0L;
                pixelW = 0;
            }
            pixelX = j;
        }
        rgb += k * l;
        pixelW += k * i;
    }

    static final long ExpandColor(int i)
    {
        return (i & 0xff000000L) << 24 | (i & 0xff0000L) << 16 | (i & 65280L) << 8 | i & 255L;
    }

    static final int PackColor(long l)
    {
        return (int)(l >> 24) & 0xff000000 | (int)(l >> 16) & 0xff0000 | (int)(l >> 8) & 0xff00 | (int)l & 0xff;
    }

/*    private final void drawPixel(int i, long l, int j)
    {
        long l1;
        SetPixel(i, (int)((l1 = l / 16L) >> 24) & 0xff000000 | (int)(l1 >> 16) & 0xff0000 | (int)(l1 >> 8) & 0xff00 | (int)l1 & 0xff);
    }*/

    private final void setPixel(int i, int j)
    {
        if(indexedColor)
        {
            pixels8[i + pixelOrg] = (byte)RGBToIndex(j);
            return;
        } else
        {
            pixels32[i + pixelOrg] = j;
            return;
        }
    }

    private final void DrawSlab(int i, int j, RColor rcolor)
    {
        if(i < bitClip.xmin)
            i = bitClip.xmin;
        if(j > bitClip.xmax)
            j = bitClip.xmax;
        switch(rcolor.fillType)
        {
        case 64: // '@'
        case 65: // 'A'
        case 66: // 'B'
            rcolor.bitmap.DrawSlab(pixelOrg, i, j, rcolor);
            return;

        case 0: // '\0'
            int k = i + pixelOrg;
            int l = j - i;
            if(k + l > pixelSize)
                return;
            if(indexedColor)
            {
                while(l-- > 0) 
                    pixels8[k++] = rcolor.index;
                return;
            }
            while(l-- > 0) 
                pixels32[k++] = rcolor.color;
            return;

        case 16: // '\020'
            Point point = new Point(i << 8, bitY << 8);
            rcolor.ginvMat.transform(point);
            int i1 = rcolor.ginvMat.a >> 8;
            int k1 = rcolor.ginvMat.b >> 8;
            for(int i2 = i; i2 < j; i2++)
            {
                int k2 = (point.x >> 15) + 128;
                if(k2 > 256)
                    k2 = 256;
                else
                if(k2 < 0)
                    k2 = 0;
                setPixel(i2, rcolor.gcolorRamp[k2]);
                point.x += i1;
                point.y += k1;
            }

            return;

        case 18: // '\022'
            Point point1 = new Point(i << 8, bitY << 8);
            rcolor.ginvMat.transform(point1);
            int j1 = rcolor.ginvMat.a >> 8;
            int l1 = rcolor.ginvMat.b >> 8;
            int j2 = 0;
            int l2 = Matrix.length(point1.x, point1.y) >> 14;
            if(l2 > 256)
                l2 = 256;
            for(int i3 = i; i3 < j; i3++)
            {
                int j3 = point1.x >> 14;
                int k3 = j3 * j3;
                j3 = point1.y >> 14;
                k3 += j3 * j3;
                int l3 = l2;
                l2 += j2;
                if(l2 < 0)
                    l2 = 0;
                else
                if(l2 > 256)
                    l2 = 256;
                do
                {
                    for(; k3 < l2 * l2; l2--);
                    if(k3 <= (l2 + 1) * (l2 + 1) || l2 >= 256)
                        break;
                    l2++;
                } while(true);
                j2 = l2 - l3;
                setPixel(i3, rcolor.gcolorRamp[l2]);
                point1.x += j1;
                point1.y += l1;
            }

            return;
        }
    }

    private final void UpdateColor(int i)
    {
        RColor rcolor = null;
        RColor rcolor1 = null;
        for(RColor rcolor2 = activeColors; rcolor2 != null; rcolor2 = rcolor2.nextActive)
            if(rcolor2.visible != 0)
            {
                if(rcolor == null || rcolor2.order > rcolor.order)
                    rcolor = rcolor2;
                rcolor1 = rcolor2;
            } else
            {
                if(rcolor1 != null)
                    rcolor1.nextActive = rcolor2.nextActive;
                else
                    activeColors = rcolor2.nextActive;
                rcolor2.onActiveList = false;
            }

        if(topColor != rcolor)
        {
            if(topColor != null)
                if(antialias)
                    PaintAASlab(topColorXleft, i);
                else
                    DrawSlab(topColorXleft, i, topColor);
            topColor = rcolor;
            topColorXleft = i;
        }
    }

    private final void ShowColor(RColor rcolor, int i)
    {
        if(topColor != null)
        {
            if(rcolor.order > topColor.order)
            {
                if(antialias)
                    PaintAASlab(topColorXleft, i);
                else
                    DrawSlab(topColorXleft, i, topColor);
                topColor = rcolor;
                topColorXleft = i;
            }
        } else
        {
            topColor = rcolor;
            topColorXleft = i;
        }
        if(!rcolor.onActiveList)
        {
            rcolor.onActiveList = true;
            rcolor.nextActive = activeColors;
            activeColors = rcolor;
        }
    }

/*    private final void hideColor(RColor rcolor, int i)
    {
        if(rcolor == topColor)
            UpdateColor(i);
    }*/

    private final void PaintAASlab(int i, int j)
    {
        RRun rrun = curRun;
        if(rrun == null || rrun.xmin >= j)
            return;
        while(rrun.xmax < i) 
        {
            rrun = rrun.next;
            if(rrun == null)
            {
                curRun = null;
                return;
            }
        }
        if(rrun.xmin < i)
            rrun = rrun.Split(this, i);
        for(; rrun != null && rrun.xmin < j; rrun = rrun.next)
        {
            if(rrun.xmax > j)
            {
                curRun = rrun.Split(this, j);
                rrun.AddColor(topColor);
                return;
            }
            rrun.AddColor(topColor);
        }

        curRun = rrun;
    }

    private final void PaintAARuns()
    {
        bitY = y / 4;
        rgb = 0L;
        pixelW = 0;
        pixelX = -32000;
        RRun rrun = firstRun;
        do
        {
            if(rrun.nColors > 0)
            {
                if(rrun.isPure && rrun.nColors == 4)
                    do
                    {
                        RRun rrun1 = rrun.next;
                        if(rrun1 == null || !rrun1.isPure || rrun1.colors[0] != rrun.colors[0] || rrun1.nColors < 4)
                            break;
                        rrun.xmax = rrun1.xmax;
                        rrun.next = rrun1.next;
                    } while(true);
                int i = rrun.xmin / 4;
                int k = rrun.xmin & 3;
                int i1 = rrun.xmax / 4;
                int j1 = rrun.xmax & 3;
                long l2 = rrun.CalcColor(i, bitY);
                if(i == i1)
                {
                    pixelAdd(l2, rrun.nColors, i, j1 - k);
                } else
                {
                    if(k > 0)
                    {
                        pixelAdd(l2, rrun.nColors, i, 4 - k);
                        i++;
                    }
                    if(i < i1)
                        if(rrun.isPure && rrun.nColors == 4)
                            DrawSlab(i, i1, rrun.colors[0]);
                        else
                        if(rrun.isComplex)
                        {
                            for(int j2 = i; j2 < i1; j2++)
                            {
                                long l3 = rrun.CalcColor(j2, bitY) * 4L;
                                long l5;
                                setPixel(j2, (int)((l5 = l3 / 16L) >> 24) & 0xff000000 | (int)(l5 >> 16) & 0xff0000 | (int)(l5 >> 8) & 0xff00 | (int)l5 & 0xff);
                            }

                        } else
                        {
                            long l4 = 4L * l2;
                            for(int k2 = i; k2 < i1; k2++)
                            {
                                long l6;
                                setPixel(k2, (int)((l6 = l4 / 16L) >> 24) & 0xff000000 | (int)(l6 >> 16) & 0xff0000 | (int)(l6 >> 8) & 0xff00 | (int)l6 & 0xff);
                            }

                        }
                    if(j1 > 0)
                    {
                        if(rrun.isComplex)
                            l2 = rrun.CalcColor(i1, bitY);
                        pixelAdd(l2, rrun.nColors, i1, j1);
                    }
                }
            }
            if(rrun.next == null)
                break;
            rrun = rrun.next;
        } while(true);
        rrun.next = runPool;
        runPool = firstRun;
        firstRun = null;
        if(pixelW > 0)
        {
            int j = pixelX;
            long l = rgb;
            long l1;
            setPixel(j, (int)((l1 = l / 16L) >> 24) & 0xff000000 | (int)(l1 >> 16) & 0xff0000 | (int)(l1 >> 8) & 0xff00 | (int)l1 & 0xff);
            rgb = 0L;
            pixelW = 0;
        }
    }

    private final void AddEdges(REdge redge)
    {
        for(; redge != null; redge = redge.nextObj)
            if(((Curve) (redge)).anchor1y <= edgeClip.ymax && ((Curve) (redge)).anchor2y > edgeClip.ymin)
            {
                int i = ((Curve) (redge)).anchor1y - edgeClip.ymin;
                if(i < 0)
                    i = 0;
                redge.nextActive = yindex[i];
                yindex[i] = redge;
            }

    }

    private final void InitPixels()
    {
        pixelOrg = -bitClip.xmin;
        pbufWidth = bitClip.xmax - bitClip.xmin;
        pbufNLines = 0;
        pbufMaxLines = pixelSize / pbufWidth;
        pbufTop = bitClip.ymin;
    }

    private final void AdvanceLine()
    {
        pbufNLines++;
        if(pbufNLines >= pbufMaxLines)
            SendPixels();
        pixelOrg = pbufNLines * pbufWidth - bitClip.xmin;
    }

    private final void SendPixels()
    {
        if(pbufNLines > 0)
            if(indexedColor)
                theConsumer.setPixels(bitClip.xmin, pbufTop, pbufWidth, pbufNLines, model, pixels8, 0, pbufWidth);
            else
                theConsumer.setPixels(bitClip.xmin, pbufTop, pbufWidth, pbufNLines, model, pixels32, 0, pbufWidth);
        pbufTop += pbufNLines;
        pbufNLines = 0;
    }

    private final void PaintBits()
    {
        InitPixels();
        for(y = edgeClip.ymin; y < edgeClip.ymax; y++)
        {
            for(REdge redge = yindex[y - edgeClip.ymin]; redge != null; redge = redge.nextActive)
            {
                redge.initStep(y);
                if(nActive == maxActive)
                {
                    maxActive *= 2;
                    REdge aredge[] = new REdge[maxActive];
                    System.arraycopy(activeEdges, 0, aredge, 0, nActive);
                    activeEdges = aredge;
                }
                int i = nActive;
                do
                {
                    if(i == 0 || activeEdges[i - 1].x < redge.x)
                    {
                        activeEdges[i] = redge;
                        break;
                    }
                    activeEdges[i] = activeEdges[i - 1];
                    i--;
                } while(true);
                nActive++;
            }

            int j = nActive - 1;
            boolean flag;
            do
            {
                flag = false;
                for(int k = 0; k < j; k++)
                    if(activeEdges[k].x > activeEdges[k + 1].x)
                    {
                        REdge redge1 = activeEdges[k];
                        activeEdges[k] = activeEdges[k + 1];
                        activeEdges[k + 1] = redge1;
                        if(!flag && k > 0 && activeEdges[k - 1].x > activeEdges[k].x)
                            flag = true;
                    }

                j--;
            } while(flag && j > 0);
            if(antialias)
            {
                if(firstRun == null)
                {
                    if(runPool != null)
                    {
                        firstRun = runPool;
                        runPool = firstRun.next;
                        firstRun.next = null;
                        firstRun.nColors = 0;
                        firstRun.isComplex = false;
                        firstRun.isPure = true;
                    } else
                    {
                        firstRun = new RRun();
                    }
                    firstRun.xmin = edgeClip.xmin;
                    firstRun.xmax = edgeClip.xmax;
                }
                curRun = firstRun;
            } else
            {
                bitY = y;
            }
            int l = 0;
            int i1 = y + 1;
            for(int j1 = 0; j1 < nActive; j1++)
            {
                REdge redge2 = activeEdges[j1];
                switch(redge2.fillRule)
                {
                case 0: // '\0'
                    RColor rcolor = redge2.color1;
                    if(rcolor.visible != 0)
                    {
                        rcolor.visible = 0;
                        int k1 = redge2.x;
                        if(rcolor == topColor)
                            UpdateColor(k1);
                    } else
                    {
                        rcolor.visible = 1;
                        ShowColor(rcolor, redge2.x);
                    }
                    rcolor = redge2.color2;
                    if(rcolor.visible != 0)
                    {
                        rcolor.visible = 0;
                        int l1 = redge2.x;
                        if(rcolor == topColor)
                            UpdateColor(l1);
                    } else
                    {
                        rcolor.visible = 1;
                        ShowColor(rcolor, redge2.x);
                    }
                    break;

                case 1: // '\001'
                    RColor rcolor1 = redge2.color1;
                    if(rcolor1.visible != 0)
                    {
                        rcolor1.visible = 0;
                        int i2 = redge2.x;
                        if(rcolor1 == topColor)
                            UpdateColor(i2);
                    } else
                    {
                        rcolor1.visible = 1;
                        ShowColor(rcolor1, redge2.x);
                    }
                    break;

                case 2: // '\002'
                    RColor rcolor2 = redge2.color1;
                    if(rcolor2.visible == 0)
                    {
                        rcolor2.visible += redge2.dir;
                        ShowColor(rcolor2, redge2.x);
                    } else
                    {
                        rcolor2.visible += redge2.dir;
                        if(rcolor2.visible == 0)
                        {
                            int j2 = redge2.x;
                            if(rcolor2 == topColor)
                                UpdateColor(j2);
                        }
                    }
                    break;
                }
                if(((Curve) (redge2)).anchor2y > i1)
                {
                    redge2.Step(i1);
                    activeEdges[l] = redge2;
                    l++;
                }
            }

            nActive = l;
            if(antialias)
            {
                if((y & 3) == 3)
                {
                    PaintAARuns();
                    if(theConsumer == null)
                        return;
                    AdvanceLine();
                }
            } else
            {
                if(theConsumer == null)
                    return;
                AdvanceLine();
            }
        }

        if(theConsumer != null)
            SendPixels();
        runPool = null;
    }

    final int IndexToRGB(int i)
    {
        return ctab[i];
    }

    final int RGBToIndex(int i)
    {
        int j = (i & 0xf00000) >> 12 | (i & 0xf000) >> 8 | (i & 0xf0) >> 4;
        return itab[j] & 0xff;
    }

    private final int ColorDist(int i, int j)
    {
        int k = i - j;
        if(k < 0)
            return -k;
        else
            return k;
    }

    private final void FillCube(int i, int j, int k)
    {
        int l = i >> 16 & 0xff;
        int i1 = i >> 8 & 0xff;
        int j1 = i & 0xff;
        int k1 = l >> 4;
        int i2 = i1 >> 4;
        int k2 = j1 >> 4;
        int i3 = Math.max(0, k1 - k);
        int j3 = Math.min(15, k1 + k);
        int k3 = Math.max(0, i2 - k);
        int l3 = Math.min(15, i2 + k);
        int i4 = Math.max(0, k2 - k);
        int j4 = Math.min(15, k2 + k);
        for(int l1 = i3; l1 <= j3; l1++)
        {
            int k4 = l1 != 15 ? l1 << 4 : 255;
            for(int j2 = k3; j2 <= l3; j2++)
            {
                int l4 = j2 != 15 ? j2 << 4 : 255;
                int i5 = ColorDist(k4, l) + ColorDist(l4, i1);
                int j5 = l1 << 8 | j2 << 4;
                for(int l2 = i4; l2 <= j4; l2++)
                {
                    int k5 = l2 != 15 ? l2 << 4 : 255;
                    int l5 = j5 | l2;
                    int i6 = i5 + ColorDist(k5, j1);
                    if(i6 < error[l5])
                    {
                        if(error[l5] == 50000)
                            nEmpty--;
                        error[l5] = i6;
                        itab[l5] = (byte)j;
                    }
                }

            }

        }

    }

    private final void BuildInverseTable()
    {
        if(itab == null || error == null)
        {
            itab = new byte[4096];
            error = new int[4096];
        }
        for(int i = 0; i < 4096; i++)
            error[i] = 50000;

        nEmpty = 4096;
        for(int j = 3; j < 16 && nEmpty > 0; j++)
        {
            for(int k = 0; k < nColors; k++)
                FillCube(ctab[k], k, j);

        }

        error = null;
    }

    public synchronized boolean UpdateImageSize(int i, int j)
    {
        if(i != width || j != height)
        {
            SetImage(i, j, model);
            return true;
        } else
        {
            return false;
        }
    }

    public synchronized void SetImage(int i, int j, ColorModel colormodel)
    {
        model = colormodel;
        width = i;
        height = j;
        invalidate();
        if(model instanceof IndexColorModel)
        {
            IndexColorModel indexcolormodel = (IndexColorModel)model;
            nColors = indexcolormodel.getMapSize();
            byte abyte0[] = new byte[nColors];
            byte abyte1[] = new byte[nColors];
            byte abyte2[] = new byte[nColors];
            indexcolormodel.getReds(abyte0);
            indexcolormodel.getGreens(abyte1);
            indexcolormodel.getBlues(abyte2);
            ctab = new int[nColors];
            int k = 0;
            for(int l = 0; l < nColors; l++)
            {
                ctab[l] = 0xff000000 | (abyte0[l] & 0xff) << 16 | (abyte1[l] & 0xff) << 8 | abyte2[l] & 0xff;
                if(ctab[l] == -1)
                    k++;
            }

            if(k > 100)
            {
                forceCompleteScanlines = true;
                SetImage(i, j, ColorModel.getRGBdefault());
                return;
            }
            indexedColor = true;
            BuildInverseTable();
            if(pixels8 == null || pixelSize < 4 * width)
            {
                pixelSize = width * height;
                if(pixelSize > 0x186a0 && !flash.allocateFullClug)
                    pixelSize = 0x186a0;
                pixels8 = new byte[pixelSize];
                pixels32 = null;
            }
            return;
        }
        model = ColorModel.getRGBdefault();
        indexedColor = false;
        if(pixels32 == null || pixelSize < 4 * width)
        {
            pixelSize = width * height;
            if(pixelSize > 50000 && !flash.allocateFullClug)
                pixelSize = 50000;
            pixels8 = null;
            pixels32 = new int[pixelSize];
        }
        ctab = null;
        itab = null;
    }

    
    
    boolean update()
       {
       Rect rect = devDirtyRgn;
       if(rect.xmin != 0x80000000 && true)
          {
          produce(true);
          return true;
          } 
       else return false;
       }

    
    
    synchronized void gotoFrame(ScriptPlayer scriptplayer, int i)
    {
        scriptplayer.mute = true;
        scriptplayer.nActions = 0;
        scriptplayer.DrawFrame(i - 1);
        scriptplayer.mute = false;
        scriptplayer.nActions = 0;
        scriptplayer.DrawFrame(i);
    }

    synchronized int drawFrame(ScriptPlayer scriptplayer, int i)
    {
        return scriptplayer.DrawFrame(i);
    }

    public synchronized void addConsumer(ImageConsumer imageconsumer)
    {
        if(imageconsumer != theConsumer)
        {
            theConsumer = imageconsumer;
            newConsumer = true;
        }
    }

    public boolean isConsumer(ImageConsumer imageconsumer)
    {
        return theConsumer == imageconsumer;
    }

    public synchronized void removeConsumer(ImageConsumer imageconsumer)
    {
        if(theConsumer == imageconsumer)
            theConsumer = null;
    }

    public void startProduction(ImageConsumer imageconsumer)
    {
        addConsumer(imageconsumer);
        produce(false);
    }

    public void requestTopDownLeftRightResend(ImageConsumer imageconsumer)
    {
    }

    private synchronized void produce(boolean flag)
    {
        if(theConsumer == null)
        {
            flash.repaint();
            return;
        }
        if(newConsumer)
        {
            theConsumer.setDimensions(width, height);
            if(theConsumer == null)
                return;
            theConsumer.setColorModel(model);
            if(theConsumer == null)
                return;
            theConsumer.setHints(10);
            newConsumer = false;
        }
        if(theConsumer == null)
            return;
        if(!flag)
        {
            bitClip = new Rect(0, 0, width, height);
        } else
        {
            bitClip = new Rect(devDirtyRgn);
            if(antialias)
            {
                bitClip.xmin /= 4;
                bitClip.ymin /= 4;
                bitClip.xmax /= 4;
                bitClip.ymax /= 4;
            }
            bitClip.xmin -= 2;
            bitClip.xmax += 2;
            bitClip.ymin -= 2;
            bitClip.ymax += 2;
            bitClip.xmin = Math.max(bitClip.xmin, 0);
            bitClip.xmax = Math.min(bitClip.xmax, width);
            bitClip.ymin = Math.max(bitClip.ymin, 0);
            bitClip.ymax = Math.min(bitClip.ymax, height);
            if(bitClip.xmin >= bitClip.xmax || bitClip.ymin >= bitClip.ymax)
            {
                Rect rect = bitClip;
                rect.xmin = rect.xmax = rect.ymin = rect.ymax = 0x80000000;
            }
        }
        Rect rect1 = devDirtyRgn;
        rect1.xmin = rect1.xmax = rect1.ymin = rect1.ymax = 0x80000000;
        rect1 = bitClip;
        if(rect1.xmin != 0x80000000 && true)
        {
            if(forceCompleteScanlines)
            {
                bitClip.xmin = 0;
                bitClip.xmax = width;
            }
            if(antialias)
            {
                edgeClip = new Rect(bitClip);
                edgeClip.xmin *= 4;
                edgeClip.xmax *= 4;
                edgeClip.ymin *= 4;
                edgeClip.ymax *= 4;
            } else
            {
                edgeClip = bitClip;
            }
            ylines = (edgeClip.ymax - edgeClip.ymin) + 1;
            yindex = new REdge[ylines];
            if(activeEdges == null)
            {
                maxActive = 250;
                activeEdges = new REdge[maxActive];
            }
            nActive = 0;
            activeColors = null;
            topColor = null;
            topColorXleft = 0;
            for(SObject sobject = objects; sobject != null; sobject = sobject.next)
                if(sobject.devBounds.testIntersect(edgeClip))
                {
                    sobject.BuildEdges();
                    sobject.drawn = true;
                    AddEdges(sobject.edges);
                }

            if(backgroundColor != 0)
            {
                REdge redge = new REdge();
                REdge redge1 = new REdge();
                RColor rcolor = new RColor(this, backgroundColor);
                rcolor.order = 0;
                redge.nextObj = redge1;
                Point point = new Point(edgeClip.xmin, edgeClip.ymin);
                Point point1 = new Point(edgeClip.xmin, edgeClip.ymax);
                redge.set(point, point1);
                point.x = point1.x = edgeClip.xmax;
                redge1.set(point, point1);
                redge.fillRule = redge1.fillRule = 1;
                redge.color1 = redge1.color1 = rcolor;
                AddEdges(redge);
            }
            PaintBits();
            theConsumer.imageComplete(2);
            flash.updateImageToScreeen(true, bitClip.xmin, bitClip.ymin, (bitClip.xmax - bitClip.xmin) + 1, (bitClip.ymax - bitClip.ymin) + 1);
        }
    }

}
