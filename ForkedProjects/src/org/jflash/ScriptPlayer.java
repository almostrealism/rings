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

import java.io.*;
import java.util.*;


class FrameLabel
{
   int    frameNum;
   String label;
 
   FrameLabel(int frameNum,String label)
      {
      this.frameNum=frameNum;
      this.label=label;
      }
}



final class ScriptPlayer extends SParser implements Runnable
{
   static final boolean debug=true;
     
   static final int MAX_CHARACTER_NUM=64;  
  
   static final int TAG_SHOWFRAME         = 1;
   static final int TAG_DEFINESHAPE       = 2;
   static final int TAG_FREE              = 3;
   static final int TAG_PLACEOBJECT       = 4;
   static final int TAG_REMOVEOBJECT      = 5;
   static final int TAG_DEFINEBITS        = 6;  
   static final int TAG_DEFINEBUTTON      = 7;
   static final int TAG_JPEGTABLES        = 8;
   static final int TAG_SETBACKGROUNDCOLOR= 9;
   static final int TAG_DEFINEFONT        =10;
   static final int TAG_DEFINETEXT        =11;
   static final int TAG_DOACTION          =12;
   static final int TAG_DEFINEFONTINFO    =13;
   static final int TAG_DEFINESOUND       =14;
   static final int TAG_STARTSOUND        =15;
   static final int TAG_DEFINEBUTTONSOUND =17;
   static final int TAG_SOUNDSTREAMHEAD   =18;
   static final int TAG_SOUNDSTREAMBLOCK  =19;
   static final int TAG_DEFINEBITSJPEG2   =21;
   static final int TAG_DEFINESHAPE2      =22;
   static final int TAG_DEFINEBUTTONCXFORM=23;
   static final int TAG_PROTECT           =24;
   static final int TAG_PLACEOBJECT2      =26;
   static final int TAG_REMOVEOBJECT2     =28;
   static final int TAG_DEFINESHAPE3      =32;
   static final int TAG_DEFINETEXT2       =33;
   static final int TAG_DEFINEBUTTON2     =34;
   static final int TAG_FRAMELABEL        =43;
   static final int TAG_DEFINEMORPHSHAPE  =46;
   static final int TAG_DEFINEFONT2       =48;
   static final int TAG_DEFINEFONTINFO2   =62;
  
   static final int noErr = 0;
   static final int badHeaderErr = -1;
   static final int noMemErr = -2;
   static final int noScriptErr = -3;
   static final int playOK = 0;
   static final int playNeedData = 1;
   static final int playAtEnd = 2;
/*   static final int sactionHasLength = 128;
   static final int sactionNone = 0;
   static final int sactionGotoFrame = 129;
   static final int sactionGetURL = 131;
   static final int sactionNextFrame = 4;
   static final int sactionPrevFrame = 5;
   static final int sactionPlay = 6;
   static final int sactionStop = 7;
   static final int sactionToggleQuality = 8;
   static final int sactionStopSounds = 9;
   static final int sactionWaitForFrame = 138;*/
   static final int soundHasInPoint = 1;
   static final int soundHasOutPoint = 2;
   static final int soundHasLoops = 4;
   static final int soundHasEnvelope = 8;
   static final int syncNoMultiple = 1;
   static final int syncStop = 2;
   
   Vector frameLabels=new Vector();
   
   int nextPos;
   int startPos;
   int scriptErr;
   int len;
   int scriptLen;
   Rect frame;
   int frameRate;
   int frameDelay;
   int numFrames;
   int version;
   int headerLen;
   byte headerBuf[];
   boolean gotHeader;
   boolean atEnd;
   int curFrame;
   Semaphore gotData;
   DisplayList display;
   static final int maxActions = 16;
   public int actionList[];
   public int nActions;
   static final int charIndexSize = 64;
   static final int charIndexMask = 63;
   SCharacter charIndex[];
   FlashPane flash;
   Sound streamSound;
   int mixFormat;
   boolean mute;
   static final int START_MARKER = 216;
   static final int END_MARKER = 217;
   static final int TAG_MARKER = 255;
   private int jpegTablePos;
   private int jpegTableLen;
   private static int imageClassToUse;
/*    private static final int IMAGE_NOT_INITIALIZED = 0;
   private static final int IMAGE_TOOLKIT = 1;
   private static final int IMAGE_SUN = 2;
   private static final int IMAGE_NONE = 3;*/
   private int numFramesComplete;
   private int numFramesCompletePos;
   private InputStream scriptStream;
   private boolean scriptComplete;

