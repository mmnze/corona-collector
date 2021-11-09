package de.mmenze.corona;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CoronaApplication {

	public static void main(String[] args) {
		// issue with serializing dates
		// see https://stackoverflow.com/questions/53715653/jackson-localdate-one-day-off-during-serialization
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(CoronaApplication.class, args);
	}

}
