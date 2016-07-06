/*
 * Copyright (C) 2006  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package com.almostrealism.flow.behavior;

import java.io.IOException;
import java.io.PrintStream;

import com.almostrealism.flow.Server;
import com.almostrealism.flow.ServerBehavior;

import net.sf.j3d.run.Settings;

public class RandomPeerJoin implements ServerBehavior {
	public void behave(Server s, PrintStream out) {
		try {
			int i = Settings.random.nextInt(s.getPeers().length);
			String peers[] = s.getPeerList(i);
			out.println("RandomPeerJoin: Got peer list for server " + i +
						" (" + peers.length + " peers).");
			if (peers.length <= 0) return;
			
			int j = Settings.random.nextInt(s.getPeers().length);
			out.println("RandomPeerJoin: Attempting to open " + peers[j]);
			
			if (!s.open(peers[j])) {
				out.println("RandomPeerJoin: Unable to open " + peers[j]);
			} else {
				s.close(i);
			}
		} catch (IOException e) {
			System.out.println("RandomPeerJoin: " + e);
		}
	}
}
