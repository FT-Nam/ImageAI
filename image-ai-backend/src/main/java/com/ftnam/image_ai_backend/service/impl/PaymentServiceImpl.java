package com.ftnam.image_ai_backend.service.impl;

import com.ftnam.image_ai_backend.configuration.VnPayConfig;
import com.ftnam.image_ai_backend.dto.request.PaymentRequest;
import com.ftnam.image_ai_backend.dto.response.PaymentReturnResponse;
import com.ftnam.image_ai_backend.dto.response.PaymentCallbackResponse;
import com.ftnam.image_ai_backend.entity.Order;
import com.ftnam.image_ai_backend.entity.PlanInfo;
import com.ftnam.image_ai_backend.entity.User;
import com.ftnam.image_ai_backend.enums.OrderStatus;
import com.ftnam.image_ai_backend.enums.SubscriptionPlan;
import com.ftnam.image_ai_backend.exception.AppException;
import com.ftnam.image_ai_backend.exception.ErrorCode;
import com.ftnam.image_ai_backend.repository.OrderRepository;
import com.ftnam.image_ai_backend.repository.PlanInfoRepository;
import com.ftnam.image_ai_backend.repository.UserRepository;
import com.ftnam.image_ai_backend.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    VnPayConfig vnPayConfig;
    UserRepository userRepository;
    OrderRepository orderRepository;
    PlanInfoRepository planInfoRepository;

    @Override
    public String createPayment(PaymentRequest request, HttpServletRequest httpServletRequest) throws UnsupportedEncodingException {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));
        String code = vnPayConfig.getRandomNumber(8);

        Order order = Order.builder()
                .code(code)
                .user(user)
                .subscriptionPlan(request.getSubscriptionPlan())
                .amount(request.getAmount())
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        long amount = Integer.parseInt(String.valueOf(request.getAmount()))* 100L;
        String bankCode = request.getBankCode();

        String vnp_TxnRef = code;
        String vnp_IpAddr = vnPayConfig.getIpAddress(httpServletRequest);

        String vnp_TmnCode = vnPayConfig.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);

        String locate = request.getLanguage();
        if (locate != null && !locate.isEmpty()) {
            vnp_Params.put("vnp_Locale", locate);
        } else {
            vnp_Params.put("vnp_Locale", "vn");
        }
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = vnPayConfig.hmacSHA512(vnPayConfig.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnPayConfig.vnp_PayUrl + "?" + queryUrl;
        return  paymentUrl;
    }

