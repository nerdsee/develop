package org.stoevesand.brain;

import org.stoevesand.brain.model.Lesson;

public class LessonWrapper {

	public Lesson lesson=null;
	public boolean subscribed=false;
	public boolean owner;
	public boolean group;
	
	public Lesson getLesson() {
		return lesson;
	}
	public void setLesson(Lesson lesson) {
		this.lesson = lesson;
	}
	public boolean getIsSubscribed() {
		return subscribed;
	}
	public void setSubscribed(boolean subscribed) {
		this.subscribed = subscribed;
	}
	
	public boolean getIsOwner() {
		return owner;
	}

	public boolean getIsGroup() {
		return group;
	}

	public boolean getIsEditable() {
		return getIsOwner();
	}

}
