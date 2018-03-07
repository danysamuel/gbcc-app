package com.danyprj.gbcc.model;

import java.io.Serializable;

public class LoanAccountTransaction implements Serializable{

	
	public LoanAccountTransaction() {
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 6717581130197017557L;

	private String accountId;
	
	private Double amount = 0.0;
	
	private Boolean isValid;

	public Boolean getIsValid() {
		return isValid;
	}

	public void setIsValid(Boolean isValid) {
		this.isValid = isValid;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public LoanAccountTransaction(String accountId, Double amount) {
		super();
		this.accountId = accountId;
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "LoanAccountTransaction [accountId=" + accountId + ", amount=" + amount + ", isValid=" + isValid + "]";
	}

	
	

}
