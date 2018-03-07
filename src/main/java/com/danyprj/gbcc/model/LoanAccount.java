package com.danyprj.gbcc.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class LoanAccount {

	@Id
	private String accountId;
	private Double accountBalance = 0.0;
	
	public String getAccountId() {
		return accountId;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountId == null) ? 0 : accountId.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LoanAccount other = (LoanAccount) obj;
		if (accountId == null) {
			if (other.accountId != null)
				return false;
		} else if (!accountId.equals(other.accountId))
			return false;
		return true;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public Double getAccountBalance() {
		return accountBalance;
	}
	public LoanAccount() {
		super();
	}
	public void setAccountBalance(Double accountBalance) {
		this.accountBalance = accountBalance;
	}
}
