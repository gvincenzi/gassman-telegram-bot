package org.gassman.telegram.bot.client;

import org.gassman.telegram.bot.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("gassman-order-service/users")
public interface UserResourceClient {
    @GetMapping("/telegram/{id}")
    UserDTO findUserByTelegramId(@PathVariable("id") Integer id);

    @PostMapping()
    UserDTO addUser(@RequestBody UserDTO userDTO);

    @DeleteMapping("/telegram/{id}")
    Boolean deleteUser(@PathVariable("id") Integer id);

    @GetMapping("/administrator")
    List<UserDTO> getAdministrators();
}
