package com.arangodb;

public class ArangoHost {

	/**
	 * server port
	 * */
	int port;

	/**
	 * server host
	 * */
	String host;

	public ArangoHost(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setHost(String host) {
		this.host = host;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArangoHost other = (ArangoHost) obj;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (port != other.port)
			return false;
		return true;
	}

	/**
	 * Returns a string representation in the form <code>host:port</code>,
	 * adding square brackets if needed
	 */
	@Override
	public String toString() {
	    // "[]:12345" requires 8 extra bytes.
	    StringBuilder builder = new StringBuilder(host.length() + 8);
	    if (host.indexOf(':') >= 0) {
	      builder.append('[').append(host).append(']');
	    } else {
	      builder.append(host);
	    }
	    builder.append(':').append(port);
	    return builder.toString();
	}
}
