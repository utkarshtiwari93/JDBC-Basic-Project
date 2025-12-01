import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final String url = "jdbc:mysql://localhost:3306/lenden";
    private static final String userName = "root";
    private static final String password = "UTKARSH@123";

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
            return;
        }

        try (Connection connection = DriverManager.getConnection(url, userName, password);
             Scanner scanner = new Scanner(System.in)) {

            connection.setAutoCommit(false);

            System.out.print("Enter account number to debit: ");
            int fromAccount = scanner.nextInt();

            System.out.print("Enter destination account number to credit: ");
            int toAccount = scanner.nextInt();

            System.out.print("Enter amount to transfer: ");
            double amount = scanner.nextDouble();

            // Check balance before debit
            if (!isSufficient(connection, fromAccount, amount)) {
                System.out.println(" Insufficient balance!");
                return;
            }

            String debitQuery = "UPDATE accounts SET balance = balance - ? WHERE account_no = ?";
            String creditQuery = "UPDATE accounts SET balance = balance + ? WHERE account_no = ?";

            try (PreparedStatement debitStmt = connection.prepareStatement(debitQuery);
                 PreparedStatement creditStmt = connection.prepareStatement(creditQuery)) {

                debitStmt.setDouble(1, amount);
                debitStmt.setInt(2, fromAccount);

                creditStmt.setDouble(1, amount);
                creditStmt.setInt(2, toAccount);

                int rows1 = debitStmt.executeUpdate();
                int rows2 = creditStmt.executeUpdate();

                if (rows1 > 0 && rows2 > 0) {
                    connection.commit();
                    System.out.println(" Transaction successful!");
                } else {
                    connection.rollback();
                    System.out.println("Transaction failed, rolled back.");
                }

            } catch (SQLException e) {
                connection.rollback();
                System.out.println("Error during transaction: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    static boolean isSufficient(Connection connection, int account_no, double amount) {
        String query = "SELECT balance FROM accounts WHERE account_no = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, account_no);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                double current_balance = resultSet.getDouble("balance");
                return current_balance >= amount;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
}
