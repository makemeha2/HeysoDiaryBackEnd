package heyso.HeysoDiaryBackEnd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class HeysoDiaryBackEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(HeysoDiaryBackEndApplication.class, args);

		log.info("{} : 정상적으로 출력됩니다.", "로그 테스트");
		System.out.println(new BCryptPasswordEncoder().encode("1234"));
	}
}