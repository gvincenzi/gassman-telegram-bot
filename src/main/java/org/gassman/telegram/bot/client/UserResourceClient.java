package org.gassman.telegram.bot.client;

import org.gassman.telegram.bot.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("gassman-order-service/users")
public interface UserResourceClient {
    @PostMapping()
    UserDTO addUser(@RequestBody UserDTO userDTO);

    @DeleteMapping("/telegram/{id}")
    Boolean deleteUser(@PathVariable("id") Integer id);
}
