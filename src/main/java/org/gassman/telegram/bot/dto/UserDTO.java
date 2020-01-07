package org.gassman.telegram.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private String surname;
    private String mail;
    private Integer telegramUserId;

    public String toHTTPQuery(String prefix) {
        return prefix + ".id=" + id;
    }
}
