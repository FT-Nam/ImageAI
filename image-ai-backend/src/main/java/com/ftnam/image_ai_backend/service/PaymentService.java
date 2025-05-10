package com.ftnam.image_ai_backend.service;

import com.ftnam.image_ai_backend.dto.request.PaymentRequest;
import com.ftnam.image_ai_backend.dto.response.PaymentReturnResponse;
import com.ftnam.image_ai_backend.dto.response.PaymentCallbackResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;

public interface PaymentService {
    String createPayment(PaymentRequest request, HttpServletRequest httpServletRequest) throws UnsupportedEncodingException;

//    PaymentCallbackResponse paymentCallback(HttpServletRequest request);

    PaymentReturnResponse paymentReturn(HttpServletRequest request) throws UnsupportedEncodingException;
}
