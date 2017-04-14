package org.stoevesand.findow.rest;

import org.stoevesand.finapi.TokenService;
import org.stoevesand.finapi.model.Token;
import org.stoevesand.findow.model.ErrorHandler;

public class TokenStore {

	static final String client_id = "7fbb36a6-e886-41fc-9a0a-3ead413cddb8";
	static final String client_secret = "3122d123-fdeb-498c-93c4-5eda3c10d396";
	static final String data_decryption_key = "e0c82a81c6886460f109fe5348c58884";

	// finAPI SANDBOX Admin-Client:
	static final String admin_client_id = "05e712de-dbb9-4b31-9889-ead3c151c54f";
	static final String admin_client_secret = "f3e251db-be41-46cc-a438-ecd55e4a7abc";
	static final String admin_data_decryption_key = "eeb4561adf992fc44313468e4035ccac";

	private Token clientToken;
	private Token adminToken;

	private TokenStore() {
		try {
			clientToken = TokenService.requestClientToken(client_id, client_secret);
			adminToken = TokenService.requestClientToken(admin_client_id, admin_client_secret);
		} catch (ErrorHandler e) {
			e.printStackTrace();
		}
	}

	private static TokenStore _instance = null;

	public static TokenStore getInstance() {
		if (_instance == null) {
			_instance = new TokenStore();
		}
		return _instance;
	}

	public void validateClientToken() {
		if (!clientToken.isValid()) {
			System.out.println("Refresh Token.");
			try {
				clientToken = TokenService.requestClientToken(client_id, client_secret);
			} catch (ErrorHandler e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Token still valid.");
		}

	}

	public void validateAdminToken() {
		if (!adminToken.isValid()) {
			System.out.println("Refresh AdminToken.");
			try {
				adminToken = TokenService.requestClientToken(admin_client_id, admin_client_secret);
			} catch (ErrorHandler e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("AdminToken still valid.");
		}

	}

	public Token getClientToken() {
		validateClientToken();
		return clientToken;
	}

	public Token getAdminToken() {
		validateAdminToken();
		return adminToken;
	}
}