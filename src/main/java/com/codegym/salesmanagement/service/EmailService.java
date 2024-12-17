package com.codegym.salesmanagement.service;

import com.codegym.salesmanagement.formatter.DateFormatter;
import com.codegym.salesmanagement.formatter.PriceFormatter;
import com.codegym.salesmanagement.model.Order;
import com.codegym.salesmanagement.model.OrderItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private OrderService orderService;

    public void sendVerificationEmail(String to, String link) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Xác nhận tài khoản");
        message.setText("Vui lòng nhấp vào liên kết dưới đây để xác nhận tài khoản của bạn:\n" + link);
        mailSender.send(message);
    }

    public void sendResetPasswordEmail(String to, String resetLink) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        mailMessage.setSubject("Đặt lại mật khẩu");
        mailMessage.setText("Click vào link sau để đặt lại mật khẩu: " + resetLink);
        mailSender.send(mailMessage);
    }

    public void sendOrderConfirmationEmail(String to, Order order) {
        String subject = "Xác nhận đơn hàng của bạn";
        StringBuilder emailContent = new StringBuilder();

        LocalDateTime orderDate = order.getOrderDate();
        String formattedOrderDate = DateFormatter.formatDateTime(orderDate);

        emailContent.append("<h3>Thông tin đơn hàng của bạn</h3>");
        emailContent.append("<p><strong>Tên người nhận:</strong> ").append(order.getUser().getFullname()).append("</p>");
        emailContent.append("<p><strong>Địa chỉ giao hàng:</strong> ").append(order.getUser().getAddress()).append("</p>");
        emailContent.append("<p><strong>Số điện thoại:</strong> ").append(order.getUser().getPhone()).append("</p>");
        emailContent.append("<p><strong>Email:</strong> ").append(order.getUser().getEmail()).append("</p>");
        emailContent.append("<p><strong>Ngày đặt hàng:</strong> ").append(formattedOrderDate).append("</p>");

        emailContent.append("<h4>Chi tiết đơn hàng:</h4>");
        emailContent.append("<table border='1' cellpadding='5' cellspacing='0'>");
        emailContent.append("<tr><th>Sản phẩm</th><th>Số lượng</th><th>Giá</th><th>Tổng</th></tr>");

        for (OrderItem orderItem : order.getOrderItems()) {
            emailContent.append("<tr>");
            emailContent.append("<td>").append(orderItem.getProduct().getProductName()).append("</td>");
            emailContent.append("<td>").append(orderItem.getQuantity()).append("</td>");
            emailContent.append("<td>").append(PriceFormatter.formatPrice(orderItem.getPrice())).append("</td>");
            emailContent.append("<td>").append(PriceFormatter.formatPrice(orderItem.getTotal())).append("</td>");
            emailContent.append("</tr>");
        }
        emailContent.append("</table>");
        emailContent.append("<h4>Tổng giá trị đơn hàng: ").append(PriceFormatter.formatPrice(order.getTotalAmount())).append("</h4>");
        emailContent.append("<p><strong>Phương thức thanh toán:</strong> ").append(order.getPaymentMethod()).append("</p>");

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(emailContent.toString(), true);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        // Gửi email
        mailSender.send(mimeMessage);
    }

    public void sendOrderShippedEmail(Order order) {
        String token = UUID.randomUUID().toString();
        order.setConfirmationToken(token);
        orderService.save(order);

        String subject = "Thông báo đơn hàng đang được vận chuyển";
        StringBuilder emailContent = new StringBuilder();

        emailContent.append("<h3>Đơn hàng của bạn đang được vận chuyển</h3>");
        emailContent.append("<p>Xin chào ").append(order.getUser().getFullname()).append(",</p>");
        emailContent.append("<p>Đơn hàng của bạn hiện đã được giao cho đơn vị vận chuyển. Vui lòng theo dõi để nhận hàng.</p>");
        emailContent.append("<p>Sau khi nhận hàng, vui lòng xác nhận đơn hàng bằng cách nhấn vào nút dưới đây:</p>");

        String confirmationUrl = "http://localhost:8080/confirmationOrder/" + order.getId() + "/confirm?token=" + token;
        emailContent.append("<form action=\"").append(confirmationUrl).append("\" method=\"post\">");
        emailContent.append("<button type=\"submit\" style=\"padding: 10px 20px; color: white; background-color: green; border: none;\">Xác nhận Hoàn Thành</button>");
        emailContent.append("</form>");

        emailContent.append("<p>Xin cảm ơn!</p>");

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(order.getUser().getEmail());
            helper.setSubject(subject);
            helper.setText(emailContent.toString(), true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
