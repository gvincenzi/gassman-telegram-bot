package org.gassman.telegram.bot.service;

import org.gassman.telegram.bot.dto.OrderDTO;
import org.gassman.telegram.bot.dto.ProductDTO;
import org.gassman.telegram.bot.dto.UserCreditDTO;
import org.gassman.telegram.bot.dto.UserDTO;
import org.telegram.telegrambots.meta.api.objects.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ResourceManagerService {
    void deleteUser(Integer user_id);
    UserDTO findUserByTelegramId(Integer user_id);
    UserCreditDTO getCredit(Integer user_id);
    List<OrderDTO> getOrders(Integer user_id);
    OrderDTO getOrder(String call_data);
    List<ProductDTO> getProducts();
    BigDecimal totalUserCredit();
    void addUser(User user, String mail);
    List<OrderDTO> findProductOrders(Long productId);
    void postOrder(OrderDTO orderDTO);
    void putOrderInProgress(Map<Integer, OrderDTO> orderInProgess, String call_data, Integer user_id);
    String makePayment(OrderDTO orderDTO);
}
