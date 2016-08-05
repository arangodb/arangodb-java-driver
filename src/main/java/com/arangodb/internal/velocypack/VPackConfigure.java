package com.arangodb.internal.velocypack;

import com.arangodb.internal.net.velocystream.RequestType;
import com.arangodb.velocypack.VPack;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VPackConfigure {

	public static void configure(final VPack vpack) {
		vpack.registerSerializer(RequestType.class, VPackSerializers.REQUEST_TYPE);
	}

}
