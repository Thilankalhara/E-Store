package controller;

import hibernate.Cart;
import hibernate.HibernateUtil;
import hibernate.Product;
import hibernate.User;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/AddToCart")
public class AddToCart extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        HttpSession httpSession = request.getSession(false);

        if (httpSession == null || httpSession.getAttribute("user") == null) {
            out.write("{\"success\": false, \"redirect\": \"sign-in.html\"}");
            return;
        }

        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            int productId = Integer.parseInt(request.getParameter("productId"));
            User user = (User) httpSession.getAttribute("user");

            Product product = (Product) session.get(Product.class, productId);

            if (product == null) {
                out.write("{\"success\": false, \"message\": \"Product not found\"}");
                return;
            }

            Cart cart = new Cart(1, product, user); 
            session.save(cart);

            tx.commit();

            out.write("{\"success\": true, \"message\": \"Product added to cart!\"}");

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            out.write("{\"success\": false, \"message\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
        } finally {
            if (session != null) session.close();
            out.close();
        }
    }
}
