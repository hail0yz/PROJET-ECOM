package org.ecom.cart.mapper;

import org.ecom.cart.dto.GetCartResponse;
import org.ecom.cart.model.Cart;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface CartMapper {

    GetCartResponse mapCart(Cart cart);

}
