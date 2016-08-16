package com.arangodb.internal.velocypack;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.arangodb.entity.BaseDocument;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPack.Builder;
import com.arangodb.velocypack.VPackBuilder;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.Value;
import com.arangodb.velocypack.ValueType;
import com.arangodb.velocypack.exception.VPackException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class BaseDocumentTest {

	@Test
	public void serialize() throws VPackException {
		final BaseDocument entity = new BaseDocument();
		entity.setDocumentHandle("test/test");
		entity.setDocumentKey("test");
		entity.setDocumentRevision("test");
		entity.addAttribute("_id", "test");
		entity.addAttribute("a", "a");

		final Builder builder = new VPack.Builder();
		VPackConfigure.configure(builder, null);
		final VPack vpacker = builder.build();

		final VPackSlice vpack = vpacker.serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.size(), is(4));

		final VPackSlice id = vpack.get("_id");
		assertThat(id.isString(), is(true));
		assertThat(id.getAsString(), is("test/test"));

		final VPackSlice key = vpack.get("_key");
		assertThat(key.isString(), is(true));
		assertThat(key.getAsString(), is("test"));

		final VPackSlice rev = vpack.get("_rev");
		assertThat(rev.isString(), is(true));
		assertThat(rev.getAsString(), is("test"));

		final VPackSlice a = vpack.get("a");
		assertThat(a.isString(), is(true));
		assertThat(a.getAsString(), is("a"));
	}

	@Test
	public void deserialize() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("_id", new Value("test/test"));
		builder.add("_key", new Value("test"));
		builder.add("_rev", new Value("test"));
		builder.add("a", new Value("a"));
		builder.close();

		final VPack.Builder vbuilder = new VPack.Builder();
		VPackConfigure.configure(vbuilder, null);
		final VPack vpacker = vbuilder.build();

		final BaseDocument entity = vpacker.deserialize(builder.slice(), BaseDocument.class);
		assertThat(entity.getDocumentHandle(), is(notNullValue()));
		assertThat(entity.getDocumentHandle(), is("test/test"));
		assertThat(entity.getDocumentKey(), is(notNullValue()));
		assertThat(entity.getDocumentKey(), is("test"));
		assertThat(entity.getDocumentRevision(), is(notNullValue()));
		assertThat(entity.getDocumentRevision(), is("test"));
		assertThat(entity.getProperties().size(), is(1));
		assertThat(entity.getAttribute("a"), is("a"));
	}
}
