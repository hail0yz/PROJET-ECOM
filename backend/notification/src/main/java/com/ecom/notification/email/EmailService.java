package com.ecom.notification.email;

import com.ecom.notification.kafka.order.Product;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ecom.notification.email.EmailTemplates.ORDER_CONFIRMATION;
import static com.ecom.notification.email.EmailTemplates.PAYMENT_CONFIRMATION;
import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@Slf4j
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Async
    public void sendPaymentSuccesEmail(
            String to,
            String customerName,
            BigDecimal amount,
            String OrderReference
    ) throws MessagingException {
          MimeMessage mimeMessage = mailSender.createMimeMessage();
          MimeMessageHelper mimeMessageHelper =
                new MimeMessageHelper(mimeMessage,MimeMessageHelper.MULTIPART_MODE_RELATED,UTF_8.name());
        mimeMessageHelper.setFrom("contact@souid.org");

        final String template= PAYMENT_CONFIRMATION.getTemplate();
        Map<String,Object> variables=new HashMap<>();
        variables.put("customerName",customerName);
        variables.put("amount",amount);
        variables.put("OrderReference",OrderReference);

        //thymleaf context set

        Context context = new Context();
        context.setVariables(variables);

        mimeMessageHelper.setSubject(PAYMENT_CONFIRMATION.getSubject());

        try{
            String htmlTemplate=templateEngine.process(template,context);
            mimeMessageHelper.setText(htmlTemplate,true);
            mimeMessageHelper.setTo(to);
            mailSender.send(mimeMessage);
            log.info(String.format("Email succesuly sent"));
        }
        catch(MessagingException exp){
            log.warn("cannot send message");
        }

    }

    public void sendOrderConfirmationEmail(
            String destination,
            String customerName,
            BigDecimal amount,
            String OrderReference,
            List<Product> products
    )throws MessagingException {
      MimeMessage mimeMessage= mailSender.createMimeMessage();
      MimeMessageHelper mimeMessageHelper=new MimeMessageHelper(mimeMessage,MimeMessageHelper.MULTIPART_MODE_RELATED,UTF_8.name());

      mimeMessageHelper.setFrom("contact@souid.org");

      final String template=ORDER_CONFIRMATION.getTemplate();
        Map<String,Object> variables=new HashMap<>();
        variables.put("customerName",customerName);
        variables.put("amount",amount);
        variables.put("OrderReference",OrderReference);
        variables.put("products",products);

        //thymleaf context set

        Context context = new Context();
        context.setVariables(variables);

        mimeMessageHelper.setSubject(ORDER_CONFIRMATION.getSubject());

        try{
            String htmlTemplate=templateEngine.process(template,context);
            mimeMessageHelper.setText(htmlTemplate,true);
            mimeMessageHelper.setTo(destination);
            mailSender.send(mimeMessage);
            log.info(String.format("Email succesuly sent"));
        }
        catch(MessagingException exp){
            log.warn("cannot send message");
        }

    }
}
