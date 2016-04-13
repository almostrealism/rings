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

final class SCharacterParser extends SParser
{
   static final int fillSolid = 0;
   static final int fillGradient = 16;
   static final int fillLinearGradient = 16;
   static final int fillRadialGradient = 18;
   static final int fillMaxGradientColors = 8;
   static final int fillBits = 64;
   static final int fillBitsClip = 65;
   static final int fillBitsNoSmooth = 66;
   DisplayList display;
   ScriptPlayer player;
   ColorTransform cx;
   Matrix mat;
   int layer;
   int nLines;
   int nFills;
   RColor fillIndex[];
   RColor lineIndexColor[];
   int lineIndexThick[];
   int nFillBits;
   int nLineBits;
   int line;
   int fill0;
   int fill1;
   Point curPt;
   Point curPtX;
   Point curPtXc;
   Point curPtX2;
   public final int eflagsMoveTo = 1;
   public final int eflagsFill0 = 2;
   public final int eflagsFill1 = 4;
   public final int eflagsLine = 8;
   public final int eflagsNewStyles = 16;
   public final int eflagsEnd = 128;
   boolean useWinding;
   int depth;
   SObject obj;
   int fillRule;
   RColor color1;
   RColor color2;
   RColor strokeColor;
   boolean isThick;
   int lineThickness;
   boolean strokeInited;
   int strokeDepth;
   Point lStartPt;
   Point startOrigin;
   Point rStartPt;
   Point lCurPt;
   Point curOrigin;
   Point rCurPt;
   Point sCurPt;

   
   SCharacterParser(ScriptPlayer scriptplayer, int i, Matrix matrix, ColorTransform colortransform)
      {
      curPt = new Point(0, 0);
        curPtX = new Point(0, 0);
        curPtXc = new Point(0, 0);
        curPtX2 = new Point(0, 0);
        useWinding = false;
        lStartPt = new Point(0, 0);
        startOrigin = new Point(0, 0);
        rStartPt = new Point(0, 0);
        lCurPt = new Point(0, 0);
        curOrigin = new Point(0, 0);
        rCurPt = new Point(0, 0);
        sCurPt = new Point(0, 0);
        mat = matrix;
        cx = colortransform;
        layer = 0;
        nLines = nFills = 0;
        line = fill0 = fill1 = 0;
        curPt.x = curPt.y = 0;
        mat.transform(curPt, curPtX);
        player = scriptplayer;
        display = scriptplayer.display;
        byte abyte0[] = ((SParser) (player)).script;
        super.script = abyte0;
        super.pos = i;
      }

    private void buildCache(RColor rcolor)
    {
        if(rcolor.cacheValid)
            return;
        switch(rcolor.fillType)
        {
        case 64: // '@'
        case 65: // 'A'
        case 66: // 'B'
            if(rcolor.bitmap == null)
            {
                rcolor.fillType = 0;
                return;
            }
            rcolor.bmFast = true;
            rcolor.bmDx = rcolor.bmInvMat.a;
            rcolor.bmDy = rcolor.bmInvMat.b;
            if(cx != null)
            {
                cx.BuildMapChannels();
                rcolor.cx = cx;
                rcolor.bmFast = false;
                return;
            }
            break;
        }
    }

