package model;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Debug filter to track session information
 * TEMPORARY - Remove this after debugging is complete
 */
@WebFilter(filterName = "SessionDebugFilter", urlPatterns = {"/*"})
public class SessionDebugFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("SessionDebugFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
        String requestURI = req.getRequestURI();
        
        // Only debug specific pages to avoid spam
        if (requestURI.contains("edit-profile") || requestURI.contains("SignIn") || 
            requestURI.contains("sign-in") || requestURI.contains("index")) {
            
            HttpSession session = req.getSession(false);
            
            System.out.println("\n=== SESSION DEBUG: " + requestURI + " ===");
            System.out.println("Request Method: " + req.getMethod());
            
            if (session == null) {
                System.out.println("No session exists");
            } else {
                System.out.println("Session ID: " + session.getId());
                System.out.println("Session created: " + new java.util.Date(session.getCreationTime()));
                System.out.println("Session last accessed: " + new java.util.Date(session.getLastAccessedTime()));
                System.out.println("Session max inactive: " + session.getMaxInactiveInterval() + " seconds");
                System.out.println("Session is new: " + session.isNew());
                
                System.out.println("Session attributes:");
                Enumeration<String> attributeNames = session.getAttributeNames();
                boolean hasAttributes = false;
                while (attributeNames.hasMoreElements()) {
                    hasAttributes = true;
                    String name = attributeNames.nextElement();
                    Object value = session.getAttribute(name);
                    System.out.println("  " + name + " = " + value + 
                                     " (Type: " + (value != null ? value.getClass().getSimpleName() : "null") + ")");
                }
                
                if (!hasAttributes) {
                    System.out.println("  No session attributes found");
                }
            }
            
            System.out.println("==========================================\n");
        }
        
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        System.out.println("SessionDebugFilter destroyed");
    }
}