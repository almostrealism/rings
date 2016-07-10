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

//import java.applet.AudioClip;
import java.io.*;
import sun.audio.AudioPlayer;

final class Sound //implements AudioClip
{
static final int sndMono = 0;
static final int sndStereo = 1;
static final int snd8Bit = 0;
static final int snd16Bit = 2;
static final int snd5K = 0;
static final int snd11K = 4;
static final int intsnd22K = 8;
static final int intsnd44K = 12;
static final int sndCompressNone = 0;
static final int sndCompressADPCM = 16;
static final int sndRateMask = 12;
static final int sndCompressMask = 240;
private int format;
private int srcSamples;
private int dstSamples;
private byte srcData[];
private int srcDataStart;
private int srcDataPos;
private boolean stereo;
private boolean dataIsValid;
private byte mulawData[];
Sound next;
int characterTag;
static final int sndRate5K_2X = 11025;
static final int sndRate8K_2X = 16000;
static final int sndRate11K_2X = 22050;
static final int sndRate22K_2X = 44100;
static final int sndRate44K_2X = 0x15888;
static final int kRateTable[] = {11025, 22050, 44100, 0x15888};
static final int kRateShiftTable[] = {3, 2, 1, 0};
SoundMixer soundMixer;
/*private static final int kNextTag = 0x2e736e64;
private static final int kSndFormatMulaw8 = 1;
private static final int kHeaderSize = 0;
private static final int kMuLawZero = 2;
private static final int kMuLawBias = 132;*/
private static byte mulawExpTable[];
int bitBuf;
int bitPos;
int nBits;
int nSamples;
int valpred[];
int index[];
static final int indexTable2[] = {-1, 2};
static final int indexTable3[] = {-1, -1, 2, 4};
static final int indexTable4[] = {-1, -1, -1, -1, 2, 4, 6, 8};
static final int indexTable5[] = {-1, -1, -1, -1, -1, -1, -1, -1, 1, 2,4, 6, 8, 10, 13, 16};
static final int indexTables[][] = {indexTable2, indexTable3, indexTable4, indexTable5};
static final int stepsizeTable[] = 
   {
   7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 
   19, 21, 23, 25, 28, 31, 34, 37, 41, 45, 
   50, 55, 60, 66, 73, 80, 88, 97, 107, 118, 
   130, 143, 157, 173, 190, 209, 230, 253, 279, 307, 
   337, 371, 408, 449, 494, 544, 598, 658, 724, 796, 
   876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066, 
   2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358, 
   5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899, 
   15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767
   };
   private FlashPane flash;
   

   
   Sound(int i, int j, byte abyte0[], int k,FlashPane flash)
      {
      this.flash=flash;
      valpred = new int[2];
      index = new int[2];
      format = i;
      srcSamples = j;
      srcData = abyte0;
      srcDataStart = srcDataPos = k;
      stereo = (format & 1) != 0;
      CreateMulawTable();
      switch(kRateTable[format >> 2 & 3])
         {
         case 11025: 
            dstSamples = (int)((j * 16000L + 11024L) / 11025L);
            return;
         case 22050: 
            dstSamples = (int)((j * 16000L + 22049L) / 22050L);
            return;
         case 44100: 
            dstSamples = (int)((j * 16000L + 44099L) / 44100L);
            return;
         case 88200: 
            dstSamples = (int)((j * 16000L + 0x15887L) / 0x15888L);
            return;
         }
      }

   
    int Rate2X()
    {
        return kRateTable[format >> 2 & 3];
    }

    int RateShift()
    {
        return kRateShiftTable[format >> 2 & 3];
    }

    boolean Stereo()
    {
        return (format & 1) != 0;
    }

    int NChannels()
    {
        return (format & 1) == 0 ? 1 : 2;
    }

    boolean Is8Bit()
    {
        return (format & 2) == 0;
    }

    int BitsPerSample()
    {
        return (format & 2) == 0 ? 8 : 16;
    }

    int BytesPerSample()
    {
        return (format & 2) == 0 ? 1 : 2;
    }

    int CompressFormat()
    {
        return format & 0xf0;
    }

    boolean Compressed()
    {
        return (format & 0xf0) != 0;
    }

    

    void SetFormat(int i)
    {
        format = i;
        stereo = (format & 1) != 0;
    }

    void SetSamples(int i)
    {
        srcSamples = i;
        switch(kRateTable[format >> 2 & 3])
        {
        case 11025: 
            dstSamples = (int)((i * 16000L + 11024L) / 11025L);
            return;

        case 22050: 
            dstSamples = (int)((i * 16000L + 22049L) / 22050L);
            return;

        case 44100: 
            dstSamples = (int)((i * 16000L + 44099L) / 44100L);
            return;

        case 88200: 
            dstSamples = (int)((i * 16000L + 0x15887L) / 0x15888L);
            return;
        }
    }

