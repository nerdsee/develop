package org.stoevesand.findow.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.stoevesand.finapi.ErrorHandler;
import org.stoevesand.finapi.TokenService;
import org.stoevesand.finapi.model.Account;
import org.stoevesand.finapi.model.Token;
import org.stoevesand.findow.rest.RestUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "USERS")
public class User {

	// internal id used for persistance
	private Long id;
	private String name = "";
	private String password = "";
	private String backendName = "";
	private String backendSecret = "";

	private transient Token token = null;
	private Set<Account> accounts;

	@Column(name = "NAME")
	@JsonGetter
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "BACKEND_NAME")
	@JsonIgnore
	public String getBackendName() {
		return backendName;
	}

	public void setBackendName(String backendName) {
		this.backendName = backendName;
	}

	@Column(name = "BACKEND_SECRET")
	@JsonIgnore
	public String getBackendSecret() {
		return backendSecret;
	}

	public void setBackendSecret(String backendSecret) {
		this.backendSecret = backendSecret;
	}

	public User() {
	}

	public User(String name, String password, String backendName, String backendSecret) {
		this.name = name;
		this.password = password;
		this.backendName = backendName;
		this.backendSecret = backendSecret;
	}

	@Id
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	@Column(name = "USER_ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.EAGER) //
	public Set<Account> getAccounts() {
		return accounts;
	}

	public void setAccounts(Set<Account> a) {
		this.accounts = a;
	}

	@Column(name = "PASSWORD")
	@JsonIgnore
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Transient
	@JsonGetter
	public String getToken() {
		if ((token == null) || (!token.isValid())) {
			try {
				token = TokenService.requestUserToken(RestUtils.getClientToken(), backendName, backendSecret);
			} catch (ErrorHandler e) {
				token = null;
				e.printStackTrace();
			}
		}
		return token.getToken();
	}

	public void setToken(Token token) {
		this.token = token;
	}

}
