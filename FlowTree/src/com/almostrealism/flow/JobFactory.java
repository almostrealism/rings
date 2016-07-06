/*
 * Copyright (C) 2004-06  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package com.almostrealism.flow;

/**
 * A JobFactory implementation produces a queue of jobs.
 * A JobFactory implementation must also be able to
 * construct a Job object given the String that was returned
 * by calling Job.encode.
 * 
 * @author Mike Murray
 */
public interface JobFactory {
	public long getTaskId();
	
    /**
     * @return  Next job in the queue.
     */
    public Job nextJob();
    
    /**
     * This should probably return net.sf.j3d.network.Server.instantiateJobClass(data)
     * to decode a Job object properly, but the stub is left open for other implementations
     * of the encoding.
     * 
     * @return  A Job object using the specified string parameter.
     */
    public Job createJob(String data);
    
    /**
     * Sets a property of this JobFactory object. Any JobFactory object that is to be
     * transmitted between network nodes will have this method called when it arrives at
     * a new host to initialize its variables based on the string returned by the encode
     * method.
     * 
     * @param key  Property name.
     * @param value  Property value.
     */
    public void set(String key, String value);
    
    /**
     * The encode method must return a string of the form:
     * "classname:key0=value0:key1=value1:key2=value2..."
     * Where classname is the name of the class that is implmenting JobFactory, and the
     * key=value pairs are pairs of keys and values that will be passed to the set method
     * of the class to initialize the state of the object after it has been transmitted
     * from one node to another.
     * 
     * @return  A String representation of this JobFactory object.
     */
    public String encode();
    
    /**
     * @return  A name for the task represented by this JobFactory.
     */
    public String getName();
    
    /**
     * @return  A value between 0.0 and 1.0 where 0.0 corresponds to 0% complete,
     *          and 1.0 to 100% complete.
     */
    public double getCompleteness();
    
    /**
     * @return  True if this JobFactory will not be producing any more jobs, false otherwise.
     */
    public boolean isComplete();
    
    /**
     * Sets the priority of this task.
     */
    public void setPriority(double p);
    
    /**
     * @return  The priority of this task.
     */
    public double getPriority();
}
