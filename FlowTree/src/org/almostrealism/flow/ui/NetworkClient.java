/*
 * Copyright 2016 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright (C) 2004-06  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package org.almostrealism.flow.ui;

import java.io.IOException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.NoSuchPaddingException;

import org.almostrealism.flow.Connection;
import org.almostrealism.flow.Job;
import org.almostrealism.flow.Node;
import org.almostrealism.flow.NodeProxy;
import org.almostrealism.flow.tests.TestJobFactory;
import org.ietf.jgss.GSSException;

/**
 * A NetworkClient instance can be used to send jobs/messages to a server.
 * 
 * @author Mike Murray
 */
public class NetworkClient {
  private Connection c;

    public static void main(String[] args) throws IOException, InterruptedException,
    											InvalidKeyException, NumberFormatException,
    											NoSuchAlgorithmException, InvalidKeySpecException,
    											NoSuchPaddingException,
    											InvalidAlgorithmParameterException, GSSException {
        TestJobFactory f = new TestJobFactory();
        NetworkClient client = new NetworkClient(args[0], Integer.parseInt(args[1]));
        
        while (true) {
            Thread.sleep(1000);
            client.sendJob(f.nextJob());
        }
    }
    
    /**
     * Constructs a new NetworkClient object using the specified host information
     * to connect to a server.
     * 
     * @param host  Hostname of server.
     * @param port  Remote port.
     * @throws IOException  If a connection cannot be established.
     * @throws GSSException 
     * @throws InvalidAlgorithmParameterException 
     * @throws NoSuchPaddingException 
     * @throws InvalidKeySpecException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     */
    public NetworkClient(String host, int port) throws IOException,
    												InvalidKeyException,
    												NoSuchAlgorithmException,
    												InvalidKeySpecException,
    												NoSuchPaddingException,
    												InvalidAlgorithmParameterException,
    												GSSException {
        Socket s = new Socket(host, port);
        NodeProxy p = new NodeProxy(s);
        this.c = new Connection(new Node(null, 0, 1, 1), p, -1);
        
        // if (!c.confirm()) throw new IOException("Error confirming connection");
    }
    
    /**
     * Sends the specified Job object to the server.
     * 
     * @param j  Job to encode and send.
     * @throws IOException  If an IO error occurs while sending.
     */
    public void sendJob(Job j) throws IOException {
        this.c.sendJob(j);
    }
}
