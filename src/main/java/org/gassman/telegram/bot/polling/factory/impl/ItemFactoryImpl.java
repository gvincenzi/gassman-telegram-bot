package org.gassman.telegram.bot.polling.factory.impl;

import org.gassman.telegram.bot.dto.OrderDTO;
import org.gassman.telegram.bot.dto.ProductDTO;
import org.gassman.telegram.bot.dto.UserDTO;
import org.gassman.telegram.bot.polling.factory.ItemFactory;
import org.gassman.telegram.bot.service.ResourceManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ItemFactoryImpl implements ItemFactory {
    @Autowired
    ResourceManagerService resourceManagerService;

    public SendMessage welcomeMessage(Update update) {
        UserDTO user = resourceManagerService.findUserByTelegramId(update.getMessage().getFrom().getId());
        SendMessage message;
        message = new SendMessage()
                .setChatId(update.getMessage().getChatId())
                .setText(String.format("%s,\nScegli tra le seguenti opzioni:",user == null ? "Benvenuto nel sistema GasSMan" : "Ciao " + user.getName()));

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline4 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline5 = new ArrayList<>();
        if(user == null){
            rowInline1.add(new InlineKeyboardButton().setText("Iscrizione").setCallbackData("iscrizione"));
        } else {
            rowInline1.add(new InlineKeyboardButton().setText("Cancellazione").setCallbackData("cancellazione"));
            rowInline1.add(new InlineKeyboardButton().setText("Credito residuo").setCallbackData("creditoResiduo"));
            rowInline2.add(new InlineKeyboardButton().setText("I tuoi ordini").setCallbackData("listaOrdini"));
            rowInline3.add(new InlineKeyboardButton().setText("Lista dei prodotti").setCallbackData("listaProdotti"));
            rowInline4.add(new InlineKeyboardButton().setText("Fondo cassa").setCallbackData("fondoCassa"));
            rowInline4.add(new InlineKeyboardButton().setText("Totali dovuti ai fornitori").setCallbackData("totaliFornitori"));
            rowInline5.add(new InlineKeyboardButton().setText("Invia avviso agli iscritti").setCallbackData("advertising"));
        }

        // Set the keyboard to the markup
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        rowsInline.add(rowInline3);
        if(user != null && user.getAdministrator()){
            rowsInline.add(rowInline4);
            rowsInline.add(rowInline5);
        }
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        return message;
    }

    @Override
    public BigDecimal computeTotalAmountSupplier(ProductDTO productDTO) {
        BigDecimal total = BigDecimal.ZERO;
        List<OrderDTO> productOrders = resourceManagerService.findProductOrders(productDTO.getProductId());
        for(OrderDTO order : productOrders){
            total = total.add(BigDecimal.valueOf(order.getQuantity()).multiply(order.getProduct().getPricePerUnit()));
        }

        return total;
    }

    @Override
    public SendMessage message(Long chat_id, String text) {
        return new SendMessage()
                .setChatId(chat_id)
                .setText(text);
    }
}
