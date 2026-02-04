package com.notFoundTomAndJerry.notFoundJerry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
    System.out.println("\n" +
        "=================================================\n" +
        "👮🚨🏃🏽‍➡️ 404 Jerry Application 시작 완료!\n" +
        "=================================================\n" +
        "📋 Swagger UI: http://localhost:8080/swagger-ui/index.html\n" +
        "=================================================\n");
  }
}