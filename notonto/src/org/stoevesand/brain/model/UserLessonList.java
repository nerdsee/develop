package org.stoevesand.brain.model;

import java.util.Vector;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "userlessons")
@XmlAccessorType( XmlAccessType.NONE )
public class UserLessonList extends AbstractContent {

//	@XmlElementRefs(value = { 
//			@XmlElementRef(name = "userlesson", type = CombinedUserLesson.class), 
//			@XmlElementRef(name = "userlesson", type = UserLesson.class), 
//			@XmlElementRef(name = "userlesson", type = SingleUserLesson.class) 
//	})
	@XmlAnyElement
	public Vector<UserLesson> lessons;

	public UserLessonList() {
	};

	public UserLessonList(Vector<UserLesson> lessons) {
		this.lessons = lessons;
	};

}
