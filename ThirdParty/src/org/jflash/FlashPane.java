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

import java.awt.*;
import java.io.*;
import java.net.*;
import java.awt.image.*;
import java.net.URL;
import javax.swing.*;

/**
 * This class provides the main entry point for the JFlash library,
 * it offers a lightweight-pane that can be added to any other gui
 * for showing the replayed animation
 */
public final class FlashPane extends JPanel implements Runnable
{
   /** replay quality flag: display the animation with lowest quality and with highest speed */
   public static final int FASTEST=1;
   /** replay quality flag: display the animation with highest quality and with possibly lower speed */
   public static final int NICEST=3;
   
   private boolean      loop,running=true;
   private int          scaleMode,quality;
   private Color        bgColor;
   boolean              allocateFullClug;
   private Image        image;
   private ScriptPlayer player;
   DisplayList          display;
   private Semaphore    playing;
   private Thread       control=null;
   boolean              stopAllSoundStreams;
   private int          actionDepth;
   private boolean      mouseIsHand;
   private String       path;
   private JApplet      applet=null;
    

   /**
    * Constructs a new FlashPane using some default settings: the animation is
    * replayed repeatedly in highest quality
    * @param path URL or path to the file that has to be replayed
    * @param applet the applet this FlasPane is embedded into or null otherwise;
    *        that parameter is important for the URL-Open-Tag of Flash files
    */
   public FlashPane(String path,JApplet applet)
      {
      this.path=path;
      this.applet=applet;
      loop = true;
      quality=NICEST;
      scaleMode = 0;
      bgColor =new Color(0xffffff);
      allocateFullClug = false;
      initFlash();
      }

   
   
   /**
    * Constructs a new FlashPane
    * @param path URL or path to the file that has to be replayed
    * @param applet the applet this FlasPane is embedded into or null otherwise;
    *        that parameter is important for the URL-Open-Tag of Flash files
    * @param loop replays the animation repeatedly if this value is set to true
    * @param quality specifies the replaying quality using the constants FASTEST
    *        or NICEST (here e.g. anti aliasing is influenced)
    */
   public FlashPane(String path,JApplet applet,boolean loop,int quality)
      {
      this.path=path;
      this.applet=applet;
      this.loop =loop;
      this.quality=quality;
      scaleMode=0;
      bgColor=new Color(0xffffff);
      allocateFullClug = false;
      initFlash();
      }
   


   private void initFlash()
      {
      loop = true;
      scaleMode = 0;
      allocateFullClug = false;
      stopAllSoundStreams = false;
      playing = new Semaphore(true);
      player = new ScriptPlayer(this);
      display = new DisplayList(this);
      player.display = display;
      image = null;
      try
         {
         try
            {
            URL url1 = new URL(path);
            InputStream inputstream = url1.openStream();
            if(inputstream != null) player.loadScript(inputstream);
            else throw new Exception("File not found!");
            }
         catch (MalformedURLException mue)
            {
            FileInputStream fis=new FileInputStream(path);
            if (fis!=null) player.loadScript(fis);
            else throw new Exception("File not found!");
            }
         }
      catch (Exception e)
         {
         e.printStackTrace();
         }
      Rectangle rectangle =this.getBounds();
      display.SetImage(rectangle.width, rectangle.height,getColorModel());
      }

    
    
    String getCodeBase()
       {
       int sepPos=path.lastIndexOf("/");
       if (sepPos>0) return path.substring(0,sepPos);
       return "/";
       }
    

   void setCamera(boolean flag)
       {
       display.setCamera(player.frame,quality, scaleMode, flag);
       }

    

   public boolean imageUpdate(Image image1, int i, int j, int k, int l, int i1)
      {
      return true;
      }

   
   
   void CauseUpdate(int i, int j, int k, int l)
      {
      repaint();
      }

   
   
   boolean updateImageToScreeen(boolean flag, int i, int j, int k, int l)
      {
      if (!flag)
         {
         repaint();
         return true;
         }
      Graphics g = getGraphics();
      if(g != null)
         {
         g.clipRect(i, j, k, l);
         g.drawImage(image, 0, 0, null);
         return true;
         } 
      else return false;
      }

   
   
    public void update(Graphics g)
    {
        Rectangle rectangle =this.getBounds();
        ScriptPlayer scriptplayer = player;
        if(scriptplayer.gotHeader)
            {
                if(image == null || display.UpdateImageSize(rectangle.width, rectangle.height))
                {
                    display.setCamera(player.frame,quality, scaleMode, false);
                    image = createImage(display);
                }
            }
        if(image != null)
        {
            g.drawImage(image, 0, 0, null);
            return;
        } else
        {
            g.setColor(new Color(bgColor.getRGB()));
            g.fillRect(0, 0, rectangle.width, rectangle.height);
            return;
        }
    }

    
    
