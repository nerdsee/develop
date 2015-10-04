package org.stoevesand.brain;

import java.sql.ResultSet;
import java.util.Vector;

import org.stoevesand.util.DBUtil;

public class Topic {

	//private static Logger log = Logger.getLogger(Topic.class);

	private String text = null;
	private String icon = null;
	private long id = 0;
	private long parentId = 0;
	private Topic parentTopic=null;
	
	private Vector<Topic> subTopics = new Vector<Topic>();

	public Topic(ResultSet rs) {
			this.text = DBUtil.getString(rs, "text");
			this.icon = DBUtil.getString(rs, "icon");
			this.id = DBUtil.getLong(rs,"id");
			this.parentId = DBUtil.getLong(rs,"parentId");
	}

	public long getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public String getIcon() {
		return icon;
	}

	public Topic getParentTopic() {
		return parentTopic;
	}

	public long getParentId() {
		return parentId;
	}
	
	public Vector<Topic> getSubTopics() {
		return subTopics;
	}

	public void addSubTopic(Topic subTopic) {
			subTopic.setParentTopic(this);
			subTopics.add(subTopic);
	}

	public void setParentTopic(Topic parentTopic) {
		this.parentTopic = parentTopic;
	}
	
	public String toString() {
		return "[Topic: "+id+"]";
	}

	public void getTopicPath(Vector<Topic> tpp) {
		tpp.add(0,this);
		if (parentTopic!=null)
			parentTopic.getTopicPath(tpp);
		return;
	}
}
