package org.gassman.telegram.bot.client;

import org.gassman.telegram.bot.configuration.FeignClientConfiguration;
import org.gassman.telegram.bot.dto.UserCreditDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.math.BigDecimal;

@FeignClient(name = "gassman-payment-service/internal-credit", configuration = FeignClientConfiguration.class)
public interface UserCreditResourceClient {
    @GetMapping("/{userId}")
    UserCreditDTO findById(@PathVariable("userId") Long userId);

    @GetMapping("/totalUserCredit")
    BigDecimal totalUserCredit();

    @GetMapping("/order/{orderId}/price")
    BigDecimal getOrderPrice(@PathVariable("orderId") Long orderId);

    @PutMapping(value = "/order/{orderId}/pay")
    String makePayment(@PathVariable("orderId") Long orderId);
}
