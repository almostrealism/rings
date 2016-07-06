/*
 * Copyright (C) 2004-06  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package com.almostrealism.flow;

import java.io.IOException;

/**
 * An implementation of the Proxy interface provides a method
 * for sending and recieving messages (objects) using IO streams
 * and an integer id. This allows for sharing of a socket connection
 * between a number of seperate clients with unique id numbers.
 * A Proxy implementation must keep a FIFO queue of recieved objects
 * and return them based on ID.
 * 
 * @author Mike Murray
 */
public interface Proxy {
    /**
     * Writes the specified object to the output stream using
     * the specified id.
     * 
     * @param o  Object to write.
     * @param id  Unique id of reciever.
     */
    public void writeObject(Object o, int id) throws IOException;
    
    /**
     * Returns the next object in the queue with the specified id.
     * 
     * @param id  Unique id of the reciever.
     * @return  The recieved object or null if one is not found.
     */
    public Object nextObject(int id);
}
