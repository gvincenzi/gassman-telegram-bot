package org.gassman.telegram.bot;

import org.gassman.telegram.bot.binding.MQBinding;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.telegram.telegrambots.ApiContextInitializer;

@EnableBinding(MQBinding.class)
@EnableEurekaClient
@EnableFeignClients
@SpringBootApplication
public class GassmanTelegramBotApplication {

    public static void main(String[] args) {
        ApiContextInitializer.init();
        SpringApplication.run(GassmanTelegramBotApplication.class, args);
    }

}
