package com.danyprj.gbcc.repository;

import java.io.Serializable;

import org.springframework.data.repository.CrudRepository;

import com.danyprj.gbcc.model.LoanAccount;

public interface LoanAccountRepository extends CrudRepository<LoanAccount, Serializable>{
	

	
}
