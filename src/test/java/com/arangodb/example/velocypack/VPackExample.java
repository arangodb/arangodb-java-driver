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

package com.arangodb.example.velocypack;

import com.arangodb.velocypack.VPackBuilder;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.ValueType;
import com.arangodb.velocypack.exception.VPackException;
import org.junit.Test;

import java.util.Iterator;
import java.util.Map.Entry;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Mark Vollmary
 */
public class VPackExample {

    @Test
    public void buildObject() throws VPackException {
        final VPackBuilder builder = new VPackBuilder();
        builder.add(ValueType.OBJECT);// object start
        builder.add("foo", 1); // add field "foo" with value 1
        builder.add("bar", 2); // add field "bar" with value 2
        builder.close();// object end

        final VPackSlice slice = builder.slice(); // create slice
        assertThat(slice.isObject(), is(true));
        assertThat(slice.size(), is(2)); // number of fields

        final VPackSlice foo = slice.get("foo"); // get field "foo"
        assertThat(foo.isInteger(), is(true));
        assertThat(foo.getAsInt(), is(1));

        final VPackSlice bar = slice.get("bar"); // get field "bar"
        assertThat(bar.isInteger(), is(true));
        assertThat(bar.getAsInt(), is(2));

        // iterate over the fields
        for (final Iterator<Entry<String, VPackSlice>> iterator = slice.objectIterator(); iterator.hasNext(); ) {
            final Entry<String, VPackSlice> field = iterator.next();
            assertThat(field.getValue().isInteger(), is(true));
        }
    }

    @Test
    public void buildArray() throws VPackException {
        final VPackBuilder builder = new VPackBuilder();
        builder.add(ValueType.ARRAY); // array start
        builder.add(1);// add value 1
        builder.add(2);// add value 2
        builder.add(3);// add value 3
        builder.close(); // array end

        final VPackSlice slice = builder.slice();// create slice
        assertThat(slice.isArray(), is(true));
        assertThat(slice.size(), is(3));// number of values

        // iterate over values
        for (int i = 0; i < slice.size(); i++) {
            final VPackSlice value = slice.get(i);
            assertThat(value.isInteger(), is(true));
            assertThat(value.getAsInt(), is(i + 1));
        }

        // iterate over values with Iterator
        for (final Iterator<VPackSlice> iterator = slice.arrayIterator(); iterator.hasNext(); ) {
            final VPackSlice value = iterator.next();
            assertThat(value.isInteger(), is(true));
        }
    }

    @Test
    public void buildObjectInObject() throws VPackException {
        final VPackBuilder builder = new VPackBuilder();
        builder.add(ValueType.OBJECT);// object start
        builder.add("foo", ValueType.OBJECT); // add object in field "foo"
        builder.add("bar", 2); // add field "bar" with value 2 to object "foo"
        builder.close();// object "foo" end
        builder.close();// object end

        final VPackSlice slice = builder.slice(); // create slice
        assertThat(slice.isObject(), is(true));

        final VPackSlice foo = slice.get("foo");
        assertThat(foo.isObject(), is(true));

        final VPackSlice bar = foo.get("bar"); // get field "bar" from "foo"
        assertThat(bar.isInteger(), is(true));
    }

}
