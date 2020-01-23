package org.gassman.telegram.bot.client;

import org.gassman.telegram.bot.dto.UserCreditDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient("gassman-payment-service/internal-credit")
public interface UserCreditResourceClient {
    @GetMapping("/{userId}")
    UserCreditDTO findById(@PathVariable("userId") Long userId);

    @GetMapping("/totalUserCredit")
    BigDecimal totalUserCredit();
}
