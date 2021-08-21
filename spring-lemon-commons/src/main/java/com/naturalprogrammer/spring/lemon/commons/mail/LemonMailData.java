/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this artifact or file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.naturalprogrammer.spring.lemon.commons.mail;

import lombok.Getter;
import lombok.Setter;

/**
 * Data needed for sending a mail.
 * Override this if you need more data to be sent.
 */
@Getter @Setter
public class LemonMailData {
	
	private String to;
	private String subject;
	private String body;

	public static LemonMailData of(String to, String subject, String body) {
		
		LemonMailData data = new LemonMailData();
		
		data.to = to;
		data.subject = subject;
		data.body = body;

		return data;
	}
}
