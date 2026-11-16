package controller;

import hibernate.*;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/TestDatabase")
public class DatabaseTest extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        out.println("<html><body>");
        out.println("<h2>Database Connection Test</h2>");
        
        Session session = null;
        Transaction tx = null;
        
        try {
            // Test session creation
            out.println("<p>1. Testing Hibernate session...</p>");
            session = HibernateUtil.getSessionFactory().openSession();
            out.println("<p style='color:green'>✓ Hibernate session created successfully</p>");
            
            tx = session.beginTransaction();
            out.println("<p style='color:green'>✓ Transaction started successfully</p>");
            
            // Test OrderStatus creation
            out.println("<p>2. Testing OrderStatus creation...</p>");
            OrderStatus testStatus = new OrderStatus("Test Status");
            session.save(testStatus);
            session.flush();
            out.println("<p style='color:green'>✓ OrderStatus created with ID: " + testStatus.getId() + "</p>");
            
            // Get a test user (assuming user ID 1 exists)
            out.println("<p>3. Testing User retrieval...</p>");
            User testUser = (User) session.get(User.class, 1);
            if (testUser != null) {
                out.println("<p style='color:green'>✓ User found: ID=" + testUser.getId() + 
                           ", Name=" + testUser.getFirst_name() + " " + testUser.getLast_name() + "</p>");
                
                // Check if user has an address
                out.println("<p>4. Testing Address retrieval...</p>");
                @SuppressWarnings("unchecked")
                java.util.List<Address> addresses = session.createCriteria(Address.class)
                        .add(org.hibernate.criterion.Restrictions.eq("user", testUser))
                        .list();
                
                if (addresses.isEmpty()) {
                    out.println("<p style='color:red'>⚠ No addresses found for user. Creating test address...</p>");
                    Address testAddress = new Address("Test Street 123", "Apt 1", "12345", testUser);
                    session.save(testAddress);
                    session.flush();
                    out.println("<p style='color:green'>✓ Test address created with ID: " + testAddress.getId() + "</p>");
                } else {
                    out.println("<p style='color:green'>✓ Found " + addresses.size() + " addresses for user</p>");
                }
                
            } else {
                out.println("<p style='color:red'>✗ No user with ID 1 found. Please create a test user first.</p>");
            }
            
            // Test Orders table structure
            out.println("<p>5. Testing Orders creation...</p>");
            if (testUser != null) {
                // Get address again
                @SuppressWarnings("unchecked")
                java.util.List<Address> userAddresses = session.createCriteria(Address.class)
                        .add(org.hibernate.criterion.Restrictions.eq("user", testUser))
                        .setMaxResults(1)
                        .list();
                
                if (!userAddresses.isEmpty()) {
                    Address userAddress = userAddresses.get(0);
                    
                    try {
                        Orders testOrder = new Orders(testUser, userAddress, testStatus, 99.99);
                        session.save(testOrder);
                        session.flush();
                        out.println("<p style='color:green'>✓ Test Order created with ID: " + testOrder.getId() + "</p>");
                        
                        // Test OrderItem creation
                        out.println("<p>6. Testing OrderItem creation...</p>");
                        
                        // Get a test product (assuming product ID 1 exists)
                        Product testProduct = (Product) session.get(Product.class, 1);
                        if (testProduct != null) {
                            OrderItem testOrderItem = new OrderItem(testOrder, testProduct, 2);
                            session.save(testOrderItem);
                            session.flush();
                            out.println("<p style='color:green'>✓ Test OrderItem created with ID: " + testOrderItem.getId() + "</p>");
                        } else {
                            out.println("<p style='color:red'>⚠ No product with ID 1 found. Cannot test OrderItem creation.</p>");
                        }
                        
                    } catch (Exception orderEx) {
                        out.println("<p style='color:red'>✗ Error creating test order: " + orderEx.getMessage() + "</p>");
                        orderEx.printStackTrace();
                    }
                }
            }
            
            // Commit test transaction
            tx.commit();
            out.println("<p style='color:green'>✓ All tests completed and committed successfully!</p>");
            
        } catch (Exception e) {
            out.println("<p style='color:red'>✗ Error: " + e.getMessage() + "</p>");
            out.println("<pre>");
            e.printStackTrace(out);
            out.println("</pre>");
            
            if (tx != null) {
                try {
                    tx.rollback();
                    out.println("<p>Transaction rolled back</p>");
                } catch (Exception rollbackEx) {
                    out.println("<p style='color:red'>Rollback failed: " + rollbackEx.getMessage() + "</p>");
                }
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
        
        out.println("</body></html>");
        out.close();
    }
}