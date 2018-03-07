package com.danyprj.gbcc.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemWriter;

import com.danyprj.gbcc.model.LoanAccount;
import com.danyprj.gbcc.model.LoanAccountTransaction;
import com.danyprj.gbcc.repository.LoanAccountRepository;

public class LoanAccountTransactionWriter implements ItemWriter<LoanAccountTransaction> {

	private String environment;

	private static final Logger logger = org.apache.log4j.Logger.getLogger(LoanAccountTransactionWriter.class);

	public LoanAccountRepository repository;

	List<LoanAccount> compiledLoanAcLst = null;
	List<LoanAccount> existingLoanAcLst=null;
	
	@Override
	public void write(List<? extends LoanAccountTransaction> loanTransactionLst) throws Exception {

		
		existingLoanAcLst=retriveLoanAccountRecords();
		if(existingLoanAcLst.size()>0) {
			
			compiledLoanAcLst = createCustomerDebitCreditWithExistingLoanAccountBalance(loanTransactionLst);
			
		}

		for (LoanAccount la : compiledLoanAcLst) {
//			LoanAccount laTemp = new LoanAccount();
//			laTemp.setAccountId(lat.getAccountId());
//			laTemp.setAccountBalance(lat.getAmount());
			repository.save(la);

		}

		logger.info("Read List Size---------------->" + loanTransactionLst.size());
		prepareReport(loanTransactionLst);

	}

	private List<LoanAccount> createCustomerDebitCreditWithExistingLoanAccountBalance(
			List<? extends LoanAccountTransaction> loanTransactionWithExistingLoanList) {

		for(LoanAccount la:existingLoanAcLst) {
			
			for(LoanAccountTransaction lat:loanTransactionWithExistingLoanList) {
				
				if(la.getAccountId().equals(lat.getAccountId())) {
					if(lat.getAmount()<0) {
						la.setAccountBalance(la.getAccountBalance() + Math.abs(lat.getAmount()));
					}else {
						la.setAccountBalance(la.getAccountBalance() - lat.getAmount());
					}
				}
			}
			
		}
		
		return existingLoanAcLst;
	}

	

	private List<LoanAccount> retriveLoanAccountRecords() {
		
		return (List<LoanAccount>) repository.findAll();
	}

	private Boolean prepareReport(List<? extends LoanAccountTransaction> loanTransactionLst) {
		Optional<Path> cpath;
		String fileDate = "";
		Boolean status = true;
		try {
			cpath = readLatestTransactionFileFromResource();

			fileDate = cpath.get().getFileName().toString().split("-")[1];
			Path path = Paths.get(environment + "/reports/finance_customer_transactions_report-" + fileDate + ".txt");
			try (BufferedWriter writer = Files.newBufferedWriter(path)) {
				writer.write("File Processed: " + cpath.get().getFileName().toString());
				writer.newLine();
				writer.write("Total Accounts: " + loanTransactionLst.size());
				writer.newLine();

				List<LoanAccountTransaction> totalCreditLst = loanTransactionLst.parallelStream()
						.filter(s -> s.getIsValid().equals(true)).filter(s -> s.getAmount() > 0)
						.collect(Collectors.toList());

				List<LoanAccountTransaction> totalDebitLst = loanTransactionLst.parallelStream()
						.filter(s -> s.getIsValid().equals(true)).filter(s -> s.getAmount() < 0)
						.collect(Collectors.toList());

				writer.write("Total Credits : $" + totalCreditLst.stream().mapToDouble(s -> s.getAmount()).sum());
				writer.newLine();
				writer.write("Total Debits : $" + Math.abs(totalDebitLst.stream().mapToDouble(s -> s.getAmount()).sum()));
				writer.newLine();
				writer.write("Skipped Transactions: "
						+ loanTransactionLst.parallelStream().filter(s -> s.getIsValid().equals(false)).count());

				logger.info("---------------------------Report Generated-----------------------------");
				logger.info("@Location : " + path.toAbsolutePath());
			} catch (IOException e) {
				status = false;
				e.printStackTrace();
			}
		} catch (IOException e) {
			status = false;
			e.printStackTrace();
		}

		return status;
	}

	private Optional<Path> readLatestTransactionFileFromResource() throws IOException {

		Optional<Path> cpath;

		logger.info("----------------------->evnvironment" + environment);
		try (Stream<Path> paths = Files.walk(Paths.get(environment + "/pending"))) {

			cpath = paths.filter(Files::isRegularFile).findFirst();
		}
		return cpath;
	}

	public LoanAccountTransactionWriter(String environment, LoanAccountRepository repository) {
		super();
		this.environment = environment;
		this.repository = repository;
	}

}