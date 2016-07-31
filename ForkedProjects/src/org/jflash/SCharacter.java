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

final class SCharacter
{
   static final int upState = 1;
   static final int overState = 2;
   static final int downState = 4;
   static final int hitTestState = 8;
   static final int shapeChar = 0;
   static final int SCHAR_TYPE_BITS = 1;
   static final int SCHAR_TYPE_BUTTON = 2;
   static final int SCHAR_TYPE_FONT = 3;
   static final int SCHAR_TYPE_TEXT = 4;
   static final int SCHAR_TYPE_SOUND = 5;
   
   SCharacter   next;
   ScriptPlayer player;
   int          tag,type,dataPos,depth,glyphBits,advancedBits;
   Rect         bounds;
   int          soundPos,cxformPos;
   Object       object;
   Matrix       matrix;
   boolean      fontItalic=false,fontBold=false;

   SCharacter()
      {
      }

}

