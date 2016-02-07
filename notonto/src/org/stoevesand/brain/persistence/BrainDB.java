package org.stoevesand.brain.persistence;

import java.sql.Connection;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import org.jfree.data.time.TimeSeries;
import org.stoevesand.brain.Category;
import org.stoevesand.brain.Group;
import org.stoevesand.brain.Topic;
import org.stoevesand.brain.UserScore;
import org.stoevesand.brain.auth.User;
import org.stoevesand.brain.exceptions.DBException;
import org.stoevesand.brain.model.Answer;
import org.stoevesand.brain.model.UserLesson;
import org.stoevesand.brain.model.Item;
import org.stoevesand.brain.model.Lesson;
import org.stoevesand.brain.model.UserItem;
import org.stoevesand.brain.model.UserLesson;
import org.stoevesand.util.News;

public interface BrainDB {

	Connection getConnection(String caller) throws DBException;

	// Connection getConnection() throws DBException;

	Vector<User> getUsers() throws DBException;

	Vector<Lesson> getLessons() throws DBException;

	Vector<Lesson> getLessons(Topic topic) throws DBException;

	UserLesson subscribeLesson(User user, Lesson lesson) throws DBException;

	void unsubscribeLesson(User user, UserLesson lesson) throws DBException;

	void unsubscribeLesson(User user, Lesson lesson) throws DBException;

	void addItem(Item item) throws DBException;

	// void addAnswer(Answer answer) throws DBException;

	void addLesson(Lesson lesson) throws DBException;

	void addUser(User user) throws DBException;

	void addNews(News news) throws DBException;

	Vector<UserLesson> getUserLessons(User user) throws DBException;

	Vector<UserLesson> getUserLessons(Lesson lesson) throws DBException;

	void storeUserLesson(UserLesson userLesson) throws DBException;

	Lesson getLesson(long lessonId) throws DBException;

	Item getItem(long itemId) throws DBException;

	Vector<Item> getItems(Lesson lesson) throws DBException;

	Vector<Item> getItems(Lesson lesson, String filter) throws DBException;

	Vector<Category> getCategories(Lesson lesson) throws DBException;

	Vector<Category> getCategories() throws DBException;

	void deleteItem(Item item) throws DBException;

	// Answer getAnswer(long answerId);

	UserItem getUserItem(long userItemId) throws DBException;

	UserItem getUserItem(User user, long userItemId) throws DBException;

	UserItem getNextUserItem(long userLessonId) throws DBException;

	UserItem getNextUserItem(User user, long userLessonId) throws DBException;

	UserItem getNextUserItem(UserLesson userLesson) throws DBException;

	Date getNextUserItemTime(long userLessonId) throws DBException;

	long getNextUserItemTimeDiff(long userLessonId) throws DBException;

	void update(UserItem item) throws DBException;

	int getUserAvailable(User user) throws DBException;

	int getUserAvailable(long userId);

	int getUserLessonAvailable(UserLesson lesson) throws DBException;

	int getUserLessonAvailable(long userLessonId);

	UserLesson getUserLesson(User user, long userLessonId) throws DBException;

	UserLesson getUserLessonByLessonID(User user, long lid) throws DBException;

	User getUser(long userId) throws DBException;

	void loadUser(User user, String name) throws DBException;

	User getUser(String name, String password) throws DBException;

	void deactivateUserItem(UserItem userItem) throws DBException;

	// void activateUserItem(UserLesson userLesson) throws DBException;

	void activateUserItemExp(UserLesson userLesson) throws DBException;

	Vector<Answer> getAnswers(long itemId) throws DBException;

	int getUserScore(User user) throws DBException;

	// int getUserLessonScore(UserLesson userLesson);

	void storeUserScore(User user, int score) throws DBException;

	void getUserLessonLevels(UserLesson parentUserLesson) throws DBException;

	public String getLessonLevels(Lesson lesson) throws DBException;

	boolean emailIsAlreadyUsed(String email) throws DBException;

	void unlockUser(User user) throws DBException;

	boolean unlockUser(String emailAddress, String unlock) throws DBException;

	void storeComment(UserItem userItem, String comment, String commentType) throws DBException;

	void storeLastLogin(User user) throws DBException;

	void deleteLesson(Lesson lesson) throws DBException;

	void loadScoreHistory(TimeSeries s1, User user) throws DBException;

	int getCategoryItemCount(Category category) throws DBException;

	void storeCategories(Lesson lesson, String tags, String locale) throws DBException;

	String getUserPassword(String email);

	void changePassword(User cu, String passnew) throws DBException;

	void changeNickname(User cu, String nicknew) throws DBException;

	boolean checkNickname(User cu, String nicknew) throws DBException;

	Vector<News> getNews(Locale locale) throws DBException;

	String getConfigParameter(UserLesson parent, String name) throws DBException;

	void setConfigParameter(UserLesson parent, String name, String value) throws DBException;

	Vector<UserScore> getTop5();

	Vector<UserScore> getLessonTop5(Lesson lesson);

	Topic getTopicTree(String lang);

	int getItemCount(Lesson lesson) throws DBException;

	int getSubscriberCount(Lesson lesson) throws DBException;

	Vector<Lesson> getOwnerLessons(User cu) throws DBException;

	int getHighestChapter(Lesson lesson) throws DBException;

	Group getGroup(long groupId) throws DBException;

	void deleteAccount(User user) throws DBException;

	void changeStatusMailFreq(User user) throws DBException;

	boolean checkUserPrefix(User cu, String prefix) throws DBException;

	void changePrefix(User cu, String prefixnew) throws DBException;

	Vector<Lesson> getLessonsByCode(String code) throws DBException;

	Vector<Lesson> getLessonsByFilter(String filter) throws DBException;

	void resetUserLesson(UserLesson userLesson) throws DBException;

}
