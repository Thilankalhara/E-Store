package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.User;
import hibernate.Address;
import hibernate.HibernateUtil;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;

@WebServlet(name = "EditProfile", urlPatterns = {"/edit-profile"})
public class EditProfile extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        
        // Check if user is logged in - redirect to sign-in if not
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"message\":\"No session found\",\"redirect\":\"sign-in.html\"}");
            return;
        }

        User user = (User) session.getAttribute("user");
        Session hibSession = HibernateUtil.getSessionFactory().openSession();
        
        try {
            // Get fresh user data from database
            User dbUser = (User) hibSession.get(User.class, user.getId());
            
            if (dbUser == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.setContentType("application/json");
                resp.getWriter().write("{\"message\":\"User not found\",\"redirect\":\"sign-in.html\"}");
                return;
            }
            
            // Get user's address using HQL
            Query addressQuery = hibSession.createQuery("FROM Address WHERE user.id = :userId");
            addressQuery.setParameter("userId", user.getId());
            List<Address> addresses = addressQuery.list();
            
            // Create response with user data and address
            JsonObject responseObj = new JsonObject();
            responseObj.addProperty("id", dbUser.getId());
            responseObj.addProperty("first_name", dbUser.getFirst_name() != null ? dbUser.getFirst_name() : "");
            responseObj.addProperty("last_name", dbUser.getLast_name() != null ? dbUser.getLast_name() : "");
            responseObj.addProperty("email", dbUser.getEmail() != null ? dbUser.getEmail() : "");
            responseObj.addProperty("mobile", dbUser.getMobile() != null ? dbUser.getMobile() : "");
            
            // Add address data
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                responseObj.addProperty("line_1", address.getLine_1() != null ? address.getLine_1() : "");
                responseObj.addProperty("line_2", address.getLine_2() != null ? address.getLine_2() : "");
                responseObj.addProperty("postal_code", address.getPostal_code() != null ? address.getPostal_code() : "");
            } else {
                responseObj.addProperty("line_1", "");
                responseObj.addProperty("line_2", "");
                responseObj.addProperty("postal_code", "");
            }
            
            resp.setContentType("application/json");
            resp.getWriter().write(responseObj.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"message\":\"Error loading profile: " + e.getMessage() + "\"}");
        } finally {
            hibSession.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        
        // Check if user is logged in - redirect to sign-in if not
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"status\":false,\"message\":\"Please log in first\",\"redirect\":\"sign-in.html\"}");
            return;
        }

        User user = (User) session.getAttribute("user");

        Gson gson = new Gson();
        JsonObject body;
        try {
            body = gson.fromJson(req.getReader(), JsonObject.class);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"status\":false,\"message\":\"Invalid JSON format\"}");
            return;
        }

        String firstName = body.has("first_name") ? body.get("first_name").getAsString().trim() : "";
        String lastName = body.has("last_name") ? body.get("last_name").getAsString().trim() : "";
        String email = body.has("email") ? body.get("email").getAsString().trim() : "";
        String line1 = body.has("line_1") ? body.get("line_1").getAsString().trim() : "";
        String line2 = body.has("line_2") ? body.get("line_2").getAsString().trim() : "";
        String postalCode = body.has("postal_code") ? body.get("postal_code").getAsString().trim() : "";

        // Validate required fields
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"status\":false,\"message\":\"First name, last name, and email are required\"}");
            return;
        }

        // Email validation
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"status\":false,\"message\":\"Please enter a valid email address\"}");
            return;
        }

        Session hibSession = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        
        try {
            tx = hibSession.beginTransaction();
            
            // Update user data
            User dbUser = (User) hibSession.get(User.class, user.getId());
            if (dbUser == null) {
                throw new Exception("User not found in database");
            }
            
            dbUser.setFirst_name(firstName);
            dbUser.setLast_name(lastName);
            dbUser.setEmail(email);
            hibSession.update(dbUser);
            
            // Handle address data
            Query addressQuery = hibSession.createQuery("FROM Address WHERE user.id = :userId");
            addressQuery.setParameter("userId", user.getId());
            List<Address> addresses = addressQuery.list();
            
            Address address;
            if (!addresses.isEmpty()) {
                // Update existing address
                address = addresses.get(0);
            } else {
                // Create new address
                address = new Address();
                address.setUser(dbUser);
            }
            
            // Set address fields
            address.setLine_1(line1.isEmpty() ? null : line1);
            address.setLine_2(line2.isEmpty() ? null : line2);
            address.setPostal_code(postalCode.isEmpty() ? null : postalCode);
            
            // Save address
            if (addresses.isEmpty()) {
                hibSession.save(address);
            } else {
                hibSession.update(address);
            }
            
            tx.commit();
            
            // Update session user
            session.setAttribute("user", dbUser);
            
            JsonObject resObj = new JsonObject();
            resObj.addProperty("status", true);
            resObj.addProperty("message", "Profile updated successfully");

            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(resObj));
            
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            
            JsonObject resObj = new JsonObject();
            resObj.addProperty("status", false);
            resObj.addProperty("message", "Error updating profile: " + e.getMessage());

            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(resObj));
        } finally {
            hibSession.close();
        }
    }
}