    boolean GetStyles()
    {
        nFills = super.script[super.pos++] & 0xff;
        if(nFills == 255)
            nFills = GetWord();
        if((fillIndex = new RColor[nFills + 1]) == null)
            return false;
        for(int i = 1; i <= nFills; i++)
        {
            RColor rcolor = null;
            int k = super.script[super.pos++] & 0xff;
            if((k & 0x10) != 0)
            {
                Matrix matrix = GetMatrix();
                int i1 = super.script[super.pos++] & 0xff;
                int ai[] = new int[i1];
                int ai1[] = new int[i1];
                for(int j1 = 0; j1 < i1; j1++)
                {
                    ai1[j1] = super.script[super.pos++] & 0xff;
                    ai[j1] = GetColor();
                }

                rcolor = new RColor(display, k, i1, ai, ai1, matrix, mat);
                if(cx != null)
                    cx.ApplyGradient(rcolor);
            } else
            if((k & 0x40) != 0)
            {
                int l = GetWord();
                Matrix matrix1 = GetMatrix();
                rcolor = new RColor(display, -65281);
                SCharacter scharacter = player.findCharacter(l);
                if(scharacter != null && scharacter.type == 1)
                {
                    if(scharacter.object != null && (scharacter.object instanceof Bitmap))
                    {
                        rcolor.bitmap = (Bitmap)scharacter.object;
                        rcolor.fillType = k;
                        if(display.model != null)
                        {
                            Matrix matrix2;
                            if(display.antialias)
                            {
                                Matrix matrix3 = new Matrix();
                                matrix3.scale(16384, 16384);
                                matrix2 = Matrix.concat(mat, matrix3);
                            } else
                            {
                                matrix2 = new Matrix(mat);
                            }
                            matrix1.tx <<= 16;
                            matrix1.ty <<= 16;
                            matrix2.tx <<= 16;
                            matrix2.ty <<= 16;
                            matrix1 = Matrix.concat(matrix1, matrix2);
                            rcolor.bmInvMat = matrix1.invert();
                            buildCache(rcolor);
                        }
                    }
                } else
                {
                    rcolor = new RColor(display, 0xffff0000);
                }
            } else
            {
                rcolor = new RColor(display, GetColor());
                if(cx != null)
                    cx.Apply(rcolor);
            }
            fillIndex[i] = rcolor;
            rcolor.order = layer + i;
        }

        nLines = super.script[super.pos++] & 0xff;
        if(nLines == 255)
            nLines = GetWord();
        lineIndexColor = new RColor[nLines + 1];
        lineIndexThick = new int[nLines + 1];
        if(lineIndexColor == null || lineIndexThick == null)
        {
            lineIndexColor = null;
            lineIndexThick = null;
            return false;
        }
        for(int j = 1; j <= nLines; j++)
        {
            lineIndexThick[j] = mat.transform(GetWord());
            RColor rcolor1 = new RColor(display, GetColor());
            if(cx != null)
                cx.Apply(rcolor1);
            lineIndexColor[j] = rcolor1;
            rcolor1.order = layer | j + nFills;
        }

        super.bitPos = 0;
        super.bitBuf = 0;
        nFillBits = GetBits(4);
        nLineBits = GetBits(4);
        return true;
    }

    int GetEdge(Curve curve)
    {
        boolean flag = GetBits(1) != 0;
        if(!flag)
        {
            int i = GetBits(5);
            if(i == 0)
                return 128;
            if((i & 1) != 0)
            {
                int l = GetBits(5);
                curPt.x = GetSBits(l);
                curPt.y = GetSBits(l);
                mat.transform(curPt, curPtX);
            }
            if((i & 2) != 0)
                fill0 = GetBits(nFillBits);
            if((i & 4) != 0)
                fill1 = GetBits(nFillBits);
            if((i & 8) != 0)
                line = GetBits(nLineBits);
            if((i & 0x10) != 0)
            {
                layer += nFills + nLines;
                GetStyles();
            }
            return i;
        }
        curve.isLine = GetBits(1) != 0;
        if(curve.isLine)
        {
            int j = GetBits(4) + 2;
            boolean flag1 = GetBits(1) != 0;
            if(flag1)
            {
                curPt.x += GetSBits(j);
                curPt.y += GetSBits(j);
            } else
            {
                boolean flag2 = GetBits(1) != 0;
                if(flag2)
                    curPt.y += GetSBits(j);
                else
                    curPt.x += GetSBits(j);
            }
            mat.transform(curPt, curPtX2);
            curve.set(curPtX, curPtX2);
        } else
        {
            int k = GetBits(4) + 2;
            curPt.x += GetSBits(k);
            curPt.y += GetSBits(k);
            mat.transform(curPt, curPtXc);
            curPt.x += GetSBits(k);
            curPt.y += GetSBits(k);
            mat.transform(curPt, curPtX2);
            curve.set(curPtX, curPtXc, curPtX2);
        }
        curPtX.x = curPtX2.x;
        curPtX.y = curPtX2.y;
        return 0;
    }

