package controller;

import hibernate.HibernateUtil;
import hibernate.Seller;
import hibernate.User;
import org.hibernate.Session;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/seller-login")
public class SellerSignIn extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

           
            List<Seller> sellers = session.createCriteria(Seller.class).list();
            Seller loggedInSeller = null;

            for (Seller seller : sellers) {
                User user = seller.getUser();
                if (user != null && user.getEmail().equals(email) && user.getPassword().equals(password)) {
                    loggedInSeller = seller;
                    break;
                }
            }

            if (loggedInSeller != null) {
                // Login success
                HttpSession httpSession = request.getSession();

                // Store seller object
                httpSession.setAttribute("seller", loggedInSeller);

                // Also store the related user (needed in ProductInsert)
                httpSession.setAttribute("user", loggedInSeller.getUser());

                // Optionally keep extra info for frontend convenience
                httpSession.setAttribute("sellerId", loggedInSeller.getId());
                httpSession.setAttribute("sellerName", loggedInSeller.getStoreName());

                response.sendRedirect("admin.html");
            } else {
                // Login failed
                response.sendRedirect("seller-signIn.html?error=Invalid+email+or+password");
            }

            session.getTransaction().commit();
        } catch (Exception e) {
            if (session != null) session.getTransaction().rollback();
            e.printStackTrace();
            response.sendRedirect("seller-signIn.html?error=An+error+occurred");
        } finally {
            if (session != null) session.close();
        }
    }
}
