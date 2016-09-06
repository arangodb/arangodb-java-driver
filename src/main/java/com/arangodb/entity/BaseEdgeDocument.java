package com.arangodb.entity;

import java.util.Map;

import com.arangodb.entity.DocumentField.Type;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class BaseEdgeDocument extends BaseDocument {

	@DocumentField(Type.FROM)
	private String from;
	@DocumentField(Type.TO)
	private String to;

	public BaseEdgeDocument() {
		super();
	}

	public BaseEdgeDocument(final Map<String, Object> properties) {
		super(properties);
		final Object tmpFrom = properties.remove(DocumentField.Type.FROM.getSerializeName());
		if (tmpFrom != null) {
			from = tmpFrom.toString();
		}
		final Object tmpTo = properties.remove(DocumentField.Type.TO.getSerializeName());
		if (tmpTo != null) {
			to = tmpTo.toString();
		}
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(final String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(final String to) {
		this.to = to;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("BaseDocument [documentRevision=");
		sb.append(getRevision());
		sb.append(", documentHandle=");
		sb.append(getId());
		sb.append(", documentKey=");
		sb.append(getKey());
		sb.append(", from=");
		sb.append(getFrom());
		sb.append(", to=");
		sb.append(getTo());
		sb.append(", properties=");
		sb.append(getProperties());
		sb.append("]");
		return sb.toString();
	}
}
