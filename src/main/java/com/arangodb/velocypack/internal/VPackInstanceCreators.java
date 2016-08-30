package com.arangodb.velocypack.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.arangodb.velocypack.VPackInstanceCreator;

/**
 * @author Mark - mark at arangodb.com
 *
 */
@SuppressWarnings("rawtypes")
public class VPackInstanceCreators {

	private VPackInstanceCreators() {
		super();
	}

	public static final VPackInstanceCreator<Collection> COLLECTION = new VPackInstanceCreator<Collection>() {
		@Override
		public Collection createInstance() {
			return new ArrayList();
		}
	};
	public static final VPackInstanceCreator<List> LIST = new VPackInstanceCreator<List>() {
		@Override
		public List createInstance() {
			return new ArrayList();
		}
	};
	public static final VPackInstanceCreator<Set> SET = new VPackInstanceCreator<Set>() {
		@Override
		public Set createInstance() {
			return new HashSet();
		}
	};
	public static final VPackInstanceCreator<Map> MAP = new VPackInstanceCreator<Map>() {
		@Override
		public Map createInstance() {
			return new HashMap();
		}
	};

}
