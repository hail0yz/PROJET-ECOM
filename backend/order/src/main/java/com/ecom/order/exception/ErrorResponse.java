package com.ecom.order.exception;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;


@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    Map<String,String> errors;
}
