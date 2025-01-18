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

package io.almostrealism.tree.ui;

import javax.swing.tree.TreeNode;

public interface WebTreeNode<T> extends TreeNode, Iterable<T> {
	/**
	 * Return the name of a field which will contain children
	 * when this object is mapped to JSON or XML. This prevents
	 * cluttering the exported data with duplicate data for
	 * children when children are already being mapped to a
	 * JSON/XML field.
	 */
	public String getChildrenFieldName();
	
	public String getHREF();
}