   ScriptPlayer()
      {
      headerBuf = new byte[8];
      gotData = new Semaphore(false);
      actionList = new int[16];
      charIndex = new SCharacter[MAX_CHARACTER_NUM];
      streamSound = new Sound(0, 0, null, 0,flash);
      frame = new Rect(0, 0, 600, 600);
      ClearScript();
      }

   ScriptPlayer(FlashPane flash1)
      {
      this();
      flash = flash1;
      }

    public final boolean ScriptComplete()
    {
        return super.script != null && len >= scriptLen;
    }

    public final boolean GotHeader()
    {
        return gotHeader;
    }

    public final int GetFrame()
    {
        return curFrame;
    }

    public final boolean AtEnd()
    {
        return atEnd;
    }

    void PlayButtonSound(SObject sobject, int i)
    {
        int j = sobject.buttonState;
        if(j == i || i != 1 && j > i)
            return;
        if(sobject.character.soundPos > 0)
        {
            SParser sparser = new SParser();
            byte abyte0[] = super.script;
            int l = sobject.character.soundPos;
            sparser.script = abyte0;
            sparser.pos = l;
            for(int k = 1; k < i; k <<= 1)
            {
                l = sparser.GetWord();
                if(l > 0)
                    sparser.GetSoundInfo(null, null, true);
            }

            l = sparser.GetWord();
            if(l > 0)
            {
                SCharacter scharacter = findCharacter(l);
                if(scharacter == null || scharacter.type != 5)
                    return;
                int ai[] = new int[1];
                int ai1[] = new int[1];
                Sound sound = (Sound)scharacter.object;
                sparser.GetSoundInfo(ai, ai1, false);
                if(sound.ConvertToMulaw())
                {
                    if((ai1[0] & 2) != 0)
                    {
                        sound.stop();
                        return;
                    }
                    if((ai1[0] & 1) != 0 && !mute)
                    {
                        sound.playNoMultiple(ai[0]);
                        return;
                    }
                    if(!mute)
                        sound.playMultiple(ai[0]);
                }
            }
        }
    }

    void SoundStreamHead()
    {
        mixFormat = super.script[super.pos++] & 0xff;
        streamSound.SetFormat(super.script[super.pos++] & 0xff);
        streamSound.SetSamples(GetWord());
    }

    void SoundStreamBlock()
    {
    }

    void stopAllSounds()
    {
        for(Sound sound = streamSound; sound != null; sound = sound.next)
            sound.stop();

        streamSound.soundMixer.stopAllStreams();
    }

    void StartSound()
    {
        int i = GetWord();
        SCharacter scharacter = findCharacter(i);
        if(scharacter == null || scharacter.type != 5)
            return;
        int ai[] = new int[1];
        int ai1[] = new int[1];
        Sound sound = (Sound)scharacter.object;
        GetSoundInfo(ai, ai1, false);
        if(sound.ConvertToMulaw())
        {
            if((ai1[0] & 2) != 0)
            {
                sound.stop();
                return;
            }
            if((ai1[0] & 1) != 0 && !mute)
            {
                sound.playNoMultiple(ai[0]);
                return;
            }
            if(!mute)
                sound.playMultiple(ai[0]);
        }
    }

    public void FreeAll()
    {
        if(display != null)
            display.FreeAll();
        super.pos = startPos;
        curFrame = -1;
        atEnd = false;
    }

    public void ClearScript()
    {
        FreeAll();
        gotHeader = false;
        curFrame = -1;
        atEnd = false;
        len = headerLen = 0;
        scriptLen = -1;
        super.pos = 0;
        super.script = null;
        scriptErr = 0;
        nActions = 0;
    }

    
    
   SCharacter findCharacter(int i)
      {
      SCharacter scharacter;
      for(scharacter = charIndex[i & 0x3f]; scharacter != null && scharacter.tag != i; scharacter = scharacter.next);
      return scharacter;
      }

    
    
   SCharacter findCharacterAtDepth(int depth)
      {
      SCharacter scharacter;
      
      if (debug) System.out.println("findCharacterAtDepth("+depth+")");
      for (int i=0; i<MAX_CHARACTER_NUM; i++)
         {
         scharacter = charIndex[i];
//         if (debug) System.out.println("charIndex["+i+"]="+scharacter);
         while ((scharacter!= null) && (scharacter.depth!=depth)) 
            {
//            if (debug) System.out.println("   depth: "+scharacter.depth+"!="+depth);
            scharacter = scharacter.next;
            }
         if ((scharacter!=null) && (scharacter.depth==depth)) return scharacter;
         }
      return null;
      }

    
    
