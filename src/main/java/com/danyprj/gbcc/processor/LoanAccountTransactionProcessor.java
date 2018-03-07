package com.danyprj.gbcc.processor;

import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;

import com.danyprj.gbcc.model.LoanAccountTransaction;

public class LoanAccountTransactionProcessor implements ItemProcessor<LoanAccountTransaction,LoanAccountTransaction> {

	private static final Logger logger = org.apache.log4j.Logger.getLogger(LoanAccountTransactionProcessor.class);

	
	@Override
	public LoanAccountTransaction process(LoanAccountTransaction lat) throws Exception {
		logger.info("-----------------------Inside Processor----------------------");

		lat.setIsValid(isNumeric(lat.getAccountId())?true:false);
		logger.info("Processed Object---------------------------------->"+lat.toString());
		return lat;
	}
	
	
	
	public static boolean isNumeric(String str)
	{
	  return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}

}
