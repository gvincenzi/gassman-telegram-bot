package org.gassman.telegram.bot.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderDTO implements Comparable<OrderDTO>{
    private Long orderId;
    private Integer quantity;
    private UserDTO user;
    private ProductDTO product;
    private Boolean paid = Boolean.FALSE;

    @Override
    public int compareTo(OrderDTO orderDTO) {
        return this.product.getDeliveryDateTime().compareTo(orderDTO.getProduct().getDeliveryDateTime());
    }

    @Override
    public String toString() {
        return "\nID : " + orderId +
                ", Quantità : " + quantity +
                "\nProdotto : " + product
                + (paid ? "" : "\n\n**Quest'ordine non è ancora stato pagato**");
    }
}