    public synchronized void play(int i)
    {
        stop();
        if(mulawData != null)
        {
            soundMixer = new SoundMixer(mulawData, i,flash);
            AudioPlayer.player.start(soundMixer);
        }
    }

    public synchronized void play()
    {
        play(1);
    }

    synchronized void playMultiple(int i)
    {
        if(mulawData != null)
        {
            soundMixer = new SoundMixer(mulawData, i,flash);
            AudioPlayer.player.start(soundMixer);
        }
    }

    synchronized void playMultiple()
    {
        playMultiple(1);
    }

    synchronized void playNoMultiple(int i)
    {
        if(soundMixer != null && soundMixer.available() > 0)
            return;
        stop();
        if(mulawData != null)
        {
            soundMixer = new SoundMixer(mulawData, i,flash);
            AudioPlayer.player.start(soundMixer);
        }
    }

    synchronized void playNoMultiple()
    {
        playMultiple(1);
    }

    public synchronized void loop()
    {
        stop();
        if(mulawData != null)
        {
            soundMixer = new SoundMixer(mulawData, -1,flash);
            AudioPlayer.player.start(soundMixer);
        }
    }

    public synchronized void stop()
    {
        if(soundMixer != null)
        {
            AudioPlayer.player.stop(soundMixer);
            try
            {
                soundMixer.close();
            }
            catch(IOException _ex) { }
            soundMixer = null;
        }
    }

    synchronized boolean ConvertToMulaw()
    {
        if(dataIsValid)
            return true;
        if((mulawData = DecompressFromADPCMAndResample()) != null)
        {
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(0);
            DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
            try
            {
                dataoutputstream.writeInt(0x2e736e64);
                dataoutputstream.writeInt(0);
                dataoutputstream.writeInt(dstSamples);
                dataoutputstream.writeInt(1);
                dataoutputstream.writeInt(8000);
                dataoutputstream.writeInt(1);
                dataoutputstream.writeInt(0);
            }
            catch(IOException _ex)
            {
                return false;
            }
            byte abyte0[] = bytearrayoutputstream.toByteArray();
            for(int i = 0; i < 0; i++)
                mulawData[i] = abyte0[i];

            dataIsValid = true;
            return true;
        } else
        {
            return false;
        }
    }

    void CreateMulawTable()
    {
        if(mulawExpTable == null)
        {
            mulawExpTable = new byte[256];
            for(int i = 0; i < 8; i++)
            {
                int j = 1 << i;
                int k = j;
                for(; j > 0; j--)
                {
                    mulawExpTable[k] = (byte)i;
                    k++;
                }

            }

            mulawExpTable[0] = 0;
        }
    }

    private static byte Convert16BitToMulaw(int i)
    {
        char c;
        if(i < 0)
        {
            c = '\200';
            i = -i;
        } else
        {
            c = '\0';
        }
        int j = i + 132;
        if(j > 32767)
            j = 32767;
        byte byte0 = mulawExpTable[j >> 7];
        int k = j >> byte0 + 3 & 0xf;
        int l = ~(c | byte0 << 4 | k);
        if(l == 0)
            l = 2;
        return (byte)l;
    }

    private byte[] DecompressFromADPCMAndResample()
    {
        int i = 0;
        int k = 0;
        int l = 0;
        int i1 = dstSamples;
        switch(kRateTable[format >> 2 & 3])
        {
        case 11025: 
            i = 45158;
            break;

        case 22050: 
            i = 0x160cc;
            break;

        case 44100: 
            i = 0x2c199;
            break;

        case 88200: 
            i = 0x58333;
            break;
        }
        srcDataPos = srcDataStart;
        byte abyte0[];
        if((abyte0 = new byte[2048]) == null)
            return null;
        Skip(0);
        byte abyte1[];
        if((abyte1 = new byte[dstSamples]) == null)
            return null;
        for(int j1 = srcSamples; j1 > 0; j1 -= 2048)
        {
            int j;
            if(j1 > 2048)
            {
                Decompress(abyte0, 2048);
                j = 0x8000000;
            } else
            {
                Decompress(abyte0, j1);
                j = j1 - 1 << 16;
            }
            for(; k < j && l < i1; k += i)
                abyte1[l++] = abyte0[k >> 16];

            if(j1 <= 2048)
                abyte1[dstSamples - 1] = abyte0[j1 - 1];
            else
                k -= 0x8000000;
        }

        return abyte1;
    }

    private void FillBuffer()
    {
        for(; bitPos <= 24; bitPos += 8)
            bitBuf = bitBuf << 8 | 0xff & srcData[srcDataPos++];

    }

