package org.stoevesand.brain.rest;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;

import org.stoevesand.brain.model.AbstractContent;
import org.stoevesand.brain.model.Answer;
import org.stoevesand.brain.model.Item;
import org.stoevesand.brain.model.UserItem;
import org.stoevesand.brain.model.UserLesson;
import org.stoevesand.brain.model.UserLessonList;

@XmlRootElement(name = "message")
public class Message {

	static final int ERROR_OK = 0;
	static final int ERROR_UNKNOWN = 100;
	static final int ERROR_ITEM_UNKNOWN = 100;

	@XmlElement
	Header header;

	@XmlElementRefs(value = { 
			@XmlElementRef(name = "useritem", type = UserItem.class), 
			@XmlElementRef(name = "item", type = Item.class), 
			@XmlElementRef(name = "answer", type = Answer.class), 
			@XmlElementRef(name = "userlessons", type = UserLessonList.class), 
			@XmlElementRef(name = "userlesson", type = UserLesson.class) 
	})
	AbstractContent content;

	public Message() {
	}

	public Message(Header header) {
		this.header = header;
	}

	public void addContent(AbstractContent content) {
		this.content = content;
	}
}
