/*
 * Copyright (C) 2006  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package com.almostrealism.flow;

import java.io.PrintStream;

public interface ServerBehavior {
	public void behave(Server s, PrintStream out);
}