package org.stoevesand.finapi.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.annotations.GenericGenerator;
import org.stoevesand.findow.persistence.PersistanceManager;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "TRANSACTIONS")
public class Transaction {

	JSONObject jo = null;

	// internal id used for persistance
	private Long id;

	// id coming from a source system
	private Long sourceId;
	private String sourceSystem = "FINAPI";

	private transient int parentId;
	private Long accountId;
	private double amount;
	private transient String valueDate;
	private Date bookingDate;
	private String purpose;
	private String counterpartName;

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public void setCounterpartName(String counterpartName) {
		this.counterpartName = counterpartName;
	}

	private Category category;

	private String type;

	public Transaction() {
		purpose = "-";
		counterpartName = "-";
	}

	@Column(name = "AMOUNT")
	public double getAmount() {
		return amount;
	}

	@Id
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	@Column(name = "TX_ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Transient
	public int getParentId() {
		return parentId;
	}

	@Column(name = "ACCOUNT_ID")
	@JsonIgnore
	public Long getAccountId() {
		return accountId;
	}

	@Column(name = "TYPE")
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	@Transient
	public String getValueDate() {
		return valueDate;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "BOOKING_DATE")
	public Date getBookingDate() {
		return bookingDate;
	}

	@Column(name = "PURPOSE")
	public String getPurpose() {
		return purpose;
	}

	@Column(name = "COUNTERPART_NAME")
	public String getCounterpartName() {
		return counterpartName;
	}

	@ManyToOne(fetch = FetchType.EAGER, optional = true)
	@JoinColumn(name = "CATEGORY_ID", nullable = true)
	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Transaction(JSONObject jo) {
		this.jo = jo;
		try {
			sourceId = jo.getLong("id");
			// parentId = jo.getInt("parentId");
			accountId = jo.getLong("accountId");
			amount = jo.getDouble("amount");
			valueDate = jo.getString("valueDate");

			String bookingDateText = jo.getString("finapiBookingDate");
			try {
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				bookingDate = df.parse(bookingDateText);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			purpose = jo.getString("purpose");
			counterpartName = jo.getString("counterpartName");
			type = jo.getString("type");

			JSONObject jocat = jo.getJSONObject("category");
			if (jocat != null) {
				category = PersistanceManager.getInstance().getCategory( new Category(jocat));
			}

		} catch (JSONException e) {
		}
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public void setBookingDate(Date bookingDate) {
		this.bookingDate = bookingDate;
	}

	@JsonIgnore
	public String toString() {
		return String.format("** %d # %s # %f # %s # %s", id, purpose, amount, counterpartName, category);
	}

	@Column(name = "SOURCE_ID")
	public Long getSourceId() {
		return sourceId;
	}

	public void setSourceId(Long sourceid) {
		this.sourceId = sourceid;
	}

	public String getSourceSystem() {
		return sourceSystem;
	}

	public void setSourceSystem(String sourceSystem) {
		this.sourceSystem = sourceSystem;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

}
