package org.gassman.telegram.bot.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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

    public String getTotalToPay(){
        return this.getQuantity() != null && this.getProduct() != null
                && this.getProduct().getPricePerUnit() != null ? new BigDecimal(getQuantity()).multiply(getProduct().getPricePerUnit()).toString() : null;
    }

    public String toHTTPQuery() {
        return "orderId=" + orderId + "&quantity=" + quantity + "&totalToPay=" + getTotalToPay() + user.toHTTPQuery("&user");
    }
}