    void AddCurve(Curve curve, boolean flag)
    {
        REdge redge = new REdge();
        if(curve.anchor1y <= curve.anchor2y)
        {
            redge.anchor1x = curve.anchor1x;
            redge.anchor1y = curve.anchor1y;
            redge.anchor2x = curve.anchor2x;
            redge.anchor2y = curve.anchor2y;
            redge.dir = 1;
        } else
        {
            redge.anchor1x = curve.anchor2x;
            redge.anchor1y = curve.anchor2y;
            redge.anchor2x = curve.anchor1x;
            redge.anchor2y = curve.anchor1y;
            redge.dir = -1;
        }
        redge.isLine = curve.isLine;
        redge.controlx = curve.controlx;
        redge.controly = curve.controly;
        if(!((Curve) (redge)).isLine)
        {
            if(((Curve) (redge)).controly < ((Curve) (redge)).anchor1y || ((Curve) (redge)).controly > ((Curve) (redge)).anchor2y)
                if(((Curve) (redge)).controly < ((Curve) (redge)).anchor1y && ((Curve) (redge)).anchor1y - ((Curve) (redge)).controly < 3)
                    redge.controly = ((Curve) (redge)).anchor1y;
                else
                if(curve.controly > ((Curve) (redge)).anchor2y && curve.controly - ((Curve) (redge)).anchor2y < 3)
                {
                    redge.controly = ((Curve) (redge)).anchor2y;
                } else
                {
                    int i = (curve.anchor1y - 2 * curve.controly) + curve.anchor2y;
                    int j = curve.anchor1y - curve.controly;
                    if(++depth > 16)
                    {
                        return;
                    } else
                    {
                        Curve curve3 = new Curve(curve);
                        Curve curve4 = curve3.divide((int)(((long)j << 16) /i));
                        AddCurve(curve3, flag);
                        AddCurve(curve4, flag);
                        depth--;
                        return;
                    }
                }
            if(((Curve) (redge)).anchor2y - ((Curve) (redge)).anchor1y > 256)
                if(++depth > 16)
                {
                    return;
                } else
                {
                    Curve curve1 = new Curve(curve);
                    Curve curve2 = curve1.divide(32768);
                    AddCurve(curve1, flag);
                    AddCurve(curve2, flag);
                    depth--;
                    return;
                }
        }
        if(((Curve) (redge)).anchor1y == ((Curve) (redge)).anchor2y)
            return;
        if(flag)
        {
            redge.fillRule = 2;
            redge.color1 = strokeColor;
        } else
        {
            redge.fillRule = fillRule;
            redge.color1 = color1;
            redge.color2 = color2;
        }
        redge.nextObj = obj.edges;
        obj.edges = redge;
    }

    void BuildEdges(boolean flag)
    {
        depth = 0;
        layer = obj.depth; //id << 16;
        if(flag)
        {
            if(!GetStyles())
                return;
        } else
        {
            super.bitPos = 0;
            super.bitBuf = 0;
            nFillBits = GetBits(4);
            nLineBits = GetBits(4);
        }
        boolean flag1 = false;
        boolean flag2 = false;
        Curve curve = new Curve();
        color1 = color2 = null;
        do
        {
            int i = GetEdge(curve);
            if(i != 0)
            {
                if(i == 128)
                    if(flag2)
                    {
                        EndStroke();
                        return;
                    } else
                    {
                        return;
                    }
                if((i & 6) != 0)
                {
                    color1 = fillIndex[fill0];
                    color2 = fillIndex[fill1];
                    if(color1 == null && color2 != null)
                    {
                        color1 = color2;
                        color2 = null;
                    }
                    fillRule = color2 == null ? useWinding ? 2 : 1 : 0;
                    flag1 = color1 != null;
                }
                if((i & 9) != 0)
                {
                    if(flag2)
                        EndStroke();
                    if(line != 0)
                    {
//System.err.println(lineIndexThick);
//System.err.println(lineIndexColor);
                        BeginStroke(lineIndexThick[line], lineIndexColor[line]);
                        flag2 = true;
                    } else
                    {
                        flag2 = false;
                    }
                }
            } else
            {
                if(flag2)
                    AddStrokeCurve(curve);
                if(flag1)
                    AddCurve(curve, false);
            }
        } while(true);
    }

