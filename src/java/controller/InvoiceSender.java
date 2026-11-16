package controller;

import hibernate.*;
import model.Mail;
import org.hibernate.Transaction;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/SendInvoice")
public class InvoiceSender extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession httpSession = request.getSession(false);
        User user = (User) (httpSession != null ? httpSession.getAttribute("user") : null);
        Integer lastOrderId = (httpSession != null) ? (Integer) httpSession.getAttribute("lastOrderId") : null;

        if (user == null) {
            out.print("{\"success\":false,\"message\":\"User not logged in\"}");
            return;
        }
        if (lastOrderId == null) {
            out.print("{\"success\":false,\"message\":\"No recent order found\"}");
            return;
        }

        org.hibernate.Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            Orders order = (Orders) session.get(Orders.class, lastOrderId);
            if (order == null) {
                out.print("{\"success\":false,\"message\":\"Order not found\"}");
                return;
            }

            @SuppressWarnings("unchecked")
            List<OrderItem> items = session.createQuery("from OrderItem where orders.id = :oid")
                    .setParameter("oid", order.getId())
                    .list();

            if (items == null || items.isEmpty()) {
                out.print("{\"success\":false,\"message\":\"No items found for this order\"}");
                return;
            }

            // Build invoice HTML
            StringBuilder html = new StringBuilder();
            html.append("<h2>E-Store Invoice</h2>");
            html.append("<p>Order ID: ").append(order.getId()).append("</p>");
            html.append("<p>Date: ").append(order.getCreated_at()).append("</p>");
            html.append("<table border='1' cellspacing='0' cellpadding='5'><tr>")
                .append("<th>Product</th><th>Price</th><th>Qty</th><th>Total</th></tr>");

            double grandTotal = 0;
            for (OrderItem item : items) {
                double total = item.getProduct().getPrice() * item.getQty();
                grandTotal += total;
                html.append("<tr>")
                        .append("<td>").append(item.getProduct().getName()).append("</td>")
                        .append("<td>$").append(item.getProduct().getPrice()).append("</td>")
                        .append("<td>").append(item.getQty()).append("</td>")
                        .append("<td>$").append(total).append("</td>")
                        .append("</tr>");
            }
            html.append("</table><h3>Total: $").append(grandTotal).append("</h3>");

            // Send invoice email
            Mail.sendMail(user.getEmail(), "Your E-Store Invoice", html.toString());

            tx.commit();
            out.print("{\"success\":true,\"message\":\"Invoice sent\"}");

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            out.print("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        } finally {
            if (session != null) session.close();
            if (out != null) out.close();
        }
    }
}
