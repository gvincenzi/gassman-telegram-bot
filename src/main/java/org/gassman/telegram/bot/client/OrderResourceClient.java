package org.gassman.telegram.bot.client;

import org.gassman.telegram.bot.configuration.FeignClientConfiguration;
import org.gassman.telegram.bot.dto.OrderDTO;
import org.gassman.telegram.bot.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@FeignClient(name = "gassman-order-service/orders", configuration = FeignClientConfiguration.class)
public interface OrderResourceClient {
    @GetMapping("/users/{id}")
    List<OrderDTO> findAllOrdersByUser(@PathVariable Long id);

    @GetMapping("/{id}")
    OrderDTO findOrderById(@PathVariable Long id);

    @PostMapping("/telegram")
    OrderDTO postOrder(@RequestBody OrderDTO order);
}
