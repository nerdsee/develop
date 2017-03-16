package org.stoevesand.findow;

import org.stoevesand.finapi.model.User;

public class FindowUser {

	private User user;
	private String name;
	private String id;

	public String getId() {
		return id;
	}

	public FindowUser(String name, User user) {
		this.user = user;
		this.name = name;
		this.id = user.getId();
	}

	public User getUser() {
		return user;
	}

	public String getName() {
		return name;
	}

}