    public void paint(Graphics g)
       {
       update(g);
       }
    


   public static Frame getFrame(Component component)
      {
      if(component instanceof Frame) return (Frame)component;
      for(java.awt.Container container = component.getParent(); container != null; container = container.getParent())
       if(container instanceof Frame) return (Frame)container;
      return null;
      }

   
   
    public void run()
    {
label0:
        do
            try
            {
                control.setPriority(4);
                while (!player.gotHeader || player.scriptErr != 0)
                    player.gotData.waitForEvent();
                {
                    display.setCamera(player.frame,quality, scaleMode, false);
                    if(image == null)
                        image = createImage(display);
                    Graphics g = getGraphics();
                    if(g != null)
                        g.drawImage(image, 0, 0, null);
                }
                if (player.curFrame <= 0)
                {
                    ScriptPlayer scriptplayer1;
                    for(; display.drawFrame(player, 0) != 0 && (((SParser) (scriptplayer1 = player)).script == null || scriptplayer1.len < scriptplayer1.scriptLen && true); player.gotData.waitForEvent())
                     display.update();
//                    display.update();
                    if(player.numFrames == 1) playing.clear();
                    doActions(0);
                    Thread.sleep(player.frameDelay);
                }
                control.setPriority(3);
                do
                {
                    playing.waitForTrue();
                    long l2 = System.currentTimeMillis();
                    long l1 = l2 +player.frameDelay;
                    {
label1:
                        {
                            do
                            {
                                int j = display.drawFrame(player,player.curFrame + 1);
                                if(j == 1)
                                {
                                    player.gotData.waitForEvent();
                                    continue;
                                }
                                if(j < 0)
                                {
                                    continue label0;
                                }
                                if(j != 2)
                                    break label1;
                                if(!loop)
                                    break;
                                ScriptPlayer scriptplayer3 = player;
                                if(scriptplayer3.curFrame == 0)
                                    break;
                                j = display.drawFrame(player, 0);
                            } while(true);
                            playing.clear();
                            continue label0;
                        }
                        if(player.nActions > 0)
                            doActions(0);
                    }
                display.update();    
                int i = (int)(l1 - System.currentTimeMillis());
                if(i > 0) Thread.sleep(i);
                } 
             while(running);
            }
            catch(Exception e)
               {
               e.printStackTrace();
               return;
               }
        while(running);
    }



   /**
    * Start replaying of the loaded Flash animation
    */
   public void startFlash()
      {
      control = new Thread(this);
      gotoFrame(0);
      playing.set();
      control.start();
      }



   /**
    * Stop replaying of the current Flash animation
    */
   public void stopFlash()
      {
      player.stopAllSounds();
      running=false;
      control = null;
      }



   private int doActions(int i)
      {
      actionDepth++;
      int j = -1;
      for (int k = 0; k < player.nActions; k++)
          {
          SParser sparser = new SParser();
          byte abyte0[] = ((SParser) (player)).script;
          int i1 = player.actionList[k];
          sparser.script = abyte0;
          sparser.pos = i1;
          int l = 0;
          do
             {
             int j1 = sparser.script[sparser.pos++] & 0xff;
             if(j1 == 0) break;
                int k1 = 0;
                if((j1 & 0x80) != 0)
                    k1 = sparser.GetWord();
                int l1 = sparser.pos + k1;
                if(l > 0)
                    l--;
                else
                    switch(j1)
                    {
                    default:
                        break;

                    case 131:
                        if (applet!=null) try
                           {
                           String s = sparser.GetString();
                           String s1 = sparser.GetString();
                           applet.getAppletContext().showDocument(new URL(applet.getDocumentBase(), s), s1);
                           }
                        catch(Exception e) 
                           { 
                           e.printStackTrace();
                           }
                        break;

                    case 129:
                        j = sparser.GetWord();
                        break;

                    case 6: // '\006'
                        i = 1;
                        break;

                    case 7: // '\007'
                        i = 2;
                        break;

                    case 9: // '\t'
                        player.stopAllSounds();
                        break;

                    case 138:
                        int i2 = sparser.GetWord();
                        if(!player.FrameComplete(i2))
                            l = sparser.script[sparser.pos++] & 0xff;
                        break;
                    }
                sparser.pos = l1;
            } while(true);
        }

        player.nActions = 0;
        if(j >= 0 && j != player.curFrame)
        {
            display.gotoFrame(player, j);
            if(actionDepth < 4)
                i = doActions(0);
            else
                player.nActions = 0;
        }
        if(i == 1)
            playing.set();
        else
        if(i == 2)
            playing.clear();
        actionDepth--;
        return i;
    }

   
   
