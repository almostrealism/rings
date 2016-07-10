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

package com.almostrealism.apple;

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.foundation.NSNetService;

public class AppleSettings {
	public static NSApplication application;
	
	public static int netServicePort = 6769;
	public static NSNetService netService;
	
	public static void initNetService() {
		if (AppleSettings.application == null)
			AppleSettings.application = new NSApplication();
		
		netService = new NSNetService("", "_rings._tcp", String.valueOf(netServicePort));
	}
}
