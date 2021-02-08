/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.mapping.annotations;

import com.arangodb.entity.DocumentField;
import com.arangodb.mapping.ArangoJack;
import com.arangodb.velocypack.VPackSlice;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Michele Rastelli
 */
public class ArangoAnnotationsTest {

	private final ArangoJack mapper = new ArangoJack();

	@Test
	public void documentField() {
		DocumentFieldEntity e = new DocumentFieldEntity();
		e.setId("Id");
		e.setKey("Key");
		e.setRev("Rev");
		e.setFrom("From");
		e.setTo("To");

		VPackSlice slice = mapper.serialize(e);
		System.out.println(slice);
		Map<String, String> deserialized = mapper.deserialize(slice, Object.class);
		assertThat(deserialized.get(DocumentField.Type.ID.getSerializeName()), is(e.getId()));
		assertThat(deserialized.get(DocumentField.Type.KEY.getSerializeName()), is(e.getKey()));
		assertThat(deserialized.get(DocumentField.Type.REV.getSerializeName()), is(e.getRev()));
		assertThat(deserialized.get(DocumentField.Type.FROM.getSerializeName()), is(e.getFrom()));
		assertThat(deserialized.get(DocumentField.Type.TO.getSerializeName()), is(e.getTo()));
		assertThat(deserialized.size(), is(DocumentField.Type.values().length));

		DocumentFieldEntity deserializedEntity = mapper.deserialize(slice, DocumentFieldEntity.class);
		assertThat(deserializedEntity, is(e));
	}

	@Test
	public void serializedName() {
		SerializedNameEntity e = new SerializedNameEntity();
		e.setA("A");
		e.setB("B");
		e.setC("C");

		VPackSlice slice = mapper.serialize(e);
		System.out.println(slice);
		Map<String, String> deserialized = mapper.deserialize(slice, Object.class);
		assertThat(deserialized.get(SerializedNameEntity.SERIALIZED_NAME_A), is(e.getA()));
		assertThat(deserialized.get(SerializedNameEntity.SERIALIZED_NAME_B), is(e.getB()));
		assertThat(deserialized.get(SerializedNameEntity.SERIALIZED_NAME_C), is(e.getC()));
		assertThat(deserialized.size(), is(3));

		SerializedNameEntity deserializedEntity = mapper.deserialize(slice, SerializedNameEntity.class);
		assertThat(deserializedEntity, is(e));
	}

	@Test
	public void serializedNameParameter() {
		Map<String, String> e = new HashMap<>();
		e.put(SerializedNameParameterEntity.SERIALIZED_NAME_A, "A");
		e.put(SerializedNameParameterEntity.SERIALIZED_NAME_B, "B");
		e.put(SerializedNameParameterEntity.SERIALIZED_NAME_C, "C");

		VPackSlice slice = mapper.serialize(e);
		SerializedNameParameterEntity deserializedEntity = mapper
				.deserialize(slice, SerializedNameParameterEntity.class);
		assertThat(deserializedEntity, is(new SerializedNameParameterEntity("A", "B", "C")));
	}

	@Test
	public void expose() {
		ExposeEntity e = new ExposeEntity();
		e.setReadWrite("readWrite");
		e.setReadOnly("readOnly");
		e.setWriteOnly("writeOnly");
		e.setIgnored("ignored");

		VPackSlice serializedEntity = mapper.serialize(e);
		Map<String, String> deserializedEntity = mapper.deserialize(serializedEntity, Object.class);
		assertThat(deserializedEntity.get("readWrite"), is("readWrite"));
		assertThat(deserializedEntity.get("readOnly"), is("readOnly"));
		assertThat(deserializedEntity.size(), is(2));

		Map<String, String> map = new HashMap<>();
		map.put("readWrite", "readWrite");
		map.put("readOnly", "readOnly");
		map.put("writeOnly", "writeOnly");
		map.put("ignored", "ignored");

		VPackSlice serializedMap = mapper.serialize(map);
		ExposeEntity deserializedMap = mapper.deserialize(serializedMap, ExposeEntity.class);
		assertThat(deserializedMap.getIgnored(), is(nullValue()));
		assertThat(deserializedMap.getReadOnly(), is(nullValue()));
		assertThat(deserializedMap.getWriteOnly(), is("writeOnly"));
		assertThat(deserializedMap.getReadWrite(), is("readWrite"));
	}

}
