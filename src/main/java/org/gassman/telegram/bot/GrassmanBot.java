package org.gassman.telegram.bot;

import feign.FeignException;
import org.gassman.telegram.bot.client.OrderResourceClient;
import org.gassman.telegram.bot.client.ProductResourceClient;
import org.gassman.telegram.bot.client.UserCreditResourceClient;
import org.gassman.telegram.bot.client.UserResourceClient;
import org.gassman.telegram.bot.dto.OrderDTO;
import org.gassman.telegram.bot.dto.ProductDTO;
import org.gassman.telegram.bot.dto.UserCreditDTO;
import org.gassman.telegram.bot.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class GrassmanBot extends TelegramLongPollingBot {
    @Value("${gassman.telegram.bot.username}")
    private String botUsername;

    @Value("${gassman.telegram.bot.token}")
    private String botToken;

    @Autowired
    private UserResourceClient userResourceClient;

    @Autowired
    private UserCreditResourceClient userCreditResourceClient;

    @Autowired
    private ProductResourceClient productResourceClient;

    @Autowired
    private OrderResourceClient orderResourceClient;

    List<ProductDTO> products;

    LocalDateTime lastProductsUpdate;

    Map<Integer, OrderDTO> orderInProgess = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        SendMessage message = null;
        if (update.hasMessage() && update.getMessage().getText().equalsIgnoreCase("/start")) {
            message = welcomeMessage(update);

        } else if (update.hasCallbackQuery()) {
            // Set variables
            String call_data = update.getCallbackQuery().getData();
            Integer user_id = update.getCallbackQuery().getFrom().getId();
            long chat_id = update.getCallbackQuery().getMessage().getChatId();

            if (call_data.equals("iscrizione")) {
                message = new SendMessage()
                        .setChatId(chat_id)
                        .setText("Per iscriversi al sistema basta scrivere un messaggio in questa chat con solo la propria email.\nGasSMan vi iscriverà al sistema con i dati del vostro account Telegram e con la mail che avrete indicato");
            } else if (call_data.equals("cancellazione")) {
                userResourceClient.deleteUser(user_id);
                message = new SendMessage()
                        .setChatId(chat_id)
                        .setText("Utente rimosso correttamente");
            } else if (call_data.equals("creditoResiduo")) {
                UserDTO user;
                try {
                    user = userResourceClient.findUserByTelegramId(user_id);
                } catch (FeignException ex) {
                    user = null;
                }
                UserCreditDTO userCreditDTO = userCreditResourceClient.findById(user.getId());
                message = new SendMessage()
                        .setChatId(chat_id)
                        .setText(String.format("Il tuo credito residuo : %s €", userCreditDTO.getCredit()));
            } else if (call_data.equals("listaProdotti")) {
                this.products = productResourceClient.findAll();
                if (products.isEmpty()) {
                    message = new SendMessage()
                            .setChatId(chat_id)
                            .setText("Non ci sono prodotti attualmente disponibili");
                } else {
                    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                    Collections.sort(products);
                    for (ProductDTO productDTO : products) {
                        List<InlineKeyboardButton> rowInline = new ArrayList<>();
                        rowInline.add(new InlineKeyboardButton().setText(productDTO.getName()).setCallbackData("product#" + productDTO.getProductId()));
                        rowsInline.add(rowInline);
                    }

                    markupInline.setKeyboard(rowsInline);
                    message = new SendMessage()
                            .setChatId(chat_id)
                            .setText("Qui di seguito la lista dei prodotti attualmente disponibili, per accedere ai dettagli e ordinare cliccare sul prodotto:\n");

                    message.setReplyMarkup(markupInline);
                }
            } else if (call_data.startsWith("product#")) {
                String[] split = call_data.split("#");
                Long productId = Long.parseLong(split[1]);
                for (ProductDTO productDTO : products) {
                    if (productId.equals(productDTO.getProductId())) {
                        message = new SendMessage()
                                .setChatId(chat_id)
                                .setText(productDTO.toString());
                    }
                }
                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline = new ArrayList<>();
                rowInline.add(new InlineKeyboardButton().setText("Ordina questo prodotto").setCallbackData("order#" + productId));
                rowInline.add(new InlineKeyboardButton().setText("Torna alla lista").setCallbackData("listaProdotti"));
                // Set the keyboard to the markup
                rowsInline.add(rowInline);
                // Add it to the message
                markupInline.setKeyboard(rowsInline);
                message.setReplyMarkup(markupInline);
            } else if (call_data.startsWith("order#")) {
                String[] split = call_data.split("#");
                Long productId = Long.parseLong(split[1]);
                OrderDTO orderDTO = new OrderDTO();
                ProductDTO productDTO = new ProductDTO();
                productDTO.setProductId(productId);
                UserDTO userDTO = new UserDTO();
                userDTO.setTelegramUserId(user_id);
                orderDTO.setProduct(productDTO);
                orderDTO.setUser(userDTO);
                orderInProgess.put(user_id, orderDTO);

                message = new SendMessage()
                        .setChatId(chat_id)
                        .setText("Ordine creato correttamente, inviare un ulteriore messaggio indicando solo la quantità desiderata (solo il valore numerico, senza unità di misura) per finalizzare l'ordine");
            }
        }

        if (update.hasMessage() && !orderInProgess.isEmpty() && orderInProgess.get(update.getMessage().getFrom().getId()) != null) {
            OrderDTO orderDTO = orderInProgess.remove(update.getMessage().getFrom().getId());
            orderDTO.setQuantity(Integer.parseInt(update.getMessage().getText()));
            try {
                orderResourceClient.postOrder(orderDTO);
                message = new SendMessage()
                        .setChatId(update.getMessage().getChatId())
                        .setText("Ordine finalizzato correttamente, una mail di conferma è stata inviata sul tuo indirizzo email");
            } catch (FeignException e) {
                if (e.status() == HttpStatus.NOT_ACCEPTABLE.value()) {
                    message = new SendMessage()
                            .setChatId(update.getMessage().getChatId())
                            .setText("Errore nella finalizzazione dell'ordine : la quantità richiesta non è disponibile.\nRipetere l'ordine tenendo conto della quantità disponibile.");
                } else {
                    message = new SendMessage()
                            .setChatId(update.getMessage().getChatId())
                            .setText("Errore nella finalizzazione dell'ordine : " + e.getMessage());
                }

            }
        } else if (update.hasMessage() && update.getMessage().getText().contains("@")) {
            UserDTO userDTO = new UserDTO();
            userDTO.setTelegramUserId(update.getMessage().getFrom().getId());
            userDTO.setName(update.getMessage().getFrom().getFirstName());
            userDTO.setSurname(update.getMessage().getFrom().getLastName());
            userDTO.setMail(update.getMessage().getText());

            userResourceClient.addUser(userDTO);
            message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText("Nuovo utente iscritto correttamente : una mail di conferma è stata inviata all'indirizzo specificato");

        } else if (update.hasMessage()){
            message = welcomeMessage(update);
        }

        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
        }
    }

    private SendMessage welcomeMessage(Update update) {
        UserDTO user;
        try {
            user = userResourceClient.findUserByTelegramId(update.getMessage().getFrom().getId());
        } catch (FeignException ex) {
            user = null;
        }
        SendMessage message;
        message = new SendMessage()
                .setChatId(update.getMessage().getChatId())
                .setText(String.format("%s,\nScegli tra le seguenti opzioni:",user == null ? "Benvenuto nel sistema GasSMan" : "Bentornato " + user.getName()));

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        if(user == null){
            rowInline1.add(new InlineKeyboardButton().setText("Iscrizione").setCallbackData("iscrizione"));
        } else {
            rowInline1.add(new InlineKeyboardButton().setText("Cancellazione").setCallbackData("cancellazione"));
            rowInline1.add(new InlineKeyboardButton().setText("Credito residuo").setCallbackData("creditoResiduo"));
            rowInline2.add(new InlineKeyboardButton().setText("Lista dei prodotti").setCallbackData("listaProdotti"));
        }

        // Set the keyboard to the markup
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        return message;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