    final void AddEdge(Point point, Point point1)
    {
        if(point.y == point1.y)
            return;
        REdge redge = new REdge();
        if(point.y > point1.y)
        {
            redge.dir = -1;
            redge.set(point1, point);
        } else
        {
            redge.dir = 1;
            redge.set(point, point1);
        }
        redge.fillRule = 2;
        redge.color1 = strokeColor;
        redge.nextObj = obj.edges;
        obj.edges = redge;
    }

    final void StrokeJoin(Point point, Point point1, Point point2)
    {
        int i = Matrix.fastLength(point.x - point1.x, point.y - point1.y);
        if(i > 3)
        {
            double d = Math.atan2(point.y - point2.y, point.x - point2.x);
            double d1;
            for(d1 = Math.atan2(point1.y - point2.y, point1.x - point2.x); d < d1; d += 6.2831853071795862D);
            double d2 = d - d1;
            if(d2 > 0.10000000000000001D && d2 <= 3.1415926535897931D)
            {
                double d3 = lineThickness / 2;
                int j = (int)(d3 * d2) / 3;
                Point point3 = new Point(point.x, point.y);
                Point point4 = new Point(0, 0);
                if(j > 1)
                {
                    if(j > 16)
                        j = 16;
                    double d4 = -d2 /j;
                    double d5 = d + d4;
                    for(j--; j-- > 0;)
                    {
                        point4.x = (int)(d3 * Math.cos(d5)) + point2.x;
                        point4.y = (int)(d3 * Math.sin(d5)) + point2.y;
                        AddEdge(point3, point4);
                        point3.x = point4.x;
                        point3.y = point4.y;
                        d5 += d4;
                    }

                }
                AddEdge(point3, point1);
                return;
            }
        }
        AddEdge(point, point1);
    }

    static final Curve CurveAdjust(Curve curve, Point point, Point point1)
    {
        int i = Matrix.length(curve.anchor1x - curve.anchor2x, curve.anchor1y - curve.anchor2y);
        int j;
        if(i > 0)
        {
            int k = Matrix.length(point.x - point1.x, point.y - point1.y);
            j = (int)(((long)k << 16) /i);
        } else
        {
            j = 0x10000;
        }
        int l = curve.controlx - curve.anchor1x;
        int i1 = curve.controly - curve.anchor1y;
        int j1 = curve.controlx - curve.anchor2x;
        int k1 = curve.controly - curve.anchor2y;
        Curve curve1 = new Curve();
        curve1.anchor1x = point.x;
        curve1.anchor1y = point.y;
        curve1.anchor2x = point1.x;
        curve1.anchor2y = point1.y;
        if(Matrix.fastLength(l, i1) > Matrix.fastLength(j1, k1))
        {
            curve1.controlx = (int)((long)j * (long)l + 32768L >> 16) + point.x;
            curve1.controly = (int)((long)j * (long)i1 + 32768L >> 16) + point.y;
        } else
        {
            curve1.controlx = (int)((long)j * (long)j1 + 32768L >> 16) + point1.x;
            curve1.controly = (int)((long)j * (long)k1 + 32768L >> 16) + point1.y;
        }
        return curve1;
    }

    static final Curve CurveReverse(Curve curve)
    {
        Curve curve1 = new Curve();
        curve1.isLine = curve.isLine;
        curve1.anchor1x = curve.anchor2x;
        curve1.anchor1y = curve.anchor2y;
        curve1.controlx = curve.controlx;
        curve1.controly = curve.controly;
        curve1.anchor2x = curve.anchor1x;
        curve1.anchor2y = curve.anchor1y;
        return curve1;
    }

