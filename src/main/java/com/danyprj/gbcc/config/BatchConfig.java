package com.danyprj.gbcc.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;

import com.danyprj.gbcc.model.LoanAccountTransaction;
import com.danyprj.gbcc.processor.LoanAccountTransactionProcessor;
import com.danyprj.gbcc.repository.LoanAccountRepository;
import com.danyprj.gbcc.tasklet.FileMoveTasklet;
import com.danyprj.gbcc.writer.LoanAccountTransactionWriter;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private Environment environment;
	
	@Autowired
    public LoanAccountRepository repository;
	
	
	private static final Logger logger = org.apache.log4j.Logger.getLogger(BatchConfig.class);

	

	@Bean
	public FlatFileItemReader<LoanAccountTransaction> reader() {
		FlatFileItemReader<LoanAccountTransaction> reader = new FlatFileItemReader<LoanAccountTransaction>();
		Optional<Path> cpath;
		try {
			cpath = readLatestTransactionFileFromResource();

			reader.setStrict(false);
			reader.setResource(new FileSystemResource(cpath.get().toFile().getAbsolutePath()));
			reader.setLinesToSkip(1);
			reader.setLineMapper(new DefaultLineMapper<LoanAccountTransaction>() {
				{
					setLineTokenizer(new DelimitedLineTokenizer() {
						{
							setNames(new String[] { "accountId", "amount" });
						}
					});
					setFieldSetMapper(new BeanWrapperFieldSetMapper<LoanAccountTransaction>() {
						{
							setTargetType(LoanAccountTransaction.class);
						}
						
						
					});
				
				}
			});
			
			

		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("----------------------------Inside Reader----------------------------");
		return reader;
	}

	
	

	private Optional<Path> readLatestTransactionFileFromResource() throws IOException {

		Optional<Path> cpath;

		String arbDir = environment.getProperty("TRANSACTION_PROCESSING");
		try (Stream<Path> paths = Files.walk(Paths.get(arbDir + "/pending"))) {

			cpath = paths.filter(Files::isRegularFile).findFirst();
		}
		return cpath;
	}

	@Bean
	public LoanAccountTransactionProcessor processor() {
		
		return new LoanAccountTransactionProcessor();
	}

	 @Bean
	  public FileMoveTasklet fileMoveTasklet() {
	    FileMoveTasklet tasklet = new FileMoveTasklet();
	    String arbDir = environment.getProperty("TRANSACTION_PROCESSING");
	    tasklet.setDirectory(new FileSystemResource(arbDir+"/pending"));
	    return tasklet;
	  }
	
	@Bean
	public Job myJob() {
		return jobBuilderFactory.get("myJob").
				incrementer(new RunIdIncrementer())
				.flow(step1())
				.next(step2())
				.end()
				.build();
	}

	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1").<LoanAccountTransaction, LoanAccountTransaction>chunk(10)
				.reader(reader()).processor(processor())
				.writer(new LoanAccountTransactionWriter(environment.getProperty("TRANSACTION_PROCESSING"),repository))
				.build();
	}

	 @Bean
	  public Step step2() {
	    return stepBuilderFactory.get("step2").tasklet(fileMoveTasklet()).build();
	  }


	
	
}
