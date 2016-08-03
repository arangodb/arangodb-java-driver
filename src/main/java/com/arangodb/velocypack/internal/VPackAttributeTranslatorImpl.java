package com.arangodb.velocypack.internal;

import java.util.HashMap;
import java.util.Map;

import com.arangodb.velocypack.VPackAttributeTranslator;
import com.arangodb.velocypack.VPackBuilder;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.Value;
import com.arangodb.velocypack.ValueType;
import com.arangodb.velocypack.exception.VPackException;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public class VPackAttributeTranslatorImpl implements VPackAttributeTranslator {

	private static final String KEY = "_key";
	private static final String REV = "_rev";
	private static final String ID = "_id";
	private static final String FROM = "_from";
	private static final String TO = "_to";

	private static final byte KEY_ATTRIBUTE = 0x31;
	private static final byte REV_ATTRIBUTE = 0x32;
	private static final byte ID_ATTRIBUTE = 0x33;
	private static final byte FROM_ATTRIBUTE = 0x34;
	private static final byte TO_ATTRIBUTE = 0x35;
	private static final byte ATTRIBUTE_BASE = 0x30;

	private VPackBuilder builder;
	private final Map<String, VPackSlice> attributeToKey;
	private final Map<Integer, VPackSlice> keyToAttribute;

	public VPackAttributeTranslatorImpl() {
		super();
		builder = null;
		attributeToKey = new HashMap<String, VPackSlice>();
		keyToAttribute = new HashMap<Integer, VPackSlice>();
		try {
			add(KEY, KEY_ATTRIBUTE - ATTRIBUTE_BASE);
			add(REV, REV_ATTRIBUTE - ATTRIBUTE_BASE);
			add(ID, ID_ATTRIBUTE - ATTRIBUTE_BASE);
			add(FROM, FROM_ATTRIBUTE - ATTRIBUTE_BASE);
			add(TO, TO_ATTRIBUTE - ATTRIBUTE_BASE);
			seal();
		} catch (final VPackException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void add(final String attribute, final int key) throws VPackException {
		if (builder == null) {
			builder = new VPackBuilder();
			builder.add(new Value(ValueType.OBJECT));
		}
		builder.add(attribute, new Value(key, (key < -6 || key > 9) ? ValueType.INT : ValueType.SMALLINT));
	}

	@Override
	public void seal() throws VPackException {
		if (builder == null) {
			return;
		}
		builder.close();
		final VPackSlice slice = builder.slice();
		for (int i = 0; i < slice.getLength(); i++) {
			final VPackSlice key = slice.keyAt(i);
			final VPackSlice value = slice.valueAt(i);
			attributeToKey.put(key.getAsString(), value);
			keyToAttribute.put(value.getAsInt(), key);
		}
	}

	@Override
	public VPackSlice translate(final String attribute) {
		return attributeToKey.get(attribute);
	}

	@Override
	public VPackSlice translate(final int key) {
		return keyToAttribute.get(key);
	}

}