    synchronized void gotoFrame(int i)
       {
       playing.clear();
       if (i !=player.curFrame)
           {
           display.gotoFrame(player, i);
           doActions(0);
           display.update();
          }
       }

    
    
   /**
    * Informs about the currently shown frame
    * @return the number of the actual frame
    */
   public int currentFlashFrame()
      {
      return player.curFrame;
      }

    
    
   public boolean mouseMove(Event event, int i, int j)
      {
      if (display!=null)
          {
          SObject sobject = display.hitButton(i, j);
          Frame frame = getFrame(this);
          if (frame != null) if(sobject != null)
             {
//                    frame.setCursor(12);
             mouseIsHand = true;
             } 
          else if(mouseIsHand)
             {
//                    frame.setCursor(0);
             mouseIsHand = false;
             }
          if(display.setButtonState(sobject, 2))
             {
             Thread.yield();
             display.update();
             }
          }
       return true;
       }

   
   
    public boolean mouseDown(Event event, int i, int j)
       {
       SObject sobject = display.hitButton(i, j);
       if(display.setButtonState(sobject, 4))
          {
          Thread.yield();
          display.update();
          }
       return true;
       }

    
    
    public boolean mouseDrag(Event event, int i, int j)
       {
       if(display.button != null)
          {
          SObject sobject = display.hitButton(i, j);
          if(display.setButtonState(display.button, sobject != display.button ? 2 : 4))
             {
             Thread.yield();
             display.update();
             }
         }
       return true;
       }

    
    
    public boolean mouseUp(Event event, int i, int j)
       {
       if(display.buttonState == 4 && display.button != null)
          {
          display.setButtonState(display.button, 1);
          Thread.yield();
          display.update();
          SCharacter scharacter = display.button.character;
          SParser sparser = new SParser();
          byte abyte0[] = ((SParser) (scharacter.player)).script;
          int i1 = scharacter.dataPos;
          sparser.script = abyte0;
          sparser.pos = i1;
          do
             {
                    int k = sparser.script[sparser.pos++] & 0xff;
                    if(k == 0)
                        break;
                    sparser.GetWord();
                    sparser.GetWord();
                    sparser.GetMatrix();
                } while(true);
                int l = 0;
                do
                {
                    int j1 = sparser.script[sparser.pos++] & 0xff;
                    if(j1 == 0)
                        break;
                    int k1 = 0;
                    if((j1 & 0x80) != 0)
                        k1 = sparser.GetWord();
                    int l1 = sparser.pos + k1;
                    if(l > 0)
                        l--;
                    else
                        switch(j1)
                        {
                        default:
                            break;

                        case 131:
                           if (applet!=null) try
                              {
                              String s = sparser.GetString();
                              String s1 = sparser.GetString();
                              applet.getAppletContext().showDocument(new URL(applet.getDocumentBase(), s), s1);
                              }
                            catch(Exception _ex) { }
                            break;

                        case 129:
                            int i2 = sparser.GetWord();
                            gotoFrame(i2);
                            break;

                        case 4: // '\004'
                            gotoFrame(player.curFrame + 1);
                            break;

                        case 5: // '\005'
                            gotoFrame(player.curFrame - 1);
                            break;

                        case 8: // '\b'
                            if (quality==FASTEST) quality=NICEST;
                            else if (quality==NICEST) quality=FASTEST;
                            display.setCamera(player.frame,quality, scaleMode, true);
                            break;

                        case 6: // '\006'
                            playing.set();
                            break;

                        case 7: // '\007'
                            playing.clear();
                            break;

                        case 9: // '\t'
                            player.stopAllSounds();
                            break;

                        case 138:
                            int j2 = sparser.GetWord();
                            if(!player.FrameComplete(j2))
                                l = sparser.script[sparser.pos++] & 0xff;
                            break;
                        }
                    sparser.pos = l1;
              } 
          while(true);
          }
       return true;
       }

    
    
    public boolean mouseExit(Event event, int i, int j)
       {
       if(display.setButtonState(null, 0))
          {
          Thread.yield();
          display.update();
          }
       return true;
       }

}