   void removeCharacter(int ID,int depth)
      {
      SCharacter scharacter,prevSChar;
      
      scharacter = charIndex[ID & 0x3F];
      if (scharacter==null) return;
      prevSChar=scharacter;
      if (scharacter.depth==depth) 
         {
         charIndex[ID & 0x3F]=null;
         return;
         }
      else scharacter=scharacter.next;
      
      while ((scharacter!= null) && (scharacter.depth!=depth)) scharacter = scharacter.next;
      if ((scharacter!=null) && (scharacter.depth==depth)) 
       prevSChar=scharacter.next;
      }

    
    
   private SCharacter createCharacter(int ID,int depth)
      {
      SCharacter scharacter = new SCharacter();
      if (scharacter != null)
         {
         scharacter.next = charIndex[ID & 0x3f];
         charIndex[ID & 0x3f] = scharacter;
         scharacter.player = this;
         scharacter.tag =ID;
         scharacter.depth=depth;
         }
      return scharacter;
      }

   
   
   private void freeCharacter(int i)
       {
       Object obj = null;
       SCharacter scharacter = charIndex[i & 0x3f];
       }

    
    
   private void defineShape()
      {
      SCharacter scharacter;
      int ID = GetWord();
      
      scharacter=findCharacter(ID);
      if (scharacter==null) scharacter = createCharacter(ID,-1);
      if (debug) System.out.println("defineShape() "+ID+", -1");     
      scharacter.type = 0;
      scharacter.bounds = GetRect();
      scharacter.dataPos = super.pos;
      return;
      }

   
   
   private void placeObject()
      {
      int ID=GetWord();
      int depth=GetWord();
      if (debug) System.out.println("placeObject() "+ID+", "+depth);     
      SCharacter scharacter=findCharacter(ID);
      scharacter.depth=depth;
      Matrix matrix = GetMatrix();
      if (super.pos < super.end)
         {
         ColorTransform colortransform = new ColorTransform(flash);
         GetColorTransform(colortransform);
         display.placeObject(scharacter,ID,depth,matrix, colortransform);
         return;
         } 
      else
         {
         display.placeObject(scharacter,ID,depth,matrix, null);
         return;
         }
      }

   
   private void placeObject2()
      {
      final int PF_MOVE          =0x01;
      final int PF_HAS_CHARACTER =0x02;
      final int PF_HAS_MATRIX    =0x04;
      final int PF_HAS_COLORTRANS=0x08;
    
      int            ID=-1,flags,depth;
      Matrix         matrix=null;
      SCharacter     scharacter=null;
      ColorTransform colortransform=null;
      
      flags=GetByte();
      depth=GetWord();
      if ((flags & PF_HAS_CHARACTER)==PF_HAS_CHARACTER) ID=GetWord();      
      if ((flags & PF_HAS_MATRIX)==PF_HAS_MATRIX) matrix=GetMatrix();
      if ((flags & PF_HAS_COLORTRANS)==PF_HAS_COLORTRANS)
         {
         colortransform = new ColorTransform(flash);
         GetColorTransform(colortransform);
         }
      if (debug) System.out.println("placeObject2() "+ID+", "+depth+", "+flags);     
      
      if ((flags & PF_MOVE|PF_HAS_CHARACTER)==(PF_MOVE|PF_HAS_CHARACTER))
         {
         removeCharacter(ID,depth);
         }
      if ((flags & /*PF_MOVE|*/PF_HAS_CHARACTER)==PF_HAS_CHARACTER)
         {
         scharacter = findCharacter(ID);         
         if (scharacter==null)
            {
            scharacter=createCharacter(ID,depth);
            scharacter.type=0;
            scharacter.dataPos = super.pos;
            scharacter.bounds=new Rect();
            }
         }
      else //if ((flags & PF_MOVE|PF_HAS_CHARACTER)==PF_MOVE)
         {
         scharacter = findCharacterAtDepth(depth);
         if (scharacter==null) 
            {
            System.err.println("Character at depth "+depth+" not found at ScriptPlayer.placeObject2()");
            return;
            }
         }
/*      else
         {
         System.err.println("Script error at ScriptPlayer.placeObject2(): PF_MOVE|PF_HAS_CHARACTER not set");
         return;
         }*/
      scharacter.depth=depth;
      display.placeObject(scharacter,ID,depth, matrix, colortransform);
      }

   
   
