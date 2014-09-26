/*
 * Copyright (C) 2012,2013 tamtam180
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.orz.arangodb.http;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Iterator;

import org.apache.http.entity.AbstractHttpEntity;

import com.google.gson.Gson;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class JsonSequenceEntity extends AbstractHttpEntity {
	
	private Iterator<?> it;
	private Gson gson;

	public JsonSequenceEntity(Iterator<?> it, Gson gson) {
		this.it = it;
		this.gson = gson;
		setChunked(true);
		setContentType("binary/octet-stream");
	}
	
	public boolean isRepeatable() {
		return false;
	}

	public long getContentLength() {
		return -1;
	}

	public InputStream getContent() throws IOException, IllegalStateException {
		throw new IllegalStateException("cannot support this method.");
	}

	public boolean isStreaming() {
		return true;
	}

	public void writeTo(OutputStream outstream) throws IOException {
		
		if (outstream == null) {
			throw new IllegalArgumentException("Output stream may not be null");
		}
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(outstream, "UTF-8"));
			while (it.hasNext()) {
				Object value = it.next();
				gson.toJson(value, writer);
				writer.newLine();
			}
			writer.flush();
		} finally {
		}
		
	}

}
