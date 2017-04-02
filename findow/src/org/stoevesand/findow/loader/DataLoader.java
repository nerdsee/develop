package org.stoevesand.findow.loader;

import java.util.List;
import java.util.Vector;

import org.stoevesand.finapi.ErrorHandler;
import org.stoevesand.finapi.TransactionsService;
import org.stoevesand.finapi.model.Transaction;
import org.stoevesand.finapi.model.TransactionList;
import org.stoevesand.findow.persistence.PersistanceManager;

public class DataLoader {

	public static void updateTransactions(String userToken, int days) throws ErrorHandler {
		TransactionList transactions = null;
		transactions = TransactionsService.searchTransactions(userToken, null, days);

		List<Transaction> newTransactions = new Vector<Transaction>();
		
		for(Transaction tx : transactions.getTransactions()) {
			Transaction knownTx = PersistanceManager.getInstance().getTxByExternalId(tx.getSourceId());
			if (knownTx==null) {
				newTransactions.add(tx);
			}
		}
		
		if (transactions != null) {
			PersistanceManager.getInstance().storeTx(newTransactions);
		}

	}

}
