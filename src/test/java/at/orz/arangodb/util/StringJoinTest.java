package at.orz.arangodb.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;


public class StringJoinTest {

	@Test
	public void test() {
		
		assertThat(StringUtils.join((String[])null), is(nullValue()));
		assertThat(StringUtils.join(""), is("/"));

		assertThat(StringUtils.join("base", "p1"), is("/base/p1"));
		assertThat(StringUtils.join("base", "/p1"), is("/base/p1"));
		assertThat(StringUtils.join("base/", "p1"), is("/base/p1"));
		assertThat(StringUtils.join("/base/", "/p1"), is("/base/p1"));

		assertThat(StringUtils.join("/base", "/p1", "abc"), is("/base/p1/abc"));

		assertThat(StringUtils.join("/base", "p1", "p2", "abc"), is("/base/p1/p2/abc"));

		assertThat(StringUtils.join("/base", null, "p2", "abc"), is("/base/p2/abc"));
		assertThat(StringUtils.join("/base", null, null, "abc"), is("/base/abc"));
		assertThat(StringUtils.join(null, null, "abc"), is("/abc"));

	}
	
}
