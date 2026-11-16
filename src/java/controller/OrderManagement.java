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

@WebServlet("/OrderManagement")
public class OrderManagement extends HttpServlet {

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
        if (httpSession == null || httpSession.getAttribute("seller") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"success\":false,\"message\":\"You must be logged in as seller\"}");
            return;
        }
        Seller seller = (Seller) httpSession.getAttribute("seller");

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        List<Map<String, Object>> orderList = new ArrayList<>();

        try {
            Criteria itemCriteria = session.createCriteria(OrderItem.class);
            itemCriteria.createAlias("product", "p");
            itemCriteria.add(Restrictions.eq("p.sellerId", seller.getId()));

            @SuppressWarnings("unchecked")
            List<OrderItem> items = itemCriteria.list();

            for (OrderItem item : items) {
                Orders o = item.getOrders();
                Map<String, Object> map = new HashMap<>();
                map.put("order_id", o.getId());
                map.put("created_at", o.getCreated_at());
                map.put("total_price", o.getPrice());
                map.put("status", o.getOrderStatus() != null ? o.getOrderStatus().getValue() : null);
                map.put("status_id", o.getOrderStatus() != null ? o.getOrderStatus().getId() : null);

                User u = o.getUser();
                map.put("first_name", u.getFirst_name());
                map.put("last_name", u.getLast_name());
                map.put("email", u.getEmail());
                map.put("mobile", u.getMobile());

                Address addr = o.getAddress();
                if (addr != null) {
                    map.put("address_line1", addr.getLine_1());
                    map.put("address_line2", addr.getLine_2());
                    map.put("postal_code", addr.getPostal_code());
                }

                Product p = item.getProduct();
                map.put("product_name", p.getName());
                map.put("product_image", toHtmlPath(p.getImage1()));
                map.put("product_qty", item.getQty());

                orderList.add(map);
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession httpSession = request.getSession(false);
        if (httpSession == null || httpSession.getAttribute("seller") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"success\":false,\"message\":\"You must be logged in as seller\"}");
            return;
        }
        Seller seller = (Seller) httpSession.getAttribute("seller");

        int orderId = Integer.parseInt(request.getParameter("order_id"));
        int newStatusId = Integer.parseInt(request.getParameter("status_id"));

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        boolean success = false;

        try {
            Orders order = (Orders) session.get(Orders.class, orderId);
            if (order != null) {
                Criteria itemCriteria = session.createCriteria(OrderItem.class);
                itemCriteria.add(Restrictions.eq("orders", order));
                itemCriteria.createAlias("product", "p");
                itemCriteria.add(Restrictions.eq("p.sellerId", seller.getId()));

                if (!itemCriteria.list().isEmpty()) {
                    OrderStatus newStatus = (OrderStatus) session.get(OrderStatus.class, newStatusId);
                    if (newStatus != null) {
                        order.setOrderStatus(newStatus);
                        session.update(order);
                        success = true;
                    }
                }
            }

            if (success) tx.commit();
            else tx.rollback();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }

        out.print("{\"success\":" + success + "}");
        out.flush();
    }
}