//    // IPN VNPAY
//    @Transactional
//    public PaymentCallbackResponse paymentCallback(HttpServletRequest request){
//        try
//        {
//            // ex:  	PaymnentStatus = 0; pending
//            //              PaymnentStatus = 1; success
//            //              PaymnentStatus = 2; Faile
//
//            //Begin process return from VNPAY
//            Map fields = new HashMap();
//            for (Enumeration params = request.getParameterNames(); params.hasMoreElements();) {
//                String fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
//                String fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
//                if ((fieldValue != null) && (fieldValue.length() > 0)) {
//                    fields.put(fieldName, fieldValue);
//                }
//            }
//
//            String vnp_SecureHash = request.getParameter("vnp_SecureHash");
//            if (fields.containsKey("vnp_SecureHashType"))
//            {
//                fields.remove("vnp_SecureHashType");
//            }
//            if (fields.containsKey("vnp_SecureHash"))
//            {
//                fields.remove("vnp_SecureHash");
//            }
//
//            // Check checksum
//            String signValue = vnPayConfig.hashAllFields(fields);
//            if (signValue.equals(vnp_SecureHash))
//            {
//                String orderCode = request.getParameter("vnp_TxnRef");
//                Order order = orderRepository.findById(orderCode).orElse(null);
//
//                boolean checkOrderId = order != null; // vnp_TxnRef exists in your database
//                boolean checkAmount = checkOrderId && (order.getAmount() * 100L == Long.parseLong(request.getParameter("vnp_Amount"))); // vnp_Amount is valid (Check vnp_Amount VNPAY returns compared to the
//                // amount of the code (vnp_TxnRef) in the Your database).
//                boolean checkOrderStatus = checkOrderId && order.getStatus() == OrderStatus.PENDING; // PaymnentStatus = 0 (pending)
//
//
//                if(checkOrderId)
//                {
//                    if(checkAmount)
//                    {
//                        if (checkOrderStatus)
//                        {
//                            if ("00".equals(request.getParameter("vnp_ResponseCode")))
//                            {
//                                //Here Code update PaymnentStatus = 1 into your Database
//                                User user = order.getUser();
//                                SubscriptionPlan plan = order.getSubscriptionPlan();
//
//                                PlanInfo planInfo = planInfoRepository.findBySubscription(plan)
//                                        .orElseThrow(()-> new AppException(ErrorCode.SUBSCRIPTION_NOT_EXISTED));
//
//                                user.setSubscription(plan);
//                                user.setCredit(user.getCredit() + planInfo.getWeeklyCredit());
//                                user.setSubscriptionExpiredAt(LocalDateTime.now().plusMonths(1));
//                                user.setCreditResetAt(LocalDateTime.now());
//
//                                order.setStatus(OrderStatus.SUCCESS);
//
//                                userRepository.save(user);
//                                orderRepository.save(order);
//                            }
//                            else
//                            {
//                                // Here Code update PaymnentStatus = 2 into your Database
//                                order.setStatus(OrderStatus.FAILED);
//                                orderRepository.save(order);
//                            }
//                            return PaymentCallbackResponse.builder()
//                                    .message("Confirm Success")
//                                    .rspCode("00")
//                                    .build();
//                        }
//                        else
//                        {
//                            return PaymentCallbackResponse.builder()
//                                    .rspCode("02")
//                                    .message("Order already confirmed")
//                                    .build();
//                        }
//                    }
//                    else
//                    {
//                        return PaymentCallbackResponse.builder()
//                                .rspCode("04")
//                                .message("Invalid Amount")
//                                .build();
//                    }
//                }
//                else
//                {
//                    return PaymentCallbackResponse.builder()
//                            .rspCode("01")
//                            .message("Order not Found")
//                            .build();
//                }
//            }
//            else
//            {
//                return PaymentCallbackResponse.builder()
//                        .message("Invalid Checksum")
//                        .rspCode("97")
//                        .build();
//            }
//        }
//        catch(Exception e)
//        {
//            log.error("Callback error: ", e);
//            return PaymentCallbackResponse.builder()
//                    .rspCode("99")
//                    .message("Unknow error")
//                    .build();
//        }
//    }

    @Override
    public PaymentReturnResponse paymentReturn(HttpServletRequest request) throws UnsupportedEncodingException {
        Map fields = new HashMap();


        for (Enumeration params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = null;
            String fieldValue = null;
            try {
                fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
                fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }
        String signValue = vnPayConfig.hashAllFields(fields);

        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
                String orderCode = request.getParameter("vnp_TxnRef");
                Order order = orderRepository.findById(orderCode)
                        .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

                User user = order.getUser();
                SubscriptionPlan plan = order.getSubscriptionPlan();

                PlanInfo planInfo = planInfoRepository.findBySubscription(plan)
                      .orElseThrow(()-> new AppException(ErrorCode.SUBSCRIPTION_NOT_EXISTED));

                user.setSubscription(plan);
                user.setCredit(user.getCredit() + planInfo.getWeeklyCredit());
                user.setSubscriptionExpiredAt(LocalDateTime.now().plusMonths(1));
                user.setCreditResetAt(LocalDateTime.now());

                order.setStatus(OrderStatus.SUCCESS);

                userRepository.save(user);
                orderRepository.save(order);

                return PaymentReturnResponse.builder()
                        .success(true)
                        .message("Successful transaction")
                        .responseCode(request.getParameter("vnp_ResponseCode"))
                        .transactionCode(request.getParameter("vnp_TxnRef"))
                        .build();
            } else {
                String orderCode = request.getParameter("vnp_TxnRef");
                Order order = orderRepository.findById(orderCode)
                        .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

                order.setStatus(OrderStatus.FAILED);
                orderRepository.save(order);

                return PaymentReturnResponse.builder()
                        .success(false)
                        .message("Successful failed")
                        .responseCode(request.getParameter("vnp_ResponseCode"))
                        .transactionCode(request.getParameter("vnp_TxnRef"))
                        .build();
            }

        } else {
            String orderCode = request.getParameter("vnp_TxnRef");
            Order order = orderRepository.findById(orderCode)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);

            return PaymentReturnResponse.builder()
                    .success(false)
                    .message("Invalid signature")
                    .responseCode(request.getParameter("vnp_ResponseCode"))
                    .transactionCode(request.getParameter("vnp_TxnRef"))
                    .build();
        }
    }
}