    private int GetBits(int i)
    {
        if(bitPos < i)
            FillBuffer();
        int j = (bitBuf << 32 - bitPos) >>> 32 - i;
        bitPos -= i;
        return j;
    }

    private int GetSBits(int i)
    {
        if(bitPos < i)
            FillBuffer();
        int j = (bitBuf << 32 - bitPos) >> 32 - i;
        bitPos -= i;
        return j;
    }

    private void SkipBits(int i)
    {
        if(i <= 32)
        {
            int j;
            for(; i > 0; i -= j)
            {
                j = Math.min(16, i);
                GetBits(j);
            }

            return;
        } else
        {
            i -= bitPos;
            bitPos = 0;
            int k = i / 8;
            srcDataPos += k;
            GetBits(i & 7);
            return;
        }
    }

    private void Skip(int i)
    {
        if(nBits == 0)
            nBits = GetBits(2) + 2;
        int j = nSamples & 0xfffff000;
        if(j > 0 && i > j + nSamples)
        {
            nSamples += j;
            i -= j;
            int k = j * nBits;
            if(stereo)
                k *= 2;
            SkipBits(k);
        }
        int l = i >> 12;
        int i1 = l * (22 + nBits * 4095);
        if(stereo)
            i1 *= 2;
        SkipBits(i1);
        i &= 0xfff;
        byte abyte0[] = new byte[2048];
        char c = stereo ? '\u0200' : '\u0400';
        int j1;
        for(; i > 0; i -= j1)
        {
            j1 = Math.min(c, i);
            Decompress(abyte0, j1 + j1);
        }

    }

    private void Decompress(byte abyte0[], int i)
    {
        int j = 0;
        if(nBits == 0)
            nBits = GetBits(2) + 2;
        int ai[] = indexTables[nBits - 2];
        int k = 1 << nBits - 2;
        int l = 1 << nBits - 1;
        if(!stereo)
        {
            int i1 = valpred[0];
            int k1 = index[0];
            int j2 = nSamples;
            while(i-- > 0) 
                if((++j2 & 0xfff) == 1)
                {
                    i1 = GetSBits(16);
                    abyte0[j++] = Convert16BitToMulaw(i1);
                    k1 = GetBits(6);
                } else
                {
                    int l2 = GetBits(nBits);
                    int j3 = stepsizeTable[k1];
                    int l3 = 0;
                    int j4 = k;
                    do
                    {
                        if((l2 & j4) != 0)
                            l3 += j3;
                        j3 >>= 1;
                        j4 >>= 1;
                    } while(j4 != 0);
                    l3 += j3;
                    if((l2 & l) != 0)
                        i1 -= l3;
                    else
                        i1 += l3;
                    k1 += ai[l2 & ~l];
                    if(k1 < 0)
                        k1 = 0;
                    else
                    if(k1 > 88)
                        k1 = 88;
                    if(i1 != (short)i1)
                        i1 = i1 >= 0 ? 32767 : -32768;
                    abyte0[j++] = Convert16BitToMulaw(i1);
                }
            valpred[0] = i1;
            index[0] = k1;
            nSamples = j2;
            return;
        }
        int j1 = 0;
        while(i-- > 0) 
        {
            nSamples++;
            if((nSamples & 0xfff) == 1)
            {
                for(int l1 = 0; l1 < 2; l1++)
                {
                    valpred[l1] = GetSBits(16);
                    if(l1 == 0)
                    {
                        j1 = valpred[l1];
                    } else
                    {
                        j1 += valpred[l1];
                        j1 >>= 1;
                        abyte0[j++] = Convert16BitToMulaw(j1);
                    }
                    index[l1] = GetBits(6);
                }

            } else
            {
                for(int i2 = 0; i2 < 2; i2++)
                {
                    int k2 = GetBits(nBits);
                    int i3 = stepsizeTable[index[i2]];
                    int k3 = 0;
                    int i4 = k;
                    do
                    {
                        if((k2 & i4) != 0)
                            k3 += i3;
                        i3 >>= 1;
                        i4 >>= 1;
                    } while(i4 != 0);
                    k3 += i3;
                    if((k2 & l) != 0)
                        valpred[i2] -= k3;
                    else
                        valpred[i2] += k3;
                    index[i2] += ai[k2 & ~l];
                    if(index[i2] < 0)
                        index[i2] = 0;
                    else
                    if(index[i2] > 88)
                        index[i2] = 88;
                    if(valpred[i2] != (short)valpred[i2])
                        valpred[i2] = valpred[i2] >= 0 ? 32767 : -32768;
                    if(i2 == 0)
                    {
                        j1 = valpred[i2];
                    } else
                    {
                        j1 += valpred[i2];
                        j1 >>= 1;
                        abyte0[j++] = Convert16BitToMulaw(j1);
                    }
                }

            }
        }
    }


}
