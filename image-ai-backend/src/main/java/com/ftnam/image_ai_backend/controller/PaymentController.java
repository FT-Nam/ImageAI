package com.ftnam.image_ai_backend.controller;

import com.ftnam.image_ai_backend.dto.request.PaymentRequest;
import com.ftnam.image_ai_backend.dto.response.ApiResponse;
import com.ftnam.image_ai_backend.dto.response.PaymentReturnResponse;
import com.ftnam.image_ai_backend.dto.response.PaymentCallbackResponse;
import com.ftnam.image_ai_backend.service.impl.PaymentServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    PaymentServiceImpl paymentService;

    @PostMapping("/vnpay/create")
    ApiResponse<String> createPayment(@RequestBody PaymentRequest request, HttpServletRequest httpServletRequest)
            throws UnsupportedEncodingException {
        return ApiResponse.<String>builder()
                .message("Success")
                .value(paymentService.createPayment(request,httpServletRequest))
                .build();
    }

//    @GetMapping("/vnpay/callback")
//    ApiResponse<PaymentCallbackResponse> paymentCallback(HttpServletRequest request){
//        return ApiResponse.<PaymentCallbackResponse>builder()
//                .value(paymentService.paymentCallback(request))
//                .build();
//    }

    @GetMapping("/vnpay/return-url")
    ApiResponse<PaymentReturnResponse> paymentReturn(HttpServletRequest request)
            throws UnsupportedEncodingException {
        return ApiResponse.<PaymentReturnResponse>builder()
                .value(paymentService.paymentReturn(request))
                .build();
    }
}
