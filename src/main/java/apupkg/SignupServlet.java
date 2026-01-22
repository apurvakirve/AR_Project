package nidhipkg;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * Servlet implementation class SignupServlet
 */
@WebServlet("/SignupServlet")
public class SignupServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SignupServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
		
		// Retrieve form data
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirm_password");

        String errorMessage = "";

        // ===== Validation =====
        if (isEmpty(name, email, phone, username, password, confirmPassword)) {
            errorMessage = "All fields are required.";
        } else if (!isValidEmail(email)) {
            errorMessage = "Invalid email format.";
        } else if (password.length() < 8) {
            errorMessage = "Password must be at least 8 characters long.";
        } else if (!password.equals(confirmPassword)) {
            errorMessage = "Passwords do not match.";
        }

        if (!errorMessage.isEmpty()) {
            response.sendRedirect("signup.html?error=" + errorMessage);
            return;
        }

        Database db = new Database();
        Connection conn = db.getConnection();

        try {
            // Check if email exists
            String emailCheck = "SELECT id FROM users WHERE email = ?";
            PreparedStatement psEmail = conn.prepareStatement(emailCheck);
            psEmail.setString(1, email);
            ResultSet rsEmail = psEmail.executeQuery();

            if (rsEmail.next()) {
                errorMessage = "Email already exists.";
                response.sendRedirect("signup.html?error=" + errorMessage);
                return;
            }

            // Check if username exists
            String userCheck = "SELECT id FROM users WHERE username = ?";
            PreparedStatement psUser = conn.prepareStatement(userCheck);
            psUser.setString(1, username);
            ResultSet rsUser = psUser.executeQuery();

            if (rsUser.next()) {
                errorMessage = "Username already taken.";
                response.sendRedirect("signup.html?error=" + errorMessage);
                return;
            }

            // Insert user into database (without hashing password)
            String insertSql = "INSERT INTO users (name, email, phone, username, password) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement psInsert = conn.prepareStatement(insertSql);
            psInsert.setString(1, name);
            psInsert.setString(2, email);
            psInsert.setString(3, phone);
            psInsert.setString(4, username);
            psInsert.setString(5, password);

            int result = psInsert.executeUpdate();

            if (result > 0) {
                response.sendRedirect("homepage.html");
            } else {
                errorMessage = "Error: Could not insert data.";
                response.sendRedirect("signup.html?error=" + errorMessage);
            }

            psEmail.close();
            psUser.close();
            psInsert.close();

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("signup.html?error=Database error: " + e.getMessage());
        } finally {
            db.closeConnection();
        }
    }

    private boolean isEmpty(String... fields) {
        for (String field : fields) {
            if (field == null || field.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidEmail(String email) {
        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return Pattern.matches(regex, email);
    }
	}


