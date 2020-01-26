package org.gassman.telegram.bot.client;

import org.gassman.telegram.bot.configuration.FeignClientConfiguration;
import org.gassman.telegram.bot.dto.OrderDTO;
import org.gassman.telegram.bot.dto.ProductDTO;
import org.gassman.telegram.bot.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "gassman-order-service/products", configuration = FeignClientConfiguration.class)
public interface ProductResourceClient {
    @GetMapping()
    List<ProductDTO> findAll();

    @GetMapping("/{id}/orders")
    List<OrderDTO> findProductOrders(@PathVariable Long id);
}
