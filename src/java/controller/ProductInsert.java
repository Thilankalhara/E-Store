package controller;

import hibernate.HibernateUtil;
import hibernate.Product;
import hibernate.Seller;
import hibernate.User;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.util.Date;
import java.util.UUID;

@WebServlet("/insertProduct")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 20,      // 20MB per file
        maxRequestSize = 1024 * 1024 * 100   // 100MB total
)
public class ProductInsert extends HttpServlet {

    // Absolute path to your assets/users folder in NetBeans project
    private static final String UPLOAD_DIR = "C:\\Users\\HP\\OneDrive\\Documents\\NetBeansProjects\\WEB2_shop\\web\\assets\\store";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession httpSession = request.getSession(false);
        if (httpSession == null || httpSession.getAttribute("seller") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"success\": false, \"message\": \"You must be logged in as seller\"}");
            return;
        }

        Seller seller = (Seller) httpSession.getAttribute("seller");
        User user = (User) httpSession.getAttribute("user");

        Session session = null;
        Transaction transaction = null;

        try {
            // Ensure upload folder exists
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            // Form fields
            String name = request.getParameter("name");
            String title = request.getParameter("title");
            String priceStr = request.getParameter("price");
            String quantityStr = request.getParameter("quantity");
            String description = request.getParameter("description");

            if (name == null || title == null || priceStr == null || quantityStr == null || description == null ||
                    name.trim().isEmpty() || title.trim().isEmpty() || description.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"success\": false, \"message\": \"All fields are required\"}");
                return;
            }

            double price = Double.parseDouble(priceStr);
            int quantity = Integer.parseInt(quantityStr);

            if (price < 0 || quantity < 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"success\": false, \"message\": \"Price and quantity cannot be negative\"}");
                return;
            }

            // Image uploads
            Part image1Part = request.getPart("image1");
            Part image2Part = request.getPart("image2");

            if (image1Part == null || image2Part == null || image1Part.getSize() == 0 || image2Part.getSize() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"success\": false, \"message\": \"Both images are required\"}");
                return;
            }

            String image1File = saveFile(image1Part);
            String image2File = saveFile(image2Part);

            // Save Product
            Product product = new Product();
            product.setName(name.trim());
            product.setTitle(title.trim());
            product.setPrice(price);
            product.setQty(quantity);
            product.setDescription(description.trim());
            product.setCreated_at(new Date());
            product.setUserId(user.getId());
            product.setSellerId(seller.getId());
            product.setStatusId(1); // default status
            product.setImage1(image1File);
            product.setImage2(image2File);

            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.save(product);
            transaction.commit();

            out.write("{\"success\": true, \"message\": \"Product inserted successfully\", \"productId\": " + product.getId() + "}");

        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) transaction.rollback();
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"success\": false, \"message\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
        } finally {
            if (session != null) session.close();
            out.close();
        }
    }

    private String saveFile(Part filePart) throws IOException {
        String fileName = UUID.randomUUID() + getFileExtension(filePart);

        File file = new File(UPLOAD_DIR, fileName);

        try (InputStream input = filePart.getInputStream();
             FileOutputStream output = new FileOutputStream(file)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }

        // Return relative path for DB (useful for HTML <img src>)
        return "assets/users/" + fileName;
    }

    private String getFileExtension(Part part) {
        String filename = part.getSubmittedFileName();
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return "";
    }
}
