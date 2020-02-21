package org.gassman.telegram.bot.service;

import org.gassman.telegram.bot.dto.OrderDTO;
import org.gassman.telegram.bot.dto.UserDTO;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface TelegramAdministratorService {
    void sendRegistrationMessage(UserDTO userDTO) throws TelegramApiException;
    void sendOrderMessage(OrderDTO orderDTO) throws TelegramApiException;
    void sendOrderPaymentConfirmationMessage(OrderDTO orderDTO) throws TelegramApiException;
    void advertising(String text) throws TelegramApiException;
}
