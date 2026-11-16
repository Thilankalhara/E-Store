package model;

import java.io.IOException;
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
 * Filter to check if user is logged in before accessing edit profile page
 * TEMPORARILY DISABLED FOR DEBUGGING - uncomment @WebFilter when ready
 */
// @WebFilter(urlPatterns = {"/edit-profile.html", "/edit-profile"})
public class EditProfileFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // Initialization code if needed
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);
        
        // Set cache control headers
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        // Check if user is logged in
        if (session != null && session.getAttribute("user") != null) {
            // User is logged in, continue to the requested resource
            chain.doFilter(req, res);
        } else {
            // User is not logged in
            String requestURI = request.getRequestURI();
            
            // If it's an AJAX request (servlet endpoint), return JSON response
            if (requestURI.endsWith("/edit-profile")) {
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"message\":\"Not logged in\"}");
            } else {
                // If it's HTML page request, redirect to sign-in page
                response.sendRedirect("sign-in.html");
            }
        }
    }

    @Override
    public void destroy() {
        // Cleanup code if needed
    }
}