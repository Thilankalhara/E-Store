package controller;

import com.google.gson.Gson;
import hibernate.Cart;
import hibernate.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/DeleteCart")
public class DeleteCart extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
       Map<String, Object> res = new HashMap<String, Object>();


        try {
            int cartId = Integer.parseInt(request.getParameter("cartId"));

            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction tx = session.beginTransaction();

            Cart cart = (Cart) session.get(Cart.class, cartId);
            if (cart != null) {
                session.delete(cart);
                tx.commit();
                res.put("success", true);
            } else {
                tx.rollback();
                res.put("success", false);
                res.put("message", "Cart item not found");
            }

            session.close();
        } catch (Exception e) {
            e.printStackTrace();
            res.put("success", false);
            res.put("message", e.getMessage());
        }

        out.print(new Gson().toJson(res));
    }
}
