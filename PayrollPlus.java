import java.sql.*;
import java.util.Scanner;

public class PayrollPlus {

    static final String URL = "jdbc:mysql://localhost:3306/payroll_db";
    static final String USER = "root";
    static final String PASS = "Meghana@123";

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {

                System.out.println("Connected successfully!");

                while (true) {
                    System.out.println("\n===== PAYROLL SYSTEM =====");
                    System.out.println("1. Add Employee");
                    System.out.println("2. View Employees");
                    System.out.println("3. Add Attendance");
                    System.out.println("4. Generate Payroll");
                    System.out.println("5. View Payroll Report");
                    System.out.println("6. Exit");

                    System.out.print("Enter choice: ");
                    int choice = getIntInput();

                    switch (choice) {
                        case 1 -> addEmployee(con);
                        case 2 -> viewEmployees(con);
                        case 3 -> addAttendance(con);
                        case 4 -> generatePayroll(con);
                        case 5 -> viewPayroll(con);
                        case 6 -> {
                            System.out.println("Exiting...");
                            return;
                        }
                        default -> System.out.println("Invalid choice!");
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    // 🔹 Safe Integer Input
    static int getIntInput() {
        while (!sc.hasNextInt()) {
            System.out.print("Enter valid number: ");
            sc.next();
        }
        return sc.nextInt();
    }

    // 1. Add Employee
    static void addEmployee(Connection con) {
        try {
            System.out.print("Enter ID: ");
            int id = getIntInput();
            sc.nextLine();

            System.out.print("Enter Name: ");
            String name = sc.nextLine();

            if (name.isEmpty()) {
                System.out.println("Name cannot be empty");
                return;
            }

            System.out.print("Enter Designation: ");
            String des = sc.nextLine();

            System.out.print("Enter Salary: ");
            double salary = sc.nextDouble();

            if (salary <= 0) {
                System.out.println("Salary must be positive");
                return;
            }

            // Check duplicate ID
            String checkQuery = "SELECT * FROM employees WHERE emp_id=?";
            try (PreparedStatement check = con.prepareStatement(checkQuery)) {
                check.setInt(1, id);
                ResultSet rs = check.executeQuery();
                if (rs.next()) {
                    System.out.println("Employee ID already exists!");
                    return;
                }
            }

            String query = "INSERT INTO employees VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, id);
                ps.setString(2, name);
                ps.setString(3, des);
                ps.setDouble(4, salary);
                ps.executeUpdate();
            }

            System.out.println("Employee Added!");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 2. View Employees
    static void viewEmployees(Connection con) {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM employees")) {

            System.out.println("\n--- Employee List ---");

            while (rs.next()) {
                System.out.println(
                        rs.getInt("emp_id") + " | " +
                                rs.getString("name") + " | " +
                                rs.getString("designation") + " | " +
                                rs.getDouble("base_salary")
                );
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 3. Add Attendance
    static void addAttendance(Connection con) {
        try {
            System.out.print("Enter Employee ID: ");
            int id = getIntInput();

            System.out.print("Enter Month (1-12): ");
            int month = getIntInput();

            System.out.print("Enter Year: ");
            int year = getIntInput();

            System.out.print("Enter Working Days: ");
            int days = getIntInput();

            if (month < 1 || month > 12) {
                System.out.println("Invalid month!");
                return;
            }

            if (days < 0 || days > 31) {
                System.out.println("Invalid working days!");
                return;
            }

            String query = "INSERT INTO attendance(emp_id, month, year, working_days) VALUES (?, ?, ?, ?)";

            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, id);
                ps.setInt(2, month);
                ps.setInt(3, year);
                ps.setInt(4, days);
                ps.executeUpdate();
            }

            System.out.println("Attendance Added!");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 4. Generate Payroll
    static void generatePayroll(Connection con) {
        try {
            System.out.print("Enter Month: ");
            int month = getIntInput();

            System.out.print("Enter Year: ");
            int year = getIntInput();

            String query =
                    "INSERT INTO payroll (emp_id, month, year, gross_salary, deductions, net_salary) " +
                            "SELECT e.emp_id, a.month, a.year, " +
                            "(e.base_salary/30)*a.working_days, " +
                            "(e.base_salary * 0.1), " +
                            "((e.base_salary/30)*a.working_days) - (e.base_salary * 0.1) " +
                            "FROM employees e JOIN attendance a ON e.emp_id = a.emp_id " +
                            "WHERE a.month=? AND a.year=? " +
                            "AND NOT EXISTS (" +
                            "SELECT 1 FROM payroll p WHERE p.emp_id=e.emp_id AND p.month=? AND p.year=?" +
                            ")";

            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, month);
                ps.setInt(2, year);
                ps.setInt(3, month);
                ps.setInt(4, year);

                int rows = ps.executeUpdate();
                System.out.println("Payroll Generated for " + rows + " employees!");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 5. View Payroll
    static void viewPayroll(Connection con) {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT e.name, p.month, p.net_salary " +
                             "FROM payroll p JOIN employees e ON p.emp_id = e.emp_id")) {

            System.out.println("\n--- Payroll Report ---");

            while (rs.next()) {
                System.out.println(
                        rs.getString("name") +
                                " | Month: " + rs.getInt("month") +
                                " | Salary: " + rs.getDouble("net_salary")
                );
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}