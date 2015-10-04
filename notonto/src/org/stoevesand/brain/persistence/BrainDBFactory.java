package org.stoevesand.brain.persistence;


public class BrainDBFactory {
	public static BrainDBFactory _instance = null;
	public static BrainDB _db = null;

	String dbcs=null;
	String dbname=null;
	String user=null;
	String pass=null;

	public static BrainDBFactory getInstance() {
		if (_instance == null)
			_instance = new BrainDBFactory();
		return _instance;
	}

	public void setCredentials(String dbcs, String dbname, String user, String pass) {
		this.dbcs = dbcs;
		this.dbname = dbname;
		this.user = user;
		this.pass = pass;
		_db=null;
	}

	public BrainDB getBrainDB() {
		if (_db == null)
			_db = new MySQLBrainDB(dbcs, dbname, user, pass);
		return _db;
	}
}
