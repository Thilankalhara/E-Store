package controller;

import com.google.gson.Gson;
import hibernate.Cart;
import hibernate.HibernateUtil;
import hibernate.User;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet("/GetCart")
public class GetCart extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession httpSession = request.getSession(false);
        User user = (User) (httpSession != null ? httpSession.getAttribute("user") : null);

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        if (user == null) {
            Map<String, Object> res = new HashMap<String, Object>();
            res.put("success", false);
            res.put("redirect", "sign-in.html");
            out.print(new Gson().toJson(res));
            return;
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        @SuppressWarnings("unchecked")
        List<Cart> carts = session.createCriteria(Cart.class)
                .add(Restrictions.eq("user", user))
                .list();

        session.getTransaction().commit();
        session.close();

        List<Map<String, Object>> cartList = new ArrayList<Map<String, Object>>();
        for (Cart c : carts) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("id", c.getId());
            item.put("productName", c.getProduct().getName());
            item.put("price", c.getProduct().getPrice());
            item.put("quantity", c.getQty());
            item.put("maxQty", c.getProduct().getQty()); // max quantity available
            String imgPath = c.getProduct().getImage1();
            if (imgPath != null && !imgPath.isEmpty()) {
                imgPath = "assets/store/" + extractFileName(imgPath);
            } else {
                imgPath = "assets/store/default.png";
            }
            item.put("image", imgPath);

            cartList.add(item);
        }

        Map<String, Object> res = new HashMap<String, Object>();
        res.put("success", true);
        res.put("carts", cartList);

        out.print(new Gson().toJson(res));
    }

    private String extractFileName(String path) {
        if (path == null) return "";
        return path.contains("\\") ? path.substring(path.lastIndexOf("\\") + 1)
                                   : path.substring(path.lastIndexOf("/") + 1);
    }
}
