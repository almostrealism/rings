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

import com.almostrealism.remote.AccessManager;

import java.util.Objects;

public class DefaultAccessManager implements AccessManager {
	private ManagerDatabase db;

	public DefaultAccessManager(ManagerDatabase db) {
		this.db = db;
	}

	@Override
	public boolean authorize(String userId, String token, String key, String requestId) {
		if (!Objects.equals(db.getKey(), key)) return false;
		return db.getUsers().stream()
				.filter(u -> Objects.equals(u.getId(), userId))
				.filter(u -> Objects.equals(u.getToken(), token)).
				findFirst()
				.isPresent();
	}
}
