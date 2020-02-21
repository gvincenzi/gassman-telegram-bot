package org.gassman.telegram.bot.polling.factory;

import org.gassman.telegram.bot.dto.ProductDTO;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.math.BigDecimal;

public interface ItemFactory {
    SendMessage welcomeMessage(Update update);
    BigDecimal computeTotalAmountSupplier(ProductDTO productDTO);
    SendMessage message(Long chat_id, String text);
}
