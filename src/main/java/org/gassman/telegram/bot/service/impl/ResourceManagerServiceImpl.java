package org.gassman.telegram.bot.service.impl;

import feign.FeignException;
import org.gassman.telegram.bot.client.OrderResourceClient;
import org.gassman.telegram.bot.client.ProductResourceClient;
import org.gassman.telegram.bot.client.UserCreditResourceClient;
import org.gassman.telegram.bot.client.UserResourceClient;
import org.gassman.telegram.bot.dto.OrderDTO;
import org.gassman.telegram.bot.dto.ProductDTO;
import org.gassman.telegram.bot.dto.UserCreditDTO;
import org.gassman.telegram.bot.dto.UserDTO;
import org.gassman.telegram.bot.service.ResourceManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class ResourceManagerServiceImpl implements ResourceManagerService {
    @Autowired
    private UserResourceClient userResourceClient;

    @Autowired
    private UserCreditResourceClient userCreditResourceClient;

    @Autowired
    private ProductResourceClient productResourceClient;

    @Autowired
    private OrderResourceClient orderResourceClient;

    public UserDTO findUserByTelegramId(Integer user_id) {
        UserDTO user;
        try {
            user = userResourceClient.findUserByTelegramId(user_id);
        } catch (FeignException ex) {
            user = null;
        }
        return user;
    }

    @Override
    public UserCreditDTO getCredit(Integer user_id) {
        UserDTO user = findUserByTelegramId(user_id);
        return userCreditResourceClient.findById(user.getId());
    }

    @Override
    public List<OrderDTO> getOrders(Integer user_id) {
        UserDTO userDTO = findUserByTelegramId(user_id);
        return orderResourceClient.findAllOrdersByUser(userDTO.getId());
    }

    @Override
    public OrderDTO getOrder(String call_data) {
        String[] split = call_data.split("#");
        Long orderId = Long.parseLong(split[1]);
        return orderResourceClient.findOrderById(orderId);
    }

    @Override
    public List<ProductDTO> getProducts() {
        return productResourceClient.findAll();
    }

    @Override
    public BigDecimal totalUserCredit() {
        return userCreditResourceClient.totalUserCredit();
    }

    @Override
    public void addUser(User from, String mail) {
        UserDTO userDTO = new UserDTO();
        userDTO.setTelegramUserId(from.getId());
        userDTO.setName(from.getFirstName());
        userDTO.setSurname(from.getLastName());
        userDTO.setMail(mail);
        userDTO.setAdministrator(Boolean.FALSE);
        userResourceClient.addUser(userDTO);
    }

    @Override
    public List<OrderDTO> findProductOrders(Long productId) {
        return productResourceClient.findProductOrders(productId);
    }

    @Override
    public void postOrder(OrderDTO orderDTO) {
        orderResourceClient.postOrder(orderDTO);
    }

    @Override
    public void putOrderInProgress(Map<Integer, OrderDTO> orderInProgess, String call_data, Integer user_id) {
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
    }

    @Override
    public void deleteUser(Integer user_id) {
        userResourceClient.deleteUser(user_id);
    }
}
