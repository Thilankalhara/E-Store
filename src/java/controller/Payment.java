package controller;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import hibernate.*;
import org.hibernate.Transaction;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/Payment")
public class Payment extends HttpServlet {

    private static final String STRIPE_SECRET_KEY = System.getenv("STRIPE_SECRET_KEY") != null ? System.getenv("STRIPE_SECRET_KEY") : "YOUR_STRIPE_SECRET_KEY";
    private static final String SUCCESS_URL = "http://localhost:8080/WEB2_shop/success.html";
    private static final String CANCEL_URL = "http://localhost:8080/WEB2_shop/checkout.html";

    @Override
    public void init() throws ServletException {
        Stripe.apiKey = STRIPE_SECRET_KEY;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        HttpSession httpSession = request.getSession(false);
        User user = null;

        if (httpSession != null) {
            user = (User) httpSession.getAttribute("user");
        }

        if (user == null) {
            out.print("{\"success\":false,\"message\":\"User not logged in\"}");
            return;
        }

        org.hibernate.Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            @SuppressWarnings("unchecked")
            List<Cart> carts = session.createCriteria(Cart.class)
                    .add(org.hibernate.criterion.Restrictions.eq("user", user))
                    .list();

            if (carts.isEmpty()) {
                out.print("{\"success\":false,\"message\":\"Cart is empty\"}");
                return;
            }

            // Debugging
            System.out.println("DEBUG: Found carts = " + carts.size());
            for (Cart c : carts) {
                System.out.println("DEBUG: Cart -> product=" + c.getProduct().getName() + ", qty=" + c.getQty());
            }

            // Build line items for Stripe
            List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
            double totalPrice = 0.0;

            for (Cart c : carts) {
                totalPrice += c.getProduct().getPrice() * c.getQty();

                SessionCreateParams.LineItem.PriceData.ProductData productData =
                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(c.getProduct().getName())
                                .build();

                SessionCreateParams.LineItem.PriceData priceData =
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setProductData(productData)
                                .setUnitAmount((long) (c.getProduct().getPrice() * 100))
                                .build();

                SessionCreateParams.LineItem lineItem =
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(priceData)
                                .setQuantity((long) c.getQty())
                                .build();

                lineItems.add(lineItem);
            }

            // ✅ Create Orders + OrderItems in DB
            OrderStatus status = (OrderStatus) session.get(OrderStatus.class, 1); // assume 1 = Pending
            Address address = (Address) session.createQuery("from Address where user.id = :uid")
                    .setParameter("uid", user.getId())
                    .setMaxResults(1)
                    .uniqueResult();

            if (address == null) {
                out.print("{\"success\":false,\"message\":\"No address found\"}");
                return;
            }

            Orders order = new Orders(user, address, status, totalPrice);
            session.save(order);

            for (Cart c : carts) {
                OrderItem item = new OrderItem(order, c.getProduct(), c.getQty());
                session.save(item);
            }

            // Clear cart
            for (Cart c : carts) {
                session.delete(c);
            }

            // Create Stripe session
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(SUCCESS_URL)
                    .setCancelUrl(CANCEL_URL);

            for (SessionCreateParams.LineItem item : lineItems) {
                paramsBuilder.addLineItem(item);
            }

            SessionCreateParams params = paramsBuilder.build();
            Session stripeSession = Session.create(params);

            tx.commit();

            // Store order id in session (InvoiceSender can use it)
            httpSession.setAttribute("lastOrderId", order.getId());

            out.print("{\"success\":true,\"sessionId\":\"" + stripeSession.getId() + "\"}");

        } catch (StripeException e) {
            if (tx != null) tx.rollback();
            System.err.println("Stripe error: " + e.getMessage());
            e.printStackTrace();
            out.print("{\"success\":false,\"message\":\"Stripe error: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("General error: " + e.getMessage());
            e.printStackTrace();
            out.print("{\"success\":false,\"message\":\"Error: " + e.getMessage() + "\"}");
        } finally {
            if (session != null) session.close();
            if (out != null) out.close();
        }
    }
}