    final void StrokeThickCurve(Curve curve)
    {
        if(!curve.isLine && strokeDepth < 5)
        {
            int i = curve.flatness();
            if(i > 6)
            {
                int k = Matrix.fastLength(curve.anchor1x - curve.anchor2x, curve.anchor1y - curve.anchor2y);
                if(2 * i > k)
                {
                    Curve curve1 = new Curve(curve);
                    Curve curve2 = curve1.divide(32768);
                    strokeDepth++;
                    StrokeThickCurve(curve1);
                    StrokeThickCurve(curve2);
                    strokeDepth--;
                    return;
                }
            }
        }
        int j = lineThickness / 2;
        int l = curve.controly - curve.anchor1y;
        int i1 = curve.anchor1x - curve.controlx;
        if(l == 0 && i1 == 0)
        {
            l = curve.anchor2y - curve.anchor1y;
            i1 = curve.anchor1x - curve.anchor2x;
        }
        int j1 = Matrix.length(l, i1);
        if(j1 > 0)
        {
            j1 = (int)(((long)j << 16) /j1);
            l = (int)((long)j1 * (long)l + 32768L >> 16);
            i1 = (int)((long)j1 * (long)i1 + 32768L >> 16);
        }
        int l1;
        int i2;
        if(curve.isLine)
        {
            l1 = l;
            i2 = i1;
        } else
        {
            l1 = curve.anchor2y - curve.controly;
            i2 = curve.controlx - curve.anchor2x;
            if(l1 == 0 && i2 == 0)
            {
                l1 = curve.anchor2y - curve.anchor1y;
                i2 = curve.anchor1x - curve.anchor2x;
            }
            int k1 = Matrix.length(l1, i2);
            if(k1 > 0)
            {
                k1 = (int)(((long)j << 16) /k1);
                l1 = (int)((long)k1 * (long)l1 + 32768L >> 16);
                i2 = (int)((long)k1 * (long)i2 + 32768L >> 16);
            }
        }
        Point point = new Point(curve.anchor1x + l, curve.anchor1y + i1);
        Point point1 = new Point(curve.anchor2x + l1, curve.anchor2y + i2);
        Point point2 = new Point(curve.anchor1x - l, curve.anchor1y - i1);
        Point point3 = new Point(curve.anchor2x - l1, curve.anchor2y - i2);
        if(curve.isLine)
        {
            AddEdge(point1, point);
            AddEdge(point2, point3);
        } else
        {
            AddCurve(CurveReverse(CurveAdjust(curve, point, point1)), true);
            AddCurve(CurveAdjust(curve, point2, point3), true);
        }
        if(!strokeInited)
        {
            lStartPt.x = point.x;
            lStartPt.y = point.y;
            startOrigin.x = curve.anchor1x;
            startOrigin.y = curve.anchor1y;
            rStartPt.x = point2.x;
            rStartPt.y = point2.y;
            strokeInited = true;
        } else
        {
            StrokeJoin(point, lCurPt, curOrigin);
            StrokeJoin(rCurPt, point2, curOrigin);
        }
        lCurPt.x = point1.x;
        lCurPt.y = point1.y;
        curOrigin.x = curve.anchor2x;
        curOrigin.y = curve.anchor2y;
        rCurPt.x = point3.x;
        rCurPt.y = point3.y;
    }

    static final int Sign(int i)
    {
        if(i < 0)
            return -1;
        return i <= 0 ? 0 : 1;
    }

