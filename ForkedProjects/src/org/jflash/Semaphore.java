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

final class Semaphore
{
   private boolean state;

    Semaphore(boolean flag)
    {
        state = flag;
    }

    synchronized boolean isSet()
    {
        return state;
    }

    synchronized void waitForTrue()
        throws InterruptedException
    {
        while(!state) 
            wait();
    }

    synchronized void waitForEvent()
        throws InterruptedException
    {
        while(!state) 
            wait();
        state = false;
    }

    synchronized void set()
    {
        if(!state)
            state = true;
        notifyAll();
    }

    synchronized boolean clear()
    {
        boolean flag = state;
        state = false;
        return flag;
    }

}
