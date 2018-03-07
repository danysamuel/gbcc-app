package com.danyprj.gbcc.tasklet;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class FileMoveTasklet implements Tasklet, InitializingBean {


	@Autowired
	private Environment environment;

	private Resource directory;

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(directory, "directory must be set");
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

		File dir = directory.getFile();

		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {

			Files.move(files[i].toPath(), Paths.get(environment.getProperty("TRANSACTION_PROCESSING") + "\\processed\\" + files[i].getName()),
					StandardCopyOption.REPLACE_EXISTING);
			
		}
		return RepeatStatus.FINISHED;
	}

	public Resource getDirectory() {
		return directory;
	}

	public void setDirectory(Resource directory) {
		this.directory = directory;
	}

}