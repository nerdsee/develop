package org.stoevesand.test;

import java.util.List;

import org.stoevesand.findow.model.CategorySum;
import org.stoevesand.findow.persistence.PersistanceManager;
import org.stoevesand.findow.persistence.PostgresqlDatabase;

public class DBTest {

	public static void main(String[] args) {

		DBTest fb = new DBTest();
		fb.run();
	}

	void run() {

		List<CategorySum> res = PersistanceManager.getInstance().getCategorySummary();
		System.out.println(res);
	
	}
}