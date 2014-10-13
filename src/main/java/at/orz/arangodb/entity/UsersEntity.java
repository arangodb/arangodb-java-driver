package at.orz.arangodb.entity;

import java.util.List;
import java.util.Map;

/**
 * @author a-brandt
 * 
 */
public class UsersEntity extends BaseEntity {

	private List<UsersEntity> users;
	private Map<String, UsersEntity> names;

	public List<UsersEntity> getUsers() {
		return users;
	}

	public void setUsers(List<UsersEntity> users) {
		this.users = users;
	}

	public Map<String, UsersEntity> getNames() {
		return names;
	}

	public void setNames(Map<String, UsersEntity> names) {
		this.names = names;
	}

}
