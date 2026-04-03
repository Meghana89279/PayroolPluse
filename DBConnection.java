import java.sql.*;

public class DBConnection {
    static final String URL = "jdbc:mysql://localhost:3306/payroll_db";
    static final String USER = "root";
    static final String PASS = "your_password";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}