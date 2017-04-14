package org.stoevesand.findow.server;

import org.stoevesand.finapi.FinapiBankingAPI;
import org.stoevesand.findow.bankingapi.BankingAPI;

public class FindowSystem {

	public static BankingAPI getBankingAPI() {
		return FinapiBankingAPI.getInstance();
	}
	
}
