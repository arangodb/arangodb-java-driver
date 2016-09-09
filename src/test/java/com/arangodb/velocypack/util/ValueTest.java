package com.arangodb.velocypack.util;

import java.math.BigInteger;

import org.junit.Test;

import com.arangodb.velocypack.ValueType;
import com.arangodb.velocypack.exception.VPackValueTypeException;
import com.arangodb.velocypack.internal.Value;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ValueTest {

	@Test(expected = VPackValueTypeException.class)
	public void wrongType() {
		new Value(ValueType.STRING);
	}

	@Test(expected = VPackValueTypeException.class)
	public void wrongLongType() {
		new Value(1L, ValueType.STRING);
	}

	@Test(expected = VPackValueTypeException.class)
	public void wrongIntegerType() {
		new Value(new Long(1L), ValueType.STRING);
	}

	@Test(expected = VPackValueTypeException.class)
	public void wrongBigIntegerType() {
		new Value(new BigInteger("1"), ValueType.STRING);
	}

}
