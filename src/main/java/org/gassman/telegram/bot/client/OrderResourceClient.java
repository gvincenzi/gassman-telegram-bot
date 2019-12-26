package org.gassman.telegram.bot.client;

import org.gassman.telegram.bot.dto.OrderDTO;
import org.gassman.telegram.bot.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gassman-order-service/orders")
public interface OrderResourceClient {
    @PostMapping("/telegram")
    OrderDTO postOrder(@RequestBody OrderDTO order);
}
