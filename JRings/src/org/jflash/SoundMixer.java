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

import java.io.ByteArrayInputStream;

final class SoundMixer extends ByteArrayInputStream
{
private int       length,index;
private FlashPane flash;

    SoundMixer(byte abyte0[], int i,FlashPane flash)
       {
       super(abyte0);
       this.flash=flash;
       if(i < 0) super.count = 0x7fffffff;
       else super.count = abyte0.length * i;
       length = abyte0.length;
       flash.stopAllSoundStreams = false;
       }

    
    
    public synchronized int read()
       {
       if(flash.stopAllSoundStreams) return -1;
       if(super.pos < super.count)
          {
          super.pos++;
          if(index >= length) index = 0;
          return super.buf[index++] & 0xff;
          } 
       else return -1;
       }

    
    
    private int localRead(byte abyte0[], int i, int j)
    {
        if(index >= length)
            return -1;
        if(index + j > length)
            j = length - index;
        if(j <= 0)
        {
            return 0;
        } else
        {
            System.arraycopy(super.buf, index, abyte0, i, j);
            index += j;
            return j;
        }
    }

    public synchronized int read(byte abyte0[], int i, int j)
       {
       if(flash.stopAllSoundStreams) return -1;
       if(super.pos >= super.count) return -1;
       if(super.pos + j > super.count) j = super.count - super.pos;
       if(j <= 0) return 0;
       if(index + j <= length)
          {
          System.arraycopy(super.buf, index, abyte0, i, j);
          index += j;
          } 
       else
          {
          for(int k = 0; k < j;)
             {
             int l = localRead(abyte0, i + k, j - k);
             if(l >= 0) k += l;
             else index = 0;
             }
          }
       super.pos += j;
       return j;
       }

    
    
    public synchronized long skip(long l)
       {
       if(flash.stopAllSoundStreams) return 0L;
       if(super.pos + l >super.count) l = super.count - super.pos;
       if(l < 0L) return 0L;
       super.pos += l;
       for(index += l; index >= length; index -= length);
       return l;
       }

    
    
    public synchronized int available()
       {
       if (flash.stopAllSoundStreams) return 0;
       else return super.count - super.pos;
       }

    
    
    public synchronized void reset()
    {
        index = 0;
        super.pos = 0;
    }

    
    
    public synchronized void stopAllStreams()
       {
       flash.stopAllSoundStreams = true;
       }
   }
