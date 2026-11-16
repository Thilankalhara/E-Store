package controller;

import com.google.gson.Gson;
import hibernate.*;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/CheckAddress")
public class CheckAddress extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession httpSession = request.getSession(false);
        User user = (httpSession != null ? (User) httpSession.getAttribute("user") : null);

        Map<String, Object> res = new HashMap<>();

        if (user == null) {
            res.put("success", false);
            res.put("redirect", "sign-in.html");
            out.print(new Gson().toJson(res));
            return;
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        Address address = (Address) session.createCriteria(Address.class)
                .add(Restrictions.eq("user", user))
                .setMaxResults(1)
                .uniqueResult();

        session.getTransaction().commit();
        session.close();

        if (address != null && address.getLine_1() != null && !address.getLine_1().trim().isEmpty()) {
            res.put("success", true);
        } else {
            res.put("success", false);
        }

        out.print(new Gson().toJson(res));
    }
}
