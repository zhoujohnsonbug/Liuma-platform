package com.autotest.LiuMa;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication(scanBasePackages={"com.autotest.LiuMa"})
@MapperScan("com.autotest.LiuMa.database.mapper")
public class LiuMaApplication {

	public static void main(String[] args) {

		SpringApplication.run(LiuMaApplication.class, args);
	}

}
