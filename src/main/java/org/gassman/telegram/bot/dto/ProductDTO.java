package org.gassman.telegram.bot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
public class ProductDTO implements Comparable<ProductDTO> {
    private Long productId;
    private String name;
    private String description;
    private String unitOfMeasure;
    private BigDecimal pricePerUnit;
    private Integer availableQuantity;
    private String deliveryDateTime;
    private Boolean active = Boolean.TRUE;

    @Override
    public String toString() {
        return  " Nome :'" + name + '\'' +
                "\nDescrizione :'" + description + '\'' +
                "\nPrezzo al " + unitOfMeasure +" : " + pricePerUnit + "€" +
                " -- Disponibilità : " + availableQuantity +
                "\nData di consegna : " + LocalDateTime.parse(deliveryDateTime).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    @Override
    public int compareTo(ProductDTO productDTO) {
        return this.deliveryDateTime.compareTo(productDTO.deliveryDateTime);
    }
}