    final void StrokeThinLine(Curve curve)
    {
        int i = curve.anchor2y - curve.anchor1y;
        int j = curve.anchor1x - curve.anchor2x;
        Point point = new Point(curve.anchor1x, curve.anchor1y);
        Point point1 = new Point(curve.anchor1x, curve.anchor1y);
        Point point2 = new Point(curve.anchor2x, curve.anchor2y);
        Point point3 = new Point(curve.anchor2x, curve.anchor2y);
        boolean flag = (i <= 0 ? -i : i) > (j <= 0 ? -j : j);
        switch(lineThickness)
        {
        default:
            break;

        case 1: // '\001'
            if(flag)
            {
                byte byte0 = i >= 0 ? ((byte) (((byte)(i <= 0 ? 0 : 1)))) : -1;
                if(byte0 > 0)
                {
                    point.x += byte0;
                    point2.x += byte0;
                } else
                {
                    point1.x -= byte0;
                    point3.x -= byte0;
                }
                break;
            }
            byte byte1 = j >= 0 ? ((byte) (((byte)(j <= 0 ? 0 : 1)))) : -1;
            if(byte1 > 0)
            {
                point.y += byte1;
                point2.y += byte1;
            } else
            {
                point1.y -= byte1;
                point3.y -= byte1;
            }
            break;

        case 2: // '\002'
            if(flag)
            {
                byte byte2 = i >= 0 ? ((byte) (((byte)(i <= 0 ? 0 : 1)))) : -1;
                point.x += byte2;
                point2.x += byte2;
                point1.x -= byte2;
                point3.x -= byte2;
            } else
            {
                byte byte3 = j >= 0 ? ((byte) (((byte)(j <= 0 ? 0 : 1)))) : -1;
                point.y += byte3;
                point2.y += byte3;
                point1.y -= byte3;
                point3.y -= byte3;
            }
            break;

        case 3: // '\003'
            if(flag)
            {
                int k = i >= 0 ? ((int) (i <= 0 ? 0 : 1)) : -1;
                point.x += k;
                point2.x += k;
                k *= 2;
                point1.x -= k;
                point3.x -= k;
            } else
            {
                int l = j >= 0 ? ((int) (j <= 0 ? 0 : 1)) : -1;
                point.y += l;
                point2.y += l;
                l *= 2;
                point1.y -= l;
                point3.y -= l;
            }
            break;
        }
        AddEdge(point2, point);
        AddEdge(point1, point3);
        if(!strokeInited)
        {
            lStartPt.x = point.x;
            lStartPt.y = point.y;
            startOrigin.x = curve.anchor1x;
            startOrigin.y = curve.anchor1y;
            rStartPt.x = point1.x;
            rStartPt.y = point1.y;
            strokeInited = true;
        } else
        {
            AddEdge(point, lCurPt);
            AddEdge(rCurPt, point1);
        }
        lCurPt.x = point2.x;
        lCurPt.y = point2.y;
        curOrigin.x = curve.anchor2x;
        curOrigin.y = curve.anchor2y;
        rCurPt.x = point3.x;
        rCurPt.y = point3.y;
    }

/*    private static final void curveOffsetX(Curve curve, int i)
    {
        curve.anchor1x += i;
        curve.controlx += i;
        curve.anchor2x += i;
    }

    private static final void curveOffsetY(Curve curve, int i)
    {
        curve.anchor1y += i;
        curve.controly += i;
        curve.anchor2y += i;
    }

    private static final boolean sameSign(int i, int j)
    {
        if(i == 0)
            return true;
        if(i > 0)
            return j >= 0;
        return j <= 0;
    }*/

