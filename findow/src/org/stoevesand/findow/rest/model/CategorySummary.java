package org.stoevesand.findow.rest.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.stoevesand.finapi.model.Category;
import org.stoevesand.finapi.model.Transaction;
import org.stoevesand.finapi.model.TransactionList;

public class CategorySummary {

	Map<Category, CategorySum> summary = new HashMap<Category, CategorySum>();
	
	public CategorySummary(TransactionList transactionList) {
		
		Category none = new Category("NONE", "");
		
		for (Transaction tx : transactionList.getTransactions()) {
			Category category = tx.getCategory();

			// wenn keine Category an den tx hängt, nimm die Dummy Cat
			if (category==null) {
				category = none;
			}
			
			// CatSum entry zur Category suchen
			CategorySum entry = summary.get(category);
			
			// category war noch nicht da, dann in die Map damit
			if (entry==null) {
				entry = new CategorySum(category);
				summary.put(category, entry);
			}

			entry.add(tx.getAmount());
		
		}
	}
	
	public Collection<CategorySum> getSummary() {
		return summary.values();
	}

}
