package id.my.hendisantika.fiboserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class FiboServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FiboServerApplication.class, args);
	}

}

// INITIALIZE
@Slf4j
@Component
class SampleInitializer {
	@EventListener(ApplicationReadyEvent.class)
	public void ready() {
		log.info("Fibo Server Working ...");
	}
}
