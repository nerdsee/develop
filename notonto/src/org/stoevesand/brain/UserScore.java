package org.stoevesand.brain;

import org.stoevesand.util.StringUtils;

public class UserScore {
	String name;
	String nick;
	int score;
	
	public UserScore(String name, String nick, int score) {
		this.name=name;
		this.nick=nick==null?StringUtils.generateNickname(name):nick;
		this.score=score;
	}

	public String getName() {
		return name;
	}

	public String getNick() {
		return nick;
	}

	public int getScore() {
		return score;
	}
	
	
}
