package controller;

import hibernate.HibernateUtil;
import hibernate.Seller;
import hibernate.User;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Date;

@WebServlet("/SellerRegistration")
public class SellerRegistration extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        // Get form parameters
        String storeName = request.getParameter("store_name");
        String storeEmail = request.getParameter("store_email");
        String taxId = request.getParameter("tax_id");
        String storeDescription = request.getParameter("store_description");

        // Fallback defaults for NOT NULL
        if (storeDescription == null || storeDescription.trim().isEmpty()) storeDescription = "No description provided";
        if (taxId == null || taxId.trim().isEmpty()) taxId = "N/A";
        if (storeEmail == null || storeEmail.trim().isEmpty()) storeEmail = "no-email@example.com";

        // Get logged-in user
        HttpSession httpSession = request.getSession(false);
        User loggedInUser = null;
        if (httpSession != null) {
            loggedInUser = (User) httpSession.getAttribute("user");
        }

        if (loggedInUser == null) {
            response.getWriter().write("<h3 style='color:red'>Error: User not logged in!</h3>");
            return;
        }

        // Create Seller entity
        Seller seller = new Seller();
        seller.setUser(loggedInUser);
        seller.setStoreName(storeName);
        seller.setStoreEmail(storeEmail);
        seller.setTaxId(taxId);
        seller.setStoreDescription(storeDescription);
        seller.setCreatedAt(new Date());

        Session hibernateSession = null;
        Transaction tx = null;

        try {
            hibernateSession = HibernateUtil.getSessionFactory().openSession();
            tx = hibernateSession.beginTransaction();

            hibernateSession.save(seller);
            tx.commit();

            // --- FIX: store seller in session ---
            httpSession.setAttribute("seller", seller);

            // Redirect to admin page after successful registration
            response.sendRedirect("admin.html");

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            response.getWriter().write("<h3 style='color:red'>Error creating store. Please check server logs.</h3>");
        } finally {
            if (hibernateSession != null) hibernateSession.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("admin.html");
    }
}
