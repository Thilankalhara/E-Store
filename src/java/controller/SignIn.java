package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "SignIn", urlPatterns = {"/SignIn"})
public class SignIn extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("status", false);

        // Parse request body JSON
        JsonObject requestBody = gson.fromJson(request.getReader(), JsonObject.class);
        String email = requestBody.get("email").getAsString();
        String password = requestBody.get("password").getAsString();

        if (email.isEmpty()) {
            jsonResponse.addProperty("message", "Email cannot be empty.");
        } else if (!Util.isEmailValid(email)) {
            jsonResponse.addProperty("message", "Invalid email format.");
        } else if (password.isEmpty()) {
            jsonResponse.addProperty("message", "Password cannot be empty.");
        } else {
            Session hibSession = HibernateUtil.getSessionFactory().openSession();
            Criteria criteria = hibSession.createCriteria(User.class);
            criteria.add(Restrictions.eq("email", email));
            criteria.add(Restrictions.eq("password", password));

            User user = (User) criteria.uniqueResult();

            if (user == null) {
                jsonResponse.addProperty("message", "Invalid email or password.");
            } else {
                // ✅ Always store user in session
                HttpSession httpSession = request.getSession();
                httpSession.setAttribute("user", user);

                jsonResponse.addProperty("status", true);

                // 1 = redirect to verify page
                // 2 = redirect to home page
                if (!"verified".equalsIgnoreCase(user.getVerification())) {
                    jsonResponse.addProperty("message", "1");
                } else {
                    jsonResponse.addProperty("message", "2");
                }
            }

            hibSession.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(jsonResponse));
    }
}