    final void StrokeThinCurve(Curve curve)
    {
        if(!curve.isLine && curve.flatness() > 2)
        {
            int i = curve.controly - curve.anchor1y;
            int k = curve.anchor1x - curve.controlx;
            int i1 = curve.anchor2y - curve.controly;
            int j1 = curve.controlx - curve.anchor2x;
            boolean flag = (i <= 0 ? -i : i) > (k <= 0 ? -k : k);
            boolean flag2 = (i1 <= 0 ? -i1 : i1) > (j1 <= 0 ? -j1 : j1);
            if(flag != flag2 || (i != 0 ? i <= 0 ? i1 > 0 && true : i1 < 0 && true : false) || (k != 0 ? k <= 0 ? j1 > 0 && true : j1 < 0 && true : false))
            {
                Curve curve3 = new Curve(curve);
                Curve curve4 = curve3.divide(32768);
                StrokeThinCurve(curve3);
                StrokeThinCurve(curve4);
                return;
            }
        }
        int j = curve.anchor2y - curve.anchor1y;
        int l = curve.anchor1x - curve.anchor2x;
        Curve curve1 = new Curve(curve);
        Curve curve2 = new Curve(curve);
        boolean flag1 = (j <= 0 ? -j : j) > (l <= 0 ? -l : l);
        switch(lineThickness)
        {
        default:
            break;

        case 1: // '\001'
            if(flag1)
            {
                byte byte0 = j >= 0 ? ((byte) (((byte)(j <= 0 ? 0 : 1)))) : -1;
                if(byte0 > 0)
                {
                    curve1.anchor1x += byte0;
                    curve1.controlx += byte0;
                    curve1.anchor2x += byte0;
                } else
                {
                    int k1 = -byte0;
                    curve2.anchor1x += k1;
                    curve2.controlx += k1;
                    curve2.anchor2x += k1;
                }
                break;
            }
            byte byte1 = l >= 0 ? ((byte) (((byte)(l <= 0 ? 0 : 1)))) : -1;
            if(byte1 > 0)
            {
                curve1.anchor1y += byte1;
                curve1.controly += byte1;
                curve1.anchor2y += byte1;
            } else
            {
                int l1 = -byte1;
                curve2.anchor1y += l1;
                curve2.controly += l1;
                curve2.anchor2y += l1;
            }
            break;

        case 2: // '\002'
            if(flag1)
            {
                byte byte2 = j >= 0 ? ((byte) (((byte)(j <= 0 ? 0 : 1)))) : -1;
                curve1.anchor1x += byte2;
                curve1.controlx += byte2;
                curve1.anchor2x += byte2;
                int i2 = -byte2;
                curve2.anchor1x += i2;
                curve2.controlx += i2;
                curve2.anchor2x += i2;
            } else
            {
                byte byte3 = l >= 0 ? ((byte) (((byte)(l <= 0 ? 0 : 1)))) : -1;
                curve1.anchor1y += byte3;
                curve1.controly += byte3;
                curve1.anchor2y += byte3;
                int j2 = -byte3;
                curve2.anchor1y += j2;
                curve2.controly += j2;
                curve2.anchor2y += j2;
            }
            break;

        case 3: // '\003'
            if(flag1)
            {
                byte byte4 = j >= 0 ? ((byte) (((byte)(j <= 0 ? 0 : 1)))) : -1;
                curve1.anchor1x += byte4;
                curve1.controlx += byte4;
                curve1.anchor2x += byte4;
                int k2 = -2 * byte4;
                curve2.anchor1x += k2;
                curve2.controlx += k2;
                curve2.anchor2x += k2;
            } else
            {
                byte byte5 = l >= 0 ? ((byte) (((byte)(l <= 0 ? 0 : 1)))) : -1;
                curve1.anchor1y += byte5;
                curve1.controly += byte5;
                curve1.anchor2y += byte5;
                int l2 = -2 * byte5;
                curve2.anchor1y += l2;
                curve2.controly += l2;
                curve2.anchor2y += l2;
            }
            break;
        }
        AddCurve(CurveReverse(curve1), true);
        AddCurve(curve2, true);
        if(!strokeInited)
        {
            lStartPt.x = curve1.anchor1x;
            lStartPt.y = curve1.anchor1y;
            startOrigin.x = curve.anchor1x;
            startOrigin.y = curve.anchor1y;
            rStartPt.x = curve2.anchor1x;
            rStartPt.y = curve2.anchor1y;
            strokeInited = true;
        } else
        {
            AddEdge(new Point(curve1.anchor1x, curve1.anchor1y), lCurPt);
            AddEdge(rCurPt, new Point(curve2.anchor1x, curve2.anchor1y));
        }
        lCurPt.x = curve1.anchor2x;
        lCurPt.y = curve1.anchor2y;
        curOrigin.x = curve.anchor2x;
        curOrigin.y = curve.anchor2y;
        rCurPt.x = curve2.anchor2x;
        rCurPt.y = curve2.anchor2y;
    }

    final void BeginStroke(int i, RColor rcolor)
    {
        strokeInited = false;
        lineThickness = Math.max(display.antialias ? 4 : 1, i);
        isThick = lineThickness > 3;
        strokeColor = rcolor;
        sCurPt.x = sCurPt.y = 0x80000000;
    }

