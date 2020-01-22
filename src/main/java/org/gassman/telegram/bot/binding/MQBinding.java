package org.gassman.telegram.bot.binding;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface MQBinding {
    String USER_REGISTRATION = "userRegistrationChannel";
    String USER_ORDER = "userOrderChannel";
    String ORDER_PAYMENT_CONFIRMATION = "orderPaymentConfirmationChannel";

    @Input(USER_REGISTRATION)
    SubscribableChannel userRegistrationChannel();

    @Input(USER_ORDER)
    SubscribableChannel userOrderChannel();

    @Input(ORDER_PAYMENT_CONFIRMATION)
    SubscribableChannel orderPaymentConfirmationChannel();
}
