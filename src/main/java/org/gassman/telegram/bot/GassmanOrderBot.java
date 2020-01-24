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
import java.util.*;

@Component
public class GassmanOrderBot extends TelegramLongPollingBot {
    @Value("${gassman.telegram.bot.username}")
    private String botUsername;

    @Value("${gassman.telegram.bot.token}")
    private String botToken;

    @Value("${gassman.template.paymentInternalCreditURL}")
    public String templatePaymentInternalCreditURL;

    @Autowired
    private UserResourceClient userResourceClient;

    @Autowired
    private UserCreditResourceClient userCreditResourceClient;

    @Autowired
    private ProductResourceClient productResourceClient;

    @Autowired
    private OrderResourceClient orderResourceClient;

    List<ProductDTO> products;

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
                UserDTO user = findUserByTelegramId(user_id);
                UserCreditDTO userCreditDTO = userCreditResourceClient.findById(user.getId());
                message = new SendMessage()
                        .setChatId(chat_id)
                        .setText(String.format("Il tuo credito residuo : %s €", userCreditDTO.getCredit()));
            } else if (call_data.startsWith("listaOrdini")) {
                UserDTO userDTO = findUserByTelegramId(user_id);
                List<OrderDTO> orders = orderResourceClient.findAllOrdersByUser(userDTO.getId());
                if (orders.isEmpty()) {
                    message = new SendMessage()
                            .setChatId(chat_id)
                            .setText("Non hai ordini in corso");
                } else {
                    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                    Collections.sort(orders);
                    for (OrderDTO orderDTO : orders) {
                        List<InlineKeyboardButton> rowInline = new ArrayList<>();
                        rowInline.add(new InlineKeyboardButton().setText("ID#"+orderDTO.getOrderId()+" : "+orderDTO.getProduct().getName()).setCallbackData("orderDetails#" + orderDTO.getOrderId()));
                        rowsInline.add(rowInline);
                    }

                    markupInline.setKeyboard(rowsInline);
                    message = new SendMessage()
                            .setChatId(chat_id)
                            .setText("Qui di seguito la lista dei tuoi ordini in corso, per accedere ai dettagli cliccare sull'ordine:\n");

                    message.setReplyMarkup(markupInline);
                }
            } else if (call_data.startsWith("orderDetails#")) {
                String[] split = call_data.split("#");
                Long orderId = Long.parseLong(split[1]);
                OrderDTO orderDTO = orderResourceClient.findOrderById(orderId);
                message = new SendMessage()
                    .setChatId(chat_id)
                    .setText(orderDTO.toString());
                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
                List<InlineKeyboardButton> rowInline2 = new ArrayList<>();

                String paymentInternalCreditURL = String.format(templatePaymentInternalCreditURL,orderDTO.getOrderId()).replaceAll(" ","%20");
                rowInline1.add(new InlineKeyboardButton().setText("Paga questo ordine").setUrl(paymentInternalCreditURL));
                rowInline2.add(new InlineKeyboardButton().setText("Torna alla lista").setCallbackData("listaOrdini"));
                // Set the keyboard to the markup
                if(!orderDTO.getPaid()){
                    rowsInline.add(rowInline1);
                }

                rowsInline.add(rowInline2);
                // Add it to the message
                markupInline.setKeyboard(rowsInline);
                message.setReplyMarkup(markupInline);
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
            } else if (call_data.equals("fondoCassa")) {
                message = new SendMessage()
                        .setChatId(chat_id)
                        .setText("Fondo cassa corrente : " + userCreditResourceClient.totalUserCredit() + "€");
            }
        }

        if (update.hasMessage() && !orderInProgess.isEmpty() && orderInProgess.get(update.getMessage().getFrom().getId()) != null) {
            OrderDTO orderDTO = orderInProgess.remove(update.getMessage().getFrom().getId());
            orderDTO.setQuantity(Integer.parseInt(update.getMessage().getText()));
            try {
                orderResourceClient.postOrder(orderDTO);
                message = new SendMessage()
                        .setChatId(update.getMessage().getChatId())
                        .setText("Ordine finalizzato correttamente, una mail di conferma con una sintesi dell'acquisto e le modalità di pagamento è stata inviata sul tuo indirizzo email.");

                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
                List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
                rowInline1.add(new InlineKeyboardButton().setText("Lista dei tuoi ordini attivi").setCallbackData("listaOrdini"));
                rowInline2.add(new InlineKeyboardButton().setText("Torna alla lista dei prodotti").setCallbackData("listaProdotti"));

                // Set the keyboard to the markup
                rowsInline.add(rowInline1);
                rowsInline.add(rowInline2);
                // Add it to the message
                markupInline.setKeyboard(rowsInline);
                message.setReplyMarkup(markupInline);

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
                    .setText("Nuovo utente iscritto correttamente : una mail di conferma è stata inviata all'indirizzo specificato.\nClicca su /start per iniziare.");

        } else if (update.hasMessage()){
            message = welcomeMessage(update);
        }

        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
        }
    }

    private UserDTO findUserByTelegramId(Integer user_id) {
        UserDTO user;
        try {
            user = userResourceClient.findUserByTelegramId(user_id);
        } catch (FeignException ex) {
            user = null;
        }
        return user;
    }

    private SendMessage welcomeMessage(Update update) {
        UserDTO user = findUserByTelegramId(update.getMessage().getFrom().getId());
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
        if(user == null){
            rowInline1.add(new InlineKeyboardButton().setText("Iscrizione").setCallbackData("iscrizione"));
        } else {
            rowInline1.add(new InlineKeyboardButton().setText("Cancellazione").setCallbackData("cancellazione"));
            rowInline1.add(new InlineKeyboardButton().setText("Credito residuo").setCallbackData("creditoResiduo"));
            rowInline2.add(new InlineKeyboardButton().setText("I tuoi ordini").setCallbackData("listaOrdini"));
            rowInline3.add(new InlineKeyboardButton().setText("Lista dei prodotti").setCallbackData("listaProdotti"));
            rowInline4.add(new InlineKeyboardButton().setText("Fondo cassa").setCallbackData("fondoCassa"));
        }

        // Set the keyboard to the markup
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        rowsInline.add(rowInline3);
        if(user != null && user.getAdministrator()){
            rowsInline.add(rowInline4);
        }
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
