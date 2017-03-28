package org.stoevesand.findow.rest.model;

import org.stoevesand.finapi.model.Category;

public class CategorySum {

	private int sum = 0;
	public int getSum() {
		return sum;
	}

	public Category getCategory() {
		return category;
	}

	private Category category;

	public CategorySum(Category category) {
		this.category = category;
		sum = 0;
	}

	public void add(int amount) {
		sum = sum + amount;
	}

}
