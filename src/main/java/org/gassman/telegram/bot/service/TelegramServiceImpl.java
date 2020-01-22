package org.gassman.telegram.bot.service;

import org.gassman.telegram.bot.GassmanOrderBot;
import org.gassman.telegram.bot.client.UserResourceClient;
import org.gassman.telegram.bot.dto.OrderDTO;
import org.gassman.telegram.bot.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Service
public class TelegramServiceImpl implements TelegramService {
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
}
