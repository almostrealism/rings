/*
 * Copyright 2024 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.almostrealism.remote.event;

public class DefaultEvent extends AbstractEvent {
	private long time;
	private long duration;

	public DefaultEvent() { }

	public DefaultEvent(String name, long duration) {
		super(name);
		setDuration(duration);
		setTime(System.currentTimeMillis());
	}

	public long getTime() { return time; }
	public void setTime(long time) { this.time = time; }

	public long getDuration() { return duration; }
	public void setDuration(long duration) { this.duration = duration; }
}
