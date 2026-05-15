package com.seurteaching.digitaltwin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class DigitaltwinApplication {

	private static final Logger logger = LoggerFactory.getLogger(DigitaltwinApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DigitaltwinApplication.class, args);
		logger.info("Started and running");
	}

}
