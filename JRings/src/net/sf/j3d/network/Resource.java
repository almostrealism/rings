/*
 * Copyright (C) 2006  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package net.sf.j3d.network;

import java.io.IOException;
import java.io.InputStream;

public interface Resource {
	public void load(Server.IOStreams io) throws IOException;
	public void loadFromURI() throws IOException;
	
	public void send(Server.IOStreams io) throws IOException;
	
	public void saveLocal(String file) throws IOException;
	
	public String getURI();
	public void setURI(String uri);
	
	public Object getData();
	public InputStream getInputStream();
}
