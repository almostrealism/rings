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

package com.almostrealism.remote.mgr;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.almostrealism.util.KeyUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ManagerDatabase {
	private String key;
	private List<User> users;

	public ManagerDatabase() {
		key = KeyUtils.generateKey();
		users = new ArrayList<>();
	}

	public String getKey() { return key; }
	public void setKey(String key) { this.key = key; }

	public List<User> getUsers() { return users; }
	public void setUsers(List<User> users) { this.users = users; }

	public void addUser(String userId, String token) {
		users.add(new User(userId, token));
	}

	public void save(File destination) {
		try {
			new ObjectMapper().writeValue(destination, this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static ManagerDatabase load(File source) {
		try {
			return new ObjectMapper().readValue(source, ManagerDatabase.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
