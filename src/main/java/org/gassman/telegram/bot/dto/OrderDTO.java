package org.gassman.telegram.bot.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderDTO {
    private Long orderId;
    private Integer quantity;
    private UserDTO user;
    private ProductDTO product;
}
