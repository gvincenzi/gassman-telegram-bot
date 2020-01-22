package org.gassman.telegram.bot.listener;

import org.gassman.telegram.bot.binding.MQBinding;
import org.gassman.telegram.bot.dto.OrderDTO;
import org.gassman.telegram.bot.dto.UserDTO;
import org.gassman.telegram.bot.service.TelegramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@EnableBinding(MQBinding.class)
public class MQListener {
    @Autowired
    TelegramService telegramService;

    @StreamListener(target = MQBinding.USER_REGISTRATION)
    public void processUserRegistration(UserDTO msg) throws TelegramApiException {
        telegramService.sendRegistrationMessage(msg);
    }

    @StreamListener(target = MQBinding.USER_ORDER)
    public void processUserOrder(OrderDTO msg) throws TelegramApiException {
        telegramService.sendOrderMessage(msg);
    }

    @StreamListener(target = MQBinding.ORDER_PAYMENT_CONFIRMATION)
    public void processOrderPaymentConfirmation(OrderDTO msg) throws TelegramApiException {
        telegramService.sendOrderPaymentConfirmationMessage(msg);
    }
}