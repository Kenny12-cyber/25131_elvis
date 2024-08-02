package servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Home
 */
@WebServlet("/ho")
public class Home extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Initialize the Logger
    private static final Logger logger = Logger.getLogger(Home.class.getName());

    public Home() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("Enter doGet method");
        response.getWriter().print("<h1>Hello, my name is Mutesa Musoni Kenny Evis and my ID is 25131</h1>");
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("Enter doPost method");

        String action = request.getParameter("action");
        String enteredId = request.getParameter("id");
        String name = request.getParameter("name");

        // Validate ID
        if (enteredId == null || !enteredId.matches("\\d+")) {
            logger.warning("Invalid ID: " + enteredId);
            response.getWriter().print("<h1>ID must be a number</h1>");
            return;
        }

        Integer id = Integer.parseInt(enteredId);

        String dbUrl = "jdbc:postgresql://host.docker.internal:5432/best_db";
        String username = "postgres";
        String password = "";

        try {
            Class.forName("org.postgresql.Driver");
            logger.info("Loaded PostgreSQL driver");
        } catch (ClassNotFoundException e) {
            logger.severe("Class Not Found Error: " + e.getMessage());
            response.getWriter().print("<h2>Class Not Found Error: " + e.getMessage() + "</h2>");
            return;
        }

        try (Connection con = DriverManager.getConnection(dbUrl, username, password)) {
            switch (action) {
                case "search":
                    searchStudent(con, id, response);
                    break;
                case "add":
                    if (name == null || name.isEmpty()) {
                        response.getWriter().print("<h1>Name cannot be empty</h1>");
                    } else {
                        addStudent(con, id, name, response);
                    }
                    break;
                case "delete":
                    deleteStudent(con, id, response);
                    break;
                default:
                    response.getWriter().print("<h1>Invalid action</h1>");
            }
        } catch (SQLException e) {
            logger.severe("SQL Error: " + e.getMessage());
            response.getWriter().print("<h2>SQL Error: " + e.getMessage() + "</h2>");
        } catch (Exception e) {
            logger.severe("Unexpected Error: " + e.getMessage());
            response.getWriter().print("<h2>Unexpected Error: " + e.getMessage() + "</h2>");
        }
    }

    private void searchStudent(Connection con, Integer id, HttpServletResponse response) throws SQLException, IOException {
        logger.info("Searching student with ID: " + id);

        String query = "SELECT * FROM student WHERE id = ?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("names");
                    logger.info("Found student: ID = " + id + ", Name = " + name);
                    response.getWriter().print("<h1>Your name is " + escapeHtml(name) + " and your id is " + id + "</h1>");
                } else {
                    logger.info("No student found with ID: " + id);
                    response.getWriter().print("<h2>ID doesn't exist</h2>");
                }
            }
        }
    }

    private void addStudent(Connection con, Integer id, String name, HttpServletResponse response) throws SQLException, IOException {
        logger.info("Adding student with ID: " + id + " and Name: " + name);

        String insertQuery = "INSERT INTO student (id, names) VALUES (?, ?)";
        try (PreparedStatement pst = con.prepareStatement(insertQuery)) {
            pst.setInt(1, id);
            pst.setString(2, name);
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected != 0) {
                logger.info("Inserted data in database: ID = " + id + ", Name = " + name);
                response.getWriter().print("<h1>Added student with ID " + id + " and name " + escapeHtml(name) + "</h1>");
            } else {
                logger.warning("Insert in database failed for ID: " + id);
                response.getWriter().print("<h1>Failed to add student</h1>");
            }
        }
    }

    private void deleteStudent(Connection con, Integer id, HttpServletResponse response) throws SQLException, IOException {
        logger.info("Deleting student with ID: " + id);

        String deleteQuery = "DELETE FROM student WHERE id = ?";
        try (PreparedStatement pst = con.prepareStatement(deleteQuery)) {
            pst.setInt(1, id);
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Successfully deleted student with ID: " + id);
                response.getWriter().print("<h1>Deleted student with ID " + id + "</h1>");
            } else {
                logger.warning("Failed to delete student with ID: " + id + " (ID may not exist)");
                response.getWriter().print("<h1>Failed to delete student with ID " + id + " (ID may not exist)</h1>");
            }
        }
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }
}
