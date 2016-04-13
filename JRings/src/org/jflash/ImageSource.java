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
import java.net.MalformedURLException;
import sun.awt.image.*;

class ImageSource extends URLImageSource
{
   ByteArrayInputStream is;
   byte                 data[];

   ImageSource(FlashPane flash)
   throws MalformedURLException
      {
      super(flash.getCodeBase());
      }

   void SetData(byte abyte0[])
       {
       data = abyte0;
       }

    void CreateInputStream()
    {
        is = new ByteArrayInputStream(data);
    }

    void DestroyInputStream()
    {
        try
        {
            if(is != null)
            {
                is.close();
                is = null;
                return;
            }
        }
        catch(IOException _ex) { }
    }

    protected ImageDecoder getDecoder()
    {
        return new JPEGImageDecoder(this, is);
    }

}
