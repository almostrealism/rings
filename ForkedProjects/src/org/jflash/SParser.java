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

class SParser
{
byte script[];
int  end,pos,bitBuf,bitPos;



   SParser()
      {  
      }

   
   
    final void Attach(byte abyte0[], int i)
    {
        script = abyte0;
        pos = i;
    }

    final int GetByte()
    {
        return script[pos++] & 0xff;
    }

    final int GetWord()
    {
        int i = script[pos] & 0xff | (script[pos + 1] & 0xff) << 8;
        pos += 2;
        return i;
    }

    final int GetSWord()
    {
        int i = GetWord();
        if((i & 0x8000) != 0)
            i |= 0xffff0000;
        return i;
    }

    final int GetDWord()
    {
        int i = script[pos] & 0xff | (script[pos + 1] & 0xff) << 8 | (script[pos + 2] & 0xff) << 16 | (script[pos + 3] & 0xff) << 24;
        pos += 4;
        return i;
    }

    final byte[] GetByteArray(int i)
    {
        byte abyte0[] = new byte[i];
        System.arraycopy(script, pos, abyte0, 0, i);
        pos += i;
        return abyte0;
    }

    final void CopyByteArray(byte abyte0[], int i, int j)
    {
        System.arraycopy(script, pos, abyte0, i, j);
        pos += j;
    }

    void GetColorTransform(ColorTransform colortransform)
    {
        bitPos = 0;
        bitBuf = 0;
        colortransform.flags = GetBits(2);
        int i = GetBits(4);
        if((colortransform.flags & 1) != 0)
        {
            colortransform.ra = GetSBits(i);
            colortransform.ga = GetSBits(i);
            colortransform.ba = GetSBits(i);
        } else
        {
            colortransform.ra = colortransform.ga = colortransform.ba = 256;
        }
        if((colortransform.flags & 2) != 0)
        {
            colortransform.rb = GetSBits(i);
            colortransform.gb = GetSBits(i);
            colortransform.bb = GetSBits(i);
            return;
        } else
        {
            colortransform.rb = colortransform.gb = colortransform.bb = 0;
            return;
        }
    }

    final int GetColor()
    {
        int i = 0xff000000;
        i |= (script[pos++] & 0xff) << 16;
        i |= (script[pos++] & 0xff) << 8;
        i |= script[pos++] & 0xff;
        return i;
    }

    final Rect GetRect()
    {
        bitPos = 0;
        bitBuf = 0;
        int i = GetBits(5);
        Rect rect = new Rect();
        rect.xmin = GetSBits(i);
        rect.xmax = GetSBits(i);
        rect.ymin = GetSBits(i);
        rect.ymax = GetSBits(i);
        return rect;
    }

    final Matrix GetMatrix()
    {
        bitPos = 0;
        bitBuf = 0;
        Matrix matrix = new Matrix();
        if(GetBits(1) != 0)
        {
            int i = GetBits(5);
            matrix.a = GetSBits(i);
            matrix.d = GetSBits(i);
        } else
        {
            matrix.a = matrix.d = 0x10000;
        }
        if(GetBits(1) != 0)
        {
            int j = GetBits(5);
            matrix.b = GetSBits(j);
            matrix.c = GetSBits(j);
        } else
        {
            matrix.b = matrix.c = 0;
        }
        int k = GetBits(5);
        matrix.tx = GetSBits(k);
        matrix.ty = GetSBits(k);
        return matrix;
    }

    final String GetString()
    {
        int i = 0;
        int j = pos;
        do
        {
            char c = (char)(script[pos++] & 0xff);
            if(c == 0)
                break;
            i++;
        } while(true);
        pos = j;
        char ac[] = new char[i];
        int k = 0;
        do
        {
            char c1 = (char)(script[pos++] & 0xff);
            if(k != i)
                ac[k++] = c1;
            else
                return new String(ac);
        } while(true);
    }

    final void InitBits()
    {
        bitPos = 0;
        bitBuf = 0;
    }

    final int GetBits(int i)
    {
        int j = 0;
        do
        {
            int k = i - bitPos;
            if(k > 0)
            {
                j |= bitBuf << k;
                i -= bitPos;
                bitBuf = script[pos++] & 0xff;
                bitPos = 8;
            } else
            {
                j |= bitBuf >>> -k;
                bitPos -= i;
                bitBuf &= 255 >>> 8 - bitPos;
                return j;
            }
        } while(true);
    }

    final int GetSBits(int i)
    {
        int j = GetBits(i);
        if((j & 1 << i - 1) != 0)
            j |= -1 << i;
        return j;
    }

    final void GetSoundInfo(int ai[], int ai1[], boolean flag)
    {
        int i = script[pos++] & 0xff;
        if(flag)
        {
            if((i & 1) != 0)
                GetDWord();
            if((i & 2) != 0)
                GetDWord();
            if((i & 4) != 0)
                GetWord();
            if((i & 8) != 0)
            {
                int j = script[pos++] & 0xff;
                for(int l = 0; l < j; l++)
                {
                    GetDWord();
                    GetWord();
                    GetWord();
                }

                return;
            }
        } else
        {
            ai1[0] = i >> 4;
            if((i & 1) != 0)
                GetDWord();
            if((i & 2) != 0)
                GetDWord();
            if((i & 4) != 0)
                ai[0] = GetWord();
            else
                ai[0] = 1;
            if((i & 8) != 0)
            {
                int k = script[pos++] & 0xff;
                for(int i1 = 0; i1 < k; i1++)
                {
                    GetDWord();
                    GetWord();
                    GetWord();
                }

            }
        }
    }


}
