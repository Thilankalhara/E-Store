package controller;

import com.google.gson.Gson;
import hibernate.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet("/UserOrders")
public class UserOrders extends HttpServlet {

    private String toHtmlPath(String dbPath) {
        if (dbPath == null || dbPath.trim().isEmpty()) return "";
        String fileName = dbPath.contains("/") 
                ? dbPath.substring(dbPath.lastIndexOf("/") + 1)
                : dbPath.substring(dbPath.lastIndexOf("\\") + 1);
        return "assets/store/" + fileName;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession httpSession = request.getSession(false);
        User user = (httpSession != null) ? (User) httpSession.getAttribute("user") : null;

        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"success\":false,\"message\":\"You must be logged in\"}");
            return;
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();

        List<Map<String, Object>> orderList = new ArrayList<>();

        try {
            Criteria orderCriteria = session.createCriteria(Orders.class);
            orderCriteria.add(Restrictions.eq("user", user));
            @SuppressWarnings("unchecked")
            List<Orders> orders = orderCriteria.list();

            for (Orders o : orders) {
                Criteria itemCriteria = session.createCriteria(OrderItem.class);
                itemCriteria.add(Restrictions.eq("orders", o));
                @SuppressWarnings("unchecked")
                List<OrderItem> items = itemCriteria.list();

                for (OrderItem item : items) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("order_id", o.getId());
                    map.put("created_at", o.getCreated_at());
                    map.put("total_price", o.getPrice());
                    map.put("status", o.getOrderStatus() != null ? o.getOrderStatus().getValue() : "Pending");

                    Product p = item.getProduct();
                    map.put("product_name", p.getName());
                    map.put("product_image", toHtmlPath(p.getImage1()));
                    map.put("price", p.getPrice());
                    map.put("product_qty", item.getQty());

                    orderList.add(map);
                }
            }

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }

        out.print(new Gson().toJson(orderList));
        out.flush();
    }
}
