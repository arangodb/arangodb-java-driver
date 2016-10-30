package com.arangodb;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * @author Anton Sarov - initial contribution
 *
 */
public class ArangoHostTest {

	@Test
	public void testToString() {
		ArangoHost arangoHost = new ArangoHost("127.0.0.1", 8529);
		assertThat(arangoHost.toString(), is("127.0.0.1:8529"));
	}
	
	@Test
	public void testToStringIPv6() {
		ArangoHost arangoHost = new ArangoHost("2001:db8:1f70::999:de8:7648:6e8", 8529);
		assertThat(arangoHost.toString(), is("[2001:db8:1f70::999:de8:7648:6e8]:8529"));
	}
}
