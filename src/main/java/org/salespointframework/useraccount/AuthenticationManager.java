/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.salespointframework.useraccount;

/**
 * Application component for authentication related use cases.
 * 
 * @author Oliver Gierke
 */
public interface AuthenticationManager {

	/**
	 * Returns the {@link UserAccount} of the currently logged in user or {@literal null} if no-one is currently logged
	 * in.
	 * 
	 * @return
	 */
	UserAccount getCurrentUser();
	
	/**
	 * Returns whether the given candidate {@link Password} matches the given existing one.
	 * 
	 * @param candidate can be {@literal null}.
	 * @param existing must not be {@literal null}.
	 * @return
	 */
	boolean matches(Password candidate, Password existing);
}