    private void removeObject()
       {
       int ID=GetWord();
       int depth=GetWord();
       display.removeObject(ID,depth);
       }

    
    
    private void removeObject2()
       {
       int depth=GetWord();
       display.removeObject2(depth);
       }

    
    
   private void defineButton()
      {
      int i = GetWord();
      if (findCharacter(i) != null) return;
      else
         {
         SCharacter scharacter = createCharacter(i,-1);
         scharacter.type = 2;
         scharacter.dataPos = super.pos;
         scharacter.bounds = new Rect();
         return;
         }
      }

   
   
   void defineButtonExtra(boolean flag)
      {
      int i = GetWord();
      SCharacter scharacter =findCharacter(i);
      if (scharacter == null || scharacter.type != 2) return;
      if (flag)
         {
         scharacter.soundPos = super.pos;
         return;
         } 
      else
         {
         scharacter.cxformPos = super.pos;
         return;
         }
      }

   
   
   void defineSound(int i)
      {
      int j = GetWord();
      if (findCharacter(j) != null) return;
      SCharacter scharacter = createCharacter(j,-1);
      if(scharacter == null) return;
      scharacter.type = 5;
      int k = super.script[super.pos++] & 0xff;
      int l = GetDWord();
      Sound sound;
      scharacter.object = sound = new Sound(k, l, super.script, super.pos,flash);
      if (sound == null)
         {
         freeCharacter(j);
         return;
         }
      scharacter.dataPos = super.pos;
      if(sound.CompressFormat() > 16)
         {
         freeCharacter(j);
         return;
         } 
      else
         {
         sound.characterTag = j;
         sound.next = streamSound.next;
         streamSound.next = sound;
         return;
         }
      }

   
   
   private void defineFont()
      {
      SCharacter scharacter;
      int ID = GetWord();
      scharacter=findCharacter(ID);
      if (scharacter==null) scharacter = createCharacter(ID,-1);
      if (debug) System.out.println("defineFont() "+ID+" ,-1");
      scharacter.type =SCharacter.SCHAR_TYPE_FONT;
      scharacter.bounds = new Rect();
      scharacter.dataPos = super.pos;
      return;
      }

   
   
   private void defineFontInfo()
      {
      int        ID,len,i,flags;
      SCharacter scharacter;
      
      ID = GetWord();
      scharacter=findCharacter(ID);
      if (scharacter==null) scharacter = createCharacter(ID,-1);
      if (debug) System.out.println("defineFontInfo() "+ID+" ,-1");
      len=GetByte();
      for (i=0; i<len; i++) GetByte(); // skip the font name         
      flags=GetByte();
      if ((flags & 2)==2) scharacter.fontBold=true;
      if ((flags & 4)==4) scharacter.fontItalic=true;
      }

   
   
   private void defineText()
      {
      SCharacter scharacter;

      int ID = GetWord();
      scharacter=findCharacter(ID);
      if (scharacter==null) scharacter = createCharacter(ID,-1);
      if (debug) System.out.println("defineText() "+ID+", -1");
      scharacter.type=SCharacter.SCHAR_TYPE_TEXT;
      scharacter.bounds = GetRect();
      scharacter.dataPos = super.pos;
      scharacter.matrix=GetMatrix();
      scharacter.glyphBits=GetByte();
      scharacter.advancedBits=GetByte();
      }

   
   
    private void ParseHeader()
    {
        if(scriptErr != 0)
            return;
        if(gotHeader)
            return;
        if(len < 21)
        {
            return;
        } else
        {
            frame = GetRect();
            frameRate = GetWord() << 8;
            frameDelay = 0x3e80000 / frameRate;
            numFrames = GetWord();
            numFramesCompletePos = startPos = super.pos;
            curFrame = -1;
            gotHeader = true;
            return;
        }
    }

