package com.arangodb.internal.net;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class AuthenticationRequest extends Request {

	private final String user;
	private final String password;
	private final String encryption;// "plain"

	public AuthenticationRequest(final String user, final String password, final String encryption) {
		super(null, null, null);
		this.user = user;
		this.password = password;
		this.encryption = encryption;
		setType(1000);
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public String getEncryption() {
		return encryption;
	}

}
