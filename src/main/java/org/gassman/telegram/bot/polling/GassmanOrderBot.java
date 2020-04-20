package org.gassman.telegram.bot.polling;

import feign.FeignException;
import org.gassman.telegram.bot.dto.OrderDTO;
import org.gassman.telegram.bot.dto.ProductDTO;
import org.gassman.telegram.bot.dto.UserDTO;
import org.gassman.telegram.bot.polling.factory.ItemFactory;
import org.gassman.telegram.bot.service.ResourceManagerService;
import org.gassman.telegram.bot.service.TelegramAdministratorService;
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

import java.text.NumberFormat;
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
    ResourceManagerService resourceManagerService;

    @Autowired
    TelegramAdministratorService telegramAdministratorService;

    @Autowired
    ItemFactory itemFactory;

    Map<Integer, OrderDTO> orderInProgess = new HashMap<>();

    List<ProductDTO> products;

    @Override
    public void onUpdateReceived(Update update) {
        SendMessage message = null;
        if (update.hasMessage() && update.getMessage().getText().equalsIgnoreCase("/start")) {
            message = itemFactory.welcomeMessage(update);

        } else if (update.hasCallbackQuery()) {
            // Set variables
            String call_data = update.getCallbackQuery().getData();
            Integer user_id = update.getCallbackQuery().getFrom().getId();
            long chat_id = update.getCallbackQuery().getMessage().getChatId();

            if (call_data.equals("iscrizione")) {
                message = itemFactory.message(chat_id,"Per iscriversi al sistema basta scrivere un messaggio in questa chat con solo la propria email.\nGasSMan vi iscriverà al sistema con i dati del vostro account Telegram e con la mail che avrete indicato");
            } else if (call_data.equals("cancellazione")) {
                resourceManagerService.deleteUser(user_id);
                message = itemFactory.message(chat_id,"Utente rimosso correttamente");
            } else if (call_data.equals("creditoResiduo")) {
                message = itemFactory.message(chat_id,String.format("Il tuo credito residuo : %s €", resourceManagerService.getCredit(user_id).getCredit()));
            } else if (call_data.startsWith("listaOrdini")) {
                List<OrderDTO> orders = resourceManagerService.getOrders(user_id);
                if (orders.isEmpty()) {
                    message = itemFactory.message(chat_id,"Non hai ordini in corso");
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
                    message = itemFactory.message(chat_id,"Qui di seguito la lista dei tuoi ordini in corso, per accedere ai dettagli cliccare sull'ordine:\n");

                    message.setReplyMarkup(markupInline);
                }
            } else if (call_data.startsWith("orderDetails#")) {
                OrderDTO orderDTO = resourceManagerService.getOrder(call_data);
                message = itemFactory.message(chat_id,orderDTO.toString());
                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
                List<InlineKeyboardButton> rowInline2 = new ArrayList<>();

                String paymentInternalCreditURL = String.format(templatePaymentInternalCreditURL,orderDTO.getOrderId()).replaceAll(" ","%20");
                // Payment via URL in 1.1.0 version
                // rowInline1.add(new InlineKeyboardButton().setText("Paga questo ordine").setUrl(paymentInternalCreditURL));
                rowInline1.add(new InlineKeyboardButton().setText("Paga questo ordine : "+ NumberFormat.getCurrencyInstance().format(orderDTO.getTotalToPay())).setCallbackData("makePayment#"+orderDTO.getOrderId()));
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
                this.products = resourceManagerService.getProducts();
                if (products.isEmpty()) {
                    message = itemFactory.message(chat_id,"Non ci sono prodotti attualmente disponibili");
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
                    message = itemFactory.message(chat_id,"Qui di seguito la lista dei prodotti attualmente disponibili, per accedere ai dettagli e ordinare cliccare sul prodotto:\n");
                    message.setReplyMarkup(markupInline);
                }
            } else if (call_data.startsWith("product#")) {
                String[] split = call_data.split("#");
                Long productId = Long.parseLong(split[1]);
                for (ProductDTO productDTO : products) {
                    if (productId.equals(productDTO.getProductId())) {
                        message = itemFactory.message(chat_id,productDTO.toString());
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
            } else if (call_data.startsWith("makePayment#")) {
                OrderDTO orderDTO = resourceManagerService.getOrder(call_data);
                message = itemFactory.message(chat_id, resourceManagerService.makePayment(orderDTO));
            } else if (call_data.startsWith("order#")) {
                resourceManagerService.putOrderInProgress(orderInProgess,call_data,user_id);
                message = itemFactory.message(chat_id,"Ordine creato correttamente, inviare un ulteriore messaggio indicando solo la quantità desiderata (solo il valore numerico, senza unità di misura) per finalizzare l'ordine");
            } else if (call_data.equals("fondoCassa")) {
                message = itemFactory.message(chat_id,"Fondo cassa corrente : " + resourceManagerService.totalUserCredit() + "€");
            } else if (call_data.equals("totaliFornitori")) {
                this.products = resourceManagerService.getProducts();
                if (products.isEmpty()) {
                    message = itemFactory.message(chat_id,"Non ci sono prodotti attualmente attivi");
                } else {
                    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                    Collections.sort(products);
                    String productLines = "";
                    for (ProductDTO productDTO : products) {
                        List<InlineKeyboardButton> rowInline = new ArrayList<>();
                        productLines += "\n"+productDTO.getName() + " : " + itemFactory.computeTotalAmountSupplier(productDTO) + " €";
                        rowsInline.add(rowInline);
                    }

                    markupInline.setKeyboard(rowsInline);
                    message = itemFactory.message(chat_id,"Qui di seguito la lista dei prodotti attualmente attivi con il totale dovuto al fornitore associato:\n" + productLines);
                    message.setReplyMarkup(markupInline);
                }
            } else if (call_data.equals("advertising")) {
                try {
                    telegramAdministratorService.advertising("Ciao %s, ti invito a cliccare su /start e visionare le novità presenti nella lista prodotti.\nNon dimenticare di controllare il tuo credito residuo prima di fare un acquisto.\nBuona giornata");
                    message = itemFactory.message(chat_id,"Invio dell'avviso effettuato con successo");
                } catch (TelegramApiException e) {
                    message = itemFactory.message(chat_id,"Errore nell'invio del messaggio");
                }
            }
        }

        if (update.hasMessage() && !orderInProgess.isEmpty() && orderInProgess.get(update.getMessage().getFrom().getId()) != null) {
            Long chat_id = update.getMessage().getChatId();
            OrderDTO orderDTO = orderInProgess.remove(update.getMessage().getFrom().getId());
            orderDTO.setQuantity(Integer.parseInt(update.getMessage().getText()));
            try {
                resourceManagerService.postOrder(orderDTO);
                message = itemFactory.message(chat_id,"Ordine finalizzato correttamente, una mail di conferma con una sintesi dell'acquisto e le modalità di pagamento è stata inviata sul tuo indirizzo email.");

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
                    message = itemFactory.message(chat_id,"Errore nella finalizzazione dell'ordine : la quantità richiesta non è disponibile.\nRipetere l'ordine tenendo conto della quantità disponibile.");
                } else {
                    message = itemFactory.message(chat_id,"Errore nella finalizzazione dell'ordine : " + e.getMessage());
                }

            }
        } else if (update.hasMessage() && update.getMessage().getText().contains("@")) {
            Long chat_id = update.getMessage().getChatId();
            resourceManagerService.addUser(update.getMessage().getFrom(),update.getMessage().getText());
            message = itemFactory.message(chat_id,"Nuovo utente iscritto correttamente : una mail di conferma è stata inviata all'indirizzo specificato.\nClicca su /start per iniziare.");

        } else if (update.hasMessage()){
            message = itemFactory.welcomeMessage(update);
        }

        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
        }
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