    public void PushData(byte abyte0[], int i)
    {
        if(scriptErr != 0)
            return;
        int j = 0;
        if(scriptLen < 0)
        {
            int k = Math.min(8 - headerLen, i);
            System.arraycopy(abyte0, j, headerBuf, headerLen, k);
            j += k;
            i -= k;
            headerLen += k;
            if(headerLen == 8)
            {
                if(headerBuf[0] != 70 || headerBuf[1] != 87 || headerBuf[2] != 83)
                {
                    scriptErr = -1;
                    return;
                }
                version = headerBuf[3];
                scriptLen = headerBuf[4] & 0xff | (headerBuf[5] & 0xff) << 8 | (headerBuf[6] & 0xff) << 16 | (headerBuf[7] & 0xff) << 24;
                scriptLen -= 8;
                super.script = new byte[scriptLen];
                if(super.script == null)
                {
                    scriptErr = -2;
                    return;
                }
            } else
            {
                return;
            }
        }
        if(len + i > scriptLen)
            i = Math.min(i, scriptLen - len);
        System.arraycopy(abyte0, j, super.script, len, i);
        len += i;
        if(!gotHeader)
            ParseHeader();
        gotData.set();
    }

/*    private void defineJPEGTables(int i)
    {
        jpegTableLen = i - 2;
        jpegTablePos = super.pos;
    }*/

/*    private int ImageClassToUse()
    {
        if(imageClassToUse == 0)
            if(("SLAV" + System.getProperty("java.version")).indexOf("SLAV1.0") == -1)
            {
                imageClassToUse = 1;
            } else
            {
                imageClassToUse = 3;
                try
                {
                    Class.forName("sun.awt.image.ImageDecoder");
                    imageClassToUse = 2;
                }
                catch(ClassNotFoundException _ex) { }
            }
        return imageClassToUse;
    }*/
    
    
    

    private void defineBits(int i, int j)
    {
        int k = GetWord();
        int l = 0;
        int i1 = 0;
        int j1 = 0;
        int k1 = 0;
        if (findCharacter(k) != null)
            return;
        SCharacter scharacter = createCharacter(k,-1);
        if(scharacter == null)
            return;
        scharacter.type = 1;
        scharacter.dataPos = super.pos - 8;
        if(j == 6)
        {
            j1 = super.pos + 2;
            k1 = i - 4;
            l = jpegTablePos;
            i1 = jpegTableLen;
        } else
        {
            l = super.pos;
            for(int l1 = l; l1 < super.end; l1++)
            {
                if((super.script[l1] & 0xff) != 255 || (super.script[l1 + 1] & 0xff) != 217)
                    continue;
                i1 = l1 - l;
                break;
            }

            for(int i2 = l + i1; i2 < super.end; i2++)
            {
                if((super.script[i2] & 0xff) != 255 || (super.script[i2 + 1] & 0xff) != 216)
                    continue;
                j1 = i2 + 2;
                k1 = i - 6 - i1;
                break;
            }

        }
        byte abyte0[] = new byte[i1 + k1];
        if(abyte0 != null)
        {
            System.arraycopy(super.script, l, abyte0, 0, i1);
            System.arraycopy(super.script, j1, abyte0, i1, k1);
/*            switch(ImageClassToUse())
            {
            case 1: // '\001'*/
                scharacter.object = new Bitmap(flash.getToolkit().createImage(abyte0), display);
                return;
/*
            case 2: // '\002'
                try
                {
                    Object obj = Class.forName("ImageSource").newInstance();
                    if(obj != null)
                    {
                        ((ImageSource)obj).SetData(abyte0);
                        ((ImageSource)obj).CreateInputStream();
                        Bitmap bitmap = new Bitmap((ImageSource)obj, display);
                        ((ImageSource)obj).DestroyInputStream();
                        scharacter.object = bitmap;
                        return;
                    }
                }
                catch(Exception _ex)
                {
                    scharacter.object = null;
                    return;
                }
                break;

            case 3: // '\003'
            default:
                scharacter.object = null;
                return;
            }*/
        } else
        {
            scharacter.object = null;
        }
    }

    
   private int tagCtr=0;
   
