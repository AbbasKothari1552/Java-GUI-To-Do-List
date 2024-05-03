import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

    public static String email;
    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;

        // Build connection with database.
        try {
            conn = DatabaseHandler.getConnection();
            stmt = conn.createStatement();

            System.out.println("Connection established");
            // Your database operations here

        } catch (SQLException e) {
            DatabaseHandler.close(conn, stmt);
            e.printStackTrace();
        }

        ToDoListApp obj = new ToDoListApp();
        // Gui opening.
        obj.createAndShowGUI(conn, stmt);
    }
}
