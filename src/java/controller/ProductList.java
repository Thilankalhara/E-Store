package controller;

import hibernate.HibernateUtil;
import hibernate.Product;
import hibernate.Seller;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@WebServlet("/productList")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,
        maxFileSize = 1024 * 1024 * 20,
        maxRequestSize = 1024 * 1024 * 100
)
public class ProductList extends HttpServlet {

    private static final String UPLOAD_DIR = "C:\\Users\\HP\\OneDrive\\Documents\\NetBeansProjects\\WEB2_shop\\web\\assets\\store";

    private String toHtmlPath(String dbPath) {
        if (dbPath == null || dbPath.trim().isEmpty()) return "";
        String fileName = dbPath.contains("/") ? dbPath.substring(dbPath.lastIndexOf("/") + 1)
                                               : dbPath.substring(dbPath.lastIndexOf("\\") + 1);
        return "assets/store/" + fileName;
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
        return "assets/store/" + fileName;
    }

    private String getFileExtension(Part part) {
        String filename = part.getSubmittedFileName();
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return "";
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession httpSession = request.getSession(false);
        if (httpSession == null || httpSession.getAttribute("seller") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"success\":false,\"message\":\"You must be logged in as seller\"}");
            return;
        }
        Seller seller = (Seller) httpSession.getAttribute("seller");

        String idStr = request.getParameter("id");
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            if (idStr != null && !idStr.trim().isEmpty()) {
                int id = Integer.parseInt(idStr);
                Product p = (Product) session.get(Product.class, id);
                if (p == null || p.getSellerId() != seller.getId()) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write("{\"success\":false,\"message\":\"Product not found or unauthorized\"}");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("{\"success\":true,\"product\":{");
                sb.append("\"id\":").append(p.getId()).append(",");
                sb.append("\"name\":\"").append(p.getName().replace("\"","\\\"")).append("\",");
                sb.append("\"title\":\"").append(p.getTitle().replace("\"","\\\"")).append("\",");
                sb.append("\"price\":").append(p.getPrice()).append(",");
                sb.append("\"qty\":").append(p.getQty()).append(",");
                sb.append("\"description\":\"").append(p.getDescription().replace("\"","\\\"")).append("\",");
                sb.append("\"image1\":\"").append(toHtmlPath(p.getImage1())).append("\",");
                sb.append("\"image2\":\"").append(toHtmlPath(p.getImage2())).append("\"");
                sb.append("}}");
                out.write(sb.toString());
                return;
            }

            List all = session.createCriteria(Product.class).list();
            List<Product> sellerProducts = new ArrayList<Product>();
            for (Object o : all) {
                Product p = (Product) o;
                if (p.getSellerId() == seller.getId()) sellerProducts.add(p);
            }

            StringBuilder json = new StringBuilder("{\"success\":true,\"products\":[");
            for (int i = 0; i < sellerProducts.size(); i++) {
                Product p = sellerProducts.get(i);
                json.append("{")
                    .append("\"id\":").append(p.getId()).append(",")
                    .append("\"name\":\"").append(p.getName().replace("\"","\\\"")).append("\",")
                    .append("\"title\":\"").append(p.getTitle().replace("\"","\\\"")).append("\",")
                    .append("\"price\":").append(p.getPrice()).append(",")
                    .append("\"qty\":").append(p.getQty()).append(",")
                    .append("\"description\":\"").append(p.getDescription().replace("\"","\\\"")).append("\",")
                    .append("\"image1\":\"").append(toHtmlPath(p.getImage1())).append("\",")
                    .append("\"image2\":\"").append(toHtmlPath(p.getImage2())).append("\"")
                    .append("}");
                if (i < sellerProducts.size() - 1) json.append(",");
            }
            json.append("]}");
            out.write(json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"success\":false,\"message\":\"" + e.getMessage().replace("\"","\\\"") + "\"}");
        } finally {
            if (session != null) session.close();
            out.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession httpSession = request.getSession(false);
        if (httpSession == null || httpSession.getAttribute("seller") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"success\":false,\"message\":\"You must be logged in as seller\"}");
            return;
        }
        Seller seller = (Seller) httpSession.getAttribute("seller");

        Session session = null;
        Transaction tx = null;

        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String name = request.getParameter("name");
            String title = request.getParameter("title");
            double price = Double.parseDouble(request.getParameter("price"));
            int qty = Integer.parseInt(request.getParameter("quantity"));
            String description = request.getParameter("description");

            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            Product product = (Product) session.get(Product.class, id);
            if (product == null || product.getSellerId() != seller.getId()) {
                out.write("{\"success\":false,\"message\":\"Product not found or unauthorized\"}");
                return;
            }

            product.setName(name);
            product.setTitle(title);
            product.setPrice(price);
            product.setQty(qty);
            product.setDescription(description);

            Part image1Part = request.getPart("image1");
            if (image1Part != null && image1Part.getSize() > 0) {
                product.setImage1(saveFile(image1Part));
            }
            Part image2Part = request.getPart("image2");
            if (image2Part != null && image2Part.getSize() > 0) {
                product.setImage2(saveFile(image2Part));
            }

            session.update(product);
            tx.commit();

            out.write("{\"success\":true,\"message\":\"Product updated successfully\"}");

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            out.write("{\"success\":false,\"message\":\"" + e.getMessage().replace("\"","\\\"") + "\"}");
        } finally {
            if (session != null) session.close();
            out.close();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession httpSession = request.getSession(false);
        if (httpSession == null || httpSession.getAttribute("seller") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"success\":false,\"message\":\"You must be logged in as seller\"}");
            return;
        }
        Seller seller = (Seller) httpSession.getAttribute("seller");

        Session session = null;
        Transaction tx = null;

        try {
            int id = Integer.parseInt(request.getParameter("id"));

            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            Product product = (Product) session.get(Product.class, id);
            if (product == null || product.getSellerId() != seller.getId()) {
                out.write("{\"success\":false,\"message\":\"Product not found or unauthorized\"}");
                return;
            }

            session.delete(product);
            tx.commit();

            out.write("{\"success\":true,\"message\":\"Product deleted successfully\"}");

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            out.write("{\"success\":false,\"message\":\"" + e.getMessage().replace("\"","\\\"") + "\"}");
        } finally {
            if (session != null) session.close();
            out.close();
        }
    }
}
