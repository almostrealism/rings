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

package com.almostrealism.photonfield.xml;

import java.awt.Container;

import javax.swing.JDesktopPane;
import javax.swing.JScrollPane;

public class LayeredNodeDisplay extends JDesktopPane implements NodeDisplay {
	private Node node;
	private int w = 8, h = 10;
	public LayeredNodeDisplay(Node n) { this.node = n; }
	public Container getContainer() { return new JScrollPane(this); }
	public Container getFrame() { return null; }
	public void setGridHeight(int h) { this.h = h; }
	public void setGridWidth(int w) { this.w = w; }
	public int getGridHeight() { return this.h; }
	public int getGridWidth() { return this.w; }
	public Node getNode() { return this.node; }
}
