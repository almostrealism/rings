/*
 * Copyright 2023 Michael Murray
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

package com.almostrealism.remote;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class RemoteAccessKey {
	private String host;
	private int port;

	private String userId;
	private String token;
	private String key;

	public RemoteAccessKey() { }

	public RemoteAccessKey(String host, int port, String userId, String token, String key) {
		this.host = host;
		this.port = port;
		this.userId = userId;
		this.token = token;
		this.key = key;
	}

	public String getHost() { return host;}
	public void setHost(String host) { this.host = host; }

	public int getPort() { return port; }
	public void setPort(int port) { this.port = port; }

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public static RemoteAccessKey load(String key) {
		try {
			return new ObjectMapper().readValue(new File(key), RemoteAccessKey.class);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
