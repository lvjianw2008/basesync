package com.bd.basesync;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.bd.basesync")
@MapperScan("com.bd.basesync.dao")
@SpringBootApplication
public class BasesyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(BasesyncApplication.class, args);
	}

}

