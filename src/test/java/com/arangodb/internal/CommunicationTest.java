package com.arangodb.internal;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.net.ArangoDBRedirectException;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import org.easymock.EasyMock;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CommunicationTest {

	private final static Request REQUEST = new Request("", RequestType.GET, "");
	private final static ArangoDBRedirectException REDIRECT_EXCEPTION = new ArangoDBRedirectException("Test", "Location");

	@Test(expected = ArangoDBException.class)
	public void noRedirection() {
		HostHandler handler = EasyMock.createMock(HostHandler.class);
		EasyMock.replay(handler);

		new TrackingCommunication(handler).handleException(new ArangoDBException(""), REQUEST);
	}

	@Test(expected = ArangoDBException.class)
	public void exceptionOnClosing() {
		HostHandler handler = EasyMock.createMock(HostHandler.class);

		handler.closeCurrentOnError();
		EasyMock.expectLastCall().andThrow(new ArangoDBException(""));

		handler.fail();
		EasyMock.expectLastCall().once();

		EasyMock.replay(handler);

		try {
			new TrackingCommunication(handler).handleException(REDIRECT_EXCEPTION, REQUEST);
		} finally {
			EasyMock.verify(handler);
		}
	}

	@Test
	public void noExceptions() {
		HostHandler handler = EasyMock.createMock(HostHandler.class);
		handler.closeCurrentOnError();
		handler.fail();
		EasyMock.replay(handler);

		TrackingCommunication trackingCommunication = new TrackingCommunication(handler);
		trackingCommunication.handleException(REDIRECT_EXCEPTION, REQUEST);
		assertTrue(trackingCommunication.isExecuted());
	}

	private static class TrackingCommunication extends Communication {
		private boolean isExecuted;

		protected TrackingCommunication(HostHandler hostHandler) {
			super(hostHandler);
		}

		public boolean isExecuted() {
			return isExecuted;
		}

		@Override
		public Object execute(Request request, HostHandle hostHandle) {
			isExecuted = true;
			return null;
		}
	}

}
