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

package io.almostrealism.matrix;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An arbitrary dimension tensor implemented as a recursive {@link LinkedList}.
 * 
 * @author  Michael Murray
 */
public class Tensor<T> {
	private LinkedList top;
	
	public Tensor() { top = new LinkedList(); }
	
	public synchronized void insert(T o, int... loc) {
		LinkedList l = top;
		
		for (int i = 0; i < loc.length - 1; i++) {
			l = get(l, loc[i]);
		}
		
		l.set(loc[loc.length - 1], new Leaf(o));
	}
	
	public Future<T> get(int... loc) {
		LinkedList l = top;
		
		for (int i = 0; i < loc.length - 1; i++) {
			l = get(l, loc[i]);
		}
		
		Object o = l.get(loc[loc.length - 1]);
		if (o == null) return null;
		return (Leaf) o;
	}
	
	public String getHTML() {
		return "<table>" + "</table>";
	}
	
	private static class Leaf<T> implements Future<T> {
		private T o;
		public Leaf(T o) { this.o = o; }
		public T get() { return o; }
		
		@Override
		public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			return get();
		}
		
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) { return false; }
		
		@Override
		public boolean isCancelled() { return false; }
		
		@Override
		public boolean isDone() { return true; }
	}
	
	private static LinkedList get(LinkedList l, int i) {
		Object o = l.get(i);
		
		if (o instanceof Leaf) {
			LinkedList newList = new LinkedList();
			newList.set(0, o);
			l.set(i, newList);
			return newList;
		}
		
		return (LinkedList) o;
	}
}
