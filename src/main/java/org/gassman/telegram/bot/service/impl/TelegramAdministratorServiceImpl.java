package org.gassman.telegram.bot.service.impl;

import org.gassman.telegram.bot.polling.GassmanOrderBot;
import org.gassman.telegram.bot.client.UserResourceClient;
import org.gassman.telegram.bot.dto.OrderDTO;
import org.gassman.telegram.bot.dto.UserDTO;
import org.gassman.telegram.bot.service.TelegramAdministratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Service
public class TelegramAdministratorServiceImpl implements TelegramAdministratorService {
    @Autowired
    GassmanOrderBot gassmanOrderBot;

    @Autowired
    UserResourceClient userResourceClient;

    @Override
    public void sendRegistrationMessage(UserDTO userDTO) throws TelegramApiException {
        List<UserDTO> administrators = userResourceClient.getAdministrators();
        if(administrators != null && !administrators.isEmpty()) {
            for(UserDTO administrator : administrators) {
                SendMessage message = new SendMessage()
                        .setChatId(String.valueOf(administrator.getTelegramUserId()))
                        .setText("Nuovo utente registrato : " + userDTO.getName() + " " + userDTO.getSurname());
                gassmanOrderBot.execute(message);
            }
        }
    }

    @Override
    public void sendOrderMessage(OrderDTO orderDTO) throws TelegramApiException {
        List<UserDTO> administrators = userResourceClient.getAdministrators();
        if(administrators != null && !administrators.isEmpty()) {
            for(UserDTO administrator : administrators) {
                SendMessage message = new SendMessage()
                        .setChatId(String.valueOf(administrator.getTelegramUserId()))
                        .setText("Nuovo ordine registrato da "+orderDTO.getUser().getName()+" "+orderDTO.getUser().getSurname()+":\n" + orderDTO.toString());
                gassmanOrderBot.execute(message);
            }
        }
    }

    @Override
    public void sendOrderPaymentConfirmationMessage(OrderDTO orderDTO) throws TelegramApiException {
        List<UserDTO> administrators = userResourceClient.getAdministrators();
        if(administrators != null && !administrators.isEmpty()) {
            for(UserDTO administrator : administrators) {
                SendMessage message = new SendMessage()
                        .setChatId(String.valueOf(administrator.getTelegramUserId()))
                        .setText("Pagamento di un ordine effettuato da "+orderDTO.getUser().getName()+" "+orderDTO.getUser().getSurname()+":\n" + orderDTO.toString());
                gassmanOrderBot.execute(message);
            }
        }
    }

    @Override
    public void advertising(String text) throws TelegramApiException {
        List<UserDTO> users = userResourceClient.getUsers();
        for (UserDTO user: users) {
            if(user.getTelegramUserId() != null) {
                SendMessage message = new SendMessage()
                        .setChatId(String.valueOf(user.getTelegramUserId()))
                        .setText(String.format(text,user.getName()));
                gassmanOrderBot.execute(message);
            }
        }
    }
}
