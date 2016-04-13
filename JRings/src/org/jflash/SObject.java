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

final class SObject
{
/*   static final int tflagsX = 1;
   static final int tflagsY = 2;
   static final int tflagsColor = 4;
   static final int tflagsFont = 8;
   static final int tflagsHeight = 8;*/
  
   DisplayList display;
   SObject next;
   SCharacter character;
   int ID,depth;
   Matrix mat;
   Matrix devMat;
   Rect devBounds;
   boolean drawn;
   int buttonState;
   REdge edges;
   ColorTransform cx;

   SObject()
      {
      }

    final void ClearEdges()
    {
        edges = null;
    }

    final void BuildEdges()
    {
        ScriptPlayer scriptplayer = character.player;
        if(edges != null || scriptplayer.scriptErr != 0)
            return;
        switch(character.type)
        {
        case 0: // '\0'
            SCharacterParser scharacterparser = new SCharacterParser(scriptplayer, character.dataPos, devMat, cx);
            scharacterparser.obj = this;
            scharacterparser.BuildEdges(true);
            return;

        case 4: // '\004'
            SParser sparser = new SParser();
            byte abyte0[] = ((SParser) (scriptplayer)).script;
            int i = character.dataPos;
            sparser.script = abyte0;
            sparser.pos = i;
            Matrix matrix = Matrix.concat(sparser.GetMatrix(), devMat);
            i = sparser.script[sparser.pos++] & 0xff;
            int j = sparser.script[sparser.pos++] & 0xff;
            int k =depth; // id << 16;
            int l = 0;
            RColor rcolor = null;
            Matrix matrix1 = new Matrix();
            SCharacter scharacter = null;
            do
            {
                if(l == 0)
                {
                    int i1 = sparser.script[sparser.pos++] & 0xff;
                    if(i1 == 0)
                        break;
                    if((i1 & 8) != 0)
                        scharacter = scriptplayer.findCharacter(sparser.GetWord());
                    if((i1 & 4) != 0)
                    {
                        rcolor = new RColor(display, sparser.GetColor());
                        rcolor.order=k;
                        k++;
                        if(cx != null)
                            cx.Apply(rcolor);
                    }
                    if((i1 & 1) != 0)
                        matrix1.tx = sparser.GetSWord();
                    if((i1 & 2) != 0)
                        matrix1.ty = sparser.GetSWord();
                    if((i1 & 8) != 0)
                        matrix1.a = matrix1.d = sparser.GetWord() * 64;
                    l = sparser.script[sparser.pos++] & 0xff;
                    sparser.bitPos = 0;
                    sparser.bitBuf = 0;
                }
                int j1 = sparser.GetBits(i);
                int k1 = sparser.GetSBits(j);
                if(scharacter != null)
                {
                    int l1 = scharacter.dataPos + 2 * j1;
                    int i2 = ((SParser) (scriptplayer)).script[l1] & 0xff | (((SParser) (scriptplayer)).script[l1 + 1] & 0xff) << 8;
                    SCharacterParser scharacterparser1 = new SCharacterParser(scriptplayer, scharacter.dataPos + i2, Matrix.concat(matrix1, matrix), cx);
                    scharacterparser1.useWinding = true;
                    scharacterparser1.obj = this;
                    scharacterparser1.nFills = 1;
                    scharacterparser1.fillIndex = new RColor[2];
                    scharacterparser1.fillIndex[1] = rcolor;
                    scharacterparser1.BuildEdges(false);
                }
                matrix1.tx += k1;
                l--;
            } while(true);
            return;
        }
    }

    boolean HitTest(Point point)
    {
        boolean flag = false;
        if(devBounds != null && devBounds.pointIn(point))
        {
            if(edges == null)
                BuildEdges();
            boolean flag1 = false;
            for(REdge redge = edges; redge != null; redge = redge.nextObj)
                if(((Curve) (redge)).anchor1y <= point.y && point.y < ((Curve) (redge)).anchor2y && redge.XRaySect(point, 0) > 0)
                {
                    flag1 = true;
                    switch(redge.fillRule)
                    {
                    case 0: // '\0'
                        redge.color1.visible ^= 1;
                        redge.color2.visible ^= 1;
                        break;

                    case 1: // '\001'
                        redge.color1.visible ^= 1;
                        break;

                    case 2: // '\002'
                        redge.color1.visible += redge.dir;
                        break;
                    }
                }

            if(flag1)
            {
                for(REdge redge1 = edges; redge1 != null; redge1 = redge1.nextObj)
                {
                    if(redge1.color1 != null)
                    {
                        if(redge1.color1.visible != 0)
                            flag = true;
                        redge1.color1.visible = 0;
                    }
                    if(redge1.color2 != null)
                    {
                        if(redge1.color2.visible != 0)
                            flag = true;
                        redge1.color2.visible = 0;
                    }
                }

            }
        }
        return flag;
    }

}
