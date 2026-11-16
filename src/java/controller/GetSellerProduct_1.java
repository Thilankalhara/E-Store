package controller;

import hibernate.HibernateUtil;
import hibernate.Product;
import org.hibernate.Session;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/getSellerProduct_1")
public class GetSellerProduct_1 extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            // Hibernate 4/5 raw list workaround
            List allObjects = session.createCriteria(Product.class).list();
            List<Product> allProducts = new ArrayList<Product>();  // fixed diamond operator
            for (Object obj : allObjects) {
                allProducts.add((Product) obj);
            }

            // Build JSON manually
            StringBuilder json = new StringBuilder("{\"success\": true, \"products\": [");
            for (int i = 0; i < allProducts.size(); i++) {
                Product p = allProducts.get(i);

                String img1 = extractFileName(p.getImage1());
                String img2 = extractFileName(p.getImage2());

                json.append("{")
                    .append("\"id\":").append(p.getId()).append(",")
                    .append("\"name\":\"").append(p.getName().replace("\"", "\\\"")).append("\",")
                    .append("\"title\":\"").append(p.getTitle().replace("\"", "\\\"")).append("\",")
                    .append("\"price\":").append(p.getPrice()).append(",")
                    .append("\"qty\":").append(p.getQty()).append(",")
                    .append("\"description\":\"").append(p.getDescription().replace("\"", "\\\"")).append("\",")
                    .append("\"image1\":\"").append(img1).append("\",")
                    .append("\"image2\":\"").append(img2).append("\"")
                    .append("}");
                if (i < allProducts.size() - 1) json.append(",");
            }
            json.append("]}");

            out.write(json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            out.write("{\"success\": false, \"message\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
        } finally {
            if (session != null) session.close();
            out.close();
        }
    }

    private String extractFileName(String path) {
        if (path == null) return "";
        return path.contains("\\") ? path.substring(path.lastIndexOf("\\") + 1)
                                   : path.substring(path.lastIndexOf("/") + 1);
    }
}