    final void AddStrokeCurve(Curve curve)
    {
label0:
        {
label1:
            {
                Curve curve1;
label2:
                {
                    sCurPt.x = curve.anchor2x;
                    sCurPt.y = curve.anchor2y;
                    if(curve.anchor1x == curve.anchor2x && curve.anchor1y == curve.anchor2y && curve.anchor1x == curve.controlx && curve.anchor1y == curve.controly)
                        return;
                    if(!isThick)
                        if(curve.isLine)
                        {
                            StrokeThinLine(curve);
                            return;
                        } else
                        {
                            StrokeThinCurve(curve);
                            return;
                        }
                    if(!display.antialias || !curve.isLine)
                        break label0;
                    if(lineThickness != 4 && lineThickness != 12)
                        break label1;
                    curve1 = new Curve(curve);
                    if(curve1.anchor1x == curve1.anchor2x)
                    {
                        int i = curve1.anchor1y - curve1.anchor2y;
                        if((i <= 0 ? -i : i) > 12)
                        {
                            curve1.anchor1x = curve1.anchor2x = (curve1.anchor1x & -4) + 2;
                            break label2;
                        }
                    }
                    if(curve1.anchor1y == curve1.anchor2y)
                    {
                        int j = curve1.anchor1x - curve1.anchor2x;
                        if((j <= 0 ? -j : j) > 12)
                            curve1.anchor1y = curve1.anchor2y = (curve1.anchor1y & -4) + 2;
                    }
                }
                StrokeThickCurve(curve1);
                return;
            }
label3:
            {
                Curve curve2;
label4:
                {
                    if(lineThickness != 8)
                        break label3;
                    curve2 = new Curve(curve);
                    if(curve2.anchor1x == curve2.anchor2x)
                    {
                        int k = curve2.anchor1y - curve2.anchor2y;
                        if((k <= 0 ? -k : k) > 12)
                        {
                            curve2.anchor1x = curve2.anchor2x = curve2.anchor1x + 2 & -4;
                            break label4;
                        }
                    }
                    if(curve2.anchor1y == curve2.anchor2y)
                    {
                        int l = curve2.anchor1x - curve2.anchor2x;
                        if((l <= 0 ? -l : l) > 12)
                            curve2.anchor1y = curve2.anchor2y = curve2.anchor1y + 2 & -4;
                    }
                }
                StrokeThickCurve(curve2);
                return;
            }
            StrokeThickCurve(curve);
            return;
        }
        StrokeThickCurve(curve);
    }

    final void EndStroke()
    {
        if(strokeInited)
        {
            if(startOrigin.x == curOrigin.x && startOrigin.y == curOrigin.y)
                if(!isThick)
                {
                    AddEdge(lStartPt, lCurPt);
                    AddEdge(rCurPt, rStartPt);
                    return;
                } else
                {
                    StrokeJoin(lStartPt, lCurPt, curOrigin);
                    StrokeJoin(rCurPt, rStartPt, curOrigin);
                    return;
                }
            if(!isThick)
            {
                AddEdge(lStartPt, rStartPt);
                AddEdge(rCurPt, lCurPt);
                return;
            } else
            {
                StrokeJoin(lStartPt, rStartPt, startOrigin);
                StrokeJoin(rCurPt, lCurPt, curOrigin);
                return;
            }
        }
        if(sCurPt.x != 0x80000000)
        {
            int i = lineThickness / 2;
            Point point = new Point(sCurPt.x, sCurPt.y);
            Point point1 = new Point(sCurPt.x, sCurPt.y);
            point.y -= i;
            point1.y += lineThickness - i;
            if(!isThick)
            {
                point.x -= i;
                point1.x -= i;
                AddEdge(point, point1);
                point.x += lineThickness - i;
                point1.x += lineThickness - i;
                AddEdge(point1, point);
                return;
            }
            StrokeJoin(point, point1, sCurPt);
            StrokeJoin(point1, point, sCurPt);
        }
    }

}
