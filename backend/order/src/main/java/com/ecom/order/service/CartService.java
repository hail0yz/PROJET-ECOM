package com.ecom.order.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.ecom.order.cart.CartDetails;
import com.ecom.order.exception.BadREquestException;
import com.ecom.order.exception.EntityNotFoundException;
import com.ecom.order.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final RestClient.Builder restClient;

    public CartDetails getCartById(Long cartId) {
        log.info("Getting cart by id {}", cartId);
        return restClient.build().get()
                .uri("http://cart-service/api/v1/carts/{cartId}", cartId)
                .retrieve()
                .onStatus(httpStatusCode -> !httpStatusCode.isSameCodeAs(HttpStatus.OK), (request, response) -> {
                    if (response.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
                        log.error("Cart not found (cartId={})", cartId);
                        throw new EntityNotFoundException("Cart not found with id : " + cartId);
                    }
                    if (response.getStatusCode().is5xxServerError()) {
                        log.error("Failed to get cart : Server Error " + response);
                        throw new ExternalServiceException("Customer service unavailable");
                    }

                    throw new BadREquestException();
                })
                .body(CartDetails.class);
    }

    public void completeCart(Long cartId) {
        log.info("Completing cart : {}", cartId);
        restClient.build().patch()
                .uri("http://cart-service/api/v1/carts/{cartId}/complete", cartId)
                .retrieve()
                .onStatus(httpStatusCode -> !httpStatusCode.isSameCodeAs(HttpStatus.OK), (request, response) -> {
                    if (response.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
                        log.error("Cart not found (cartId={})", cartId);
                        throw new EntityNotFoundException("Cart not found with id : " + cartId);
                    }
                    if (response.getStatusCode().is5xxServerError()) {
                        log.error("Failed to get cart : Server Error " + response);
                        throw new ExternalServiceException("Customer service unavailable");
                    }

                    throw new BadREquestException();
                })
                .toBodilessEntity();
    }

}