   private int doTag()
      {
      if (super.script == null) return -3;
      if (scriptErr != 0) return scriptErr;
      if (atEnd) return 2;
      if (len - super.pos < 2) return 1;
      int i = super.pos;
      int j = GetWord();
      int k = j & 0x3f;
      if (k == 63)
         {
         if(len - super.pos < 4)
            {
            super.pos = i;
            return 1;
            }
         k = GetDWord();
         }
      nextPos = super.pos + k;
      super.end = nextPos;
      if(nextPos > len)
         {
         super.pos = i;
         return 1;
         }
      if ((debug) && ((j>>6)!=TAG_SOUNDSTREAMHEAD) && ((j>>6)!=TAG_SOUNDSTREAMBLOCK))
         {
//         if (tagCtr>30) System.exit(0);
         System.out.println(""+(j>>6));
         tagCtr++;
         }
      switch(j >> 6)
         {
         case TAG_SOUNDSTREAMHEAD:
         case TAG_SOUNDSTREAMBLOCK: 
            // ToDo: add full sound support
            break;
         default:
            System.err.println("Unknown tag "+(j>>6)+" at ScriptPlayer.doTag()");
            break;
         case TAG_PROTECT:
            // not important for a player
            break;
         case TAG_DEFINEFONTINFO:
         case TAG_DEFINEFONTINFO2:
            defineFontInfo();
            break;
         case 0:
            atEnd = true;
            break;
         case TAG_SHOWFRAME: 
            curFrame++;
            break;
         case TAG_DEFINESHAPE: 
         case TAG_DEFINESHAPE2:
         case TAG_DEFINESHAPE3:
            defineShape();
            break;
         case TAG_FREE:
            freeCharacter(GetWord());
            break;
         case TAG_PLACEOBJECT:
            placeObject();
            break;
         case TAG_PLACEOBJECT2:
            placeObject2();
            break;
         case TAG_REMOVEOBJECT:
            removeObject();
            break;
         case TAG_REMOVEOBJECT2:
            removeObject2();
            break;
         case TAG_DEFINEBITS:
         case TAG_DEFINEBITSJPEG2:
            defineBits(k, j >> 6);
            break;
         case TAG_JPEGTABLES:
            jpegTableLen = k - 2;
            jpegTablePos = super.pos;
            break;
         case TAG_DEFINESOUND:
            defineSound(k);
            break;
         case TAG_STARTSOUND:
            StartSound();
            break;
         case TAG_DEFINEBUTTONSOUND:
            defineButtonExtra(true);
            break;
         case TAG_DEFINEBUTTON:
         case TAG_DEFINEBUTTON2:
            defineButton();
            break;
         case TAG_DEFINEFONT:
         case TAG_DEFINEFONT2:
            defineFont();
            break;
         case TAG_DEFINETEXT:
         case TAG_DEFINETEXT2:
            defineText();
            break;
         case TAG_SETBACKGROUNDCOLOR:
            display.setBackgroundColor(GetColor(),3);
            break;
         case TAG_DOACTION:
            if (nActions < 16)
               {
               actionList[nActions] = super.pos;
               nActions++;
               }
             break;
         case TAG_DEFINEBUTTONCXFORM:
             defineButtonExtra(false);
             break;
         case TAG_FRAMELABEL:
             frameLabels.add(new FrameLabel(curFrame,GetString()));
             break; 
         }
      super.pos = nextPos;
      return scriptErr;
      }



   public int DrawFrame(int i)
      {
      if(scriptErr != 0)
            return scriptErr;
        if(!gotHeader)
            return 1;
        if(curFrame > i)
            FreeAll();
        int j;
        for(j = 0; curFrame < i && j == 0; j =doTag());
        return j;
    }

    boolean FrameComplete(int i)
    {
        if(numFramesComplete >= i)
            return true;
        if(scriptComplete)
            return true;
        if(super.script == null || scriptErr != -3)
            return false;
        SParser sparser = new SParser();
        byte abyte0[] = super.script;
        int k = numFramesCompletePos;
        sparser.script = abyte0;
        sparser.pos = k;
        do
        {
            int j;
            do
            {
                if(len - sparser.pos < 2)
                    return false;
                j = sparser.GetWord();
                int l = j & 0x3f;
                if(l == 63)
                {
                    if(len - super.pos < 4)
                        return false;
                    l = sparser.GetDWord();
                }
                sparser.pos += l;
                if(sparser.pos > len)
                    return false;
                numFramesCompletePos = sparser.pos;
            } while(j >> 6 != 1);
            numFramesComplete++;
        } while(numFramesComplete < i);
        return true;
    }

    public void loadScript(InputStream inputstream)
    {
        ClearScript();
        scriptStream = inputstream;
        Thread thread = new Thread(this);
        thread.setPriority(6);
        thread.start();
    }

   public void run()
      {
      try
         {
         byte abyte0[] = new byte[512];
         do
            {
            int i = scriptStream.read(abyte0);
            if(i < 0) break;
            PushData(abyte0, i);
            } 
         while(true);
         scriptStream.close();
         }
      catch(IOException _ex) { }
      scriptStream = null;
      scriptComplete = true;
      }

}


