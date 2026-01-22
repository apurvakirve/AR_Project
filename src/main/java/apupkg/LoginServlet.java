package nidhipkg;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
/**
 * Servlet implementation class LoginServlet
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public LoginServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Cleanly redirect GET requests to the login page.
		response.sendRedirect("login.html");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 🛑 ERROR FIXED: Removed the call to doGet(request, response);
        // This call caused the IllegalStateException because doGet() performs a redirect, 
        // which commits the response before doPost() can execute its own redirects.
		
		// Set content type
        response.setContentType("text/html;charset=UTF-8");

		// Get form values
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        String errorMessage;
        
        // Resource variables (initialization to null for finally block)
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Database db = new Database(); // Assuming Database class is available

        // Basic validation
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {

            errorMessage = "Username and password are required.";
            // Perform redirect and immediately exit the method
            response.sendRedirect("login.html?error=" + errorMessage);
            return; 
        }

        try {
            conn = db.getConnection(); 

            if (conn == null) {
                errorMessage = "Database connection failed.";
                // Perform redirect and immediately exit the method
                response.sendRedirect("login.html?error=" + errorMessage);
                return; 
            }

            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);

            rs = ps.executeQuery();

            if (rs.next()) {
                // ✅ Login success: Use a session for state management (highly recommended)
                // request.getSession().setAttribute("user", username);
                response.sendRedirect("homepage.html");
            } else {
                // ❌ Invalid credentials
                response.sendRedirect("login.html?error=Invalid+Username+or+Password");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // In case of a database exception
            response.sendRedirect("login.html?error=Database+Error");
        } finally {
            // Close resources in reverse order
            try { if (rs != null) rs.close(); } catch (SQLException e) { /* log error */ }
            try { if (ps != null) ps.close(); } catch (SQLException e) { /* log error */ }
            // Assuming db.closeConnection() handles the Connection object
            if (conn != null) {
                db.closeConnection(); 
            }
        }
    }
}