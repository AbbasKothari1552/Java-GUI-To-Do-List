import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Dashboard {

    static JTable table = new JTable();
    public static JPanel createDashboardPanel(Connection conn, Statement stmt) {
        JPanel dashboardPanel = new JPanel();
        dashboardPanel.setLayout(new BorderLayout());
        dashboardPanel.setSize(700, 500);


        // Create a custom font with bold style and size 16
        Font customFont = new Font("Arial", Font.BOLD, 50);

        // Create the label with the custom font
        JLabel titleLabel = new JLabel("To Do List");
        titleLabel.setFont(customFont);

        // New Task Button
        JButton taskButton = new JButton("create Task");
        taskButton.setPreferredSize(new Dimension(120, 30)); // Set the preferred size
        taskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // Create a custom panel for the form
                JPanel formPanel = new JPanel(new GridLayout(15, 5));

                // Create Form Field objects
                JLabel taskNameLabel = new JLabel("Enter Task:");
                JTextField taskNameField = new JTextField();

                // Add Fields to formPanel
                formPanel.add(taskNameLabel);
                formPanel.add(taskNameField);

                // Show the form in a dialog
                int result = JOptionPane.showConfirmDialog(null, formPanel, "Form", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    // Process form data
                    String tName = taskNameField.getText();
                    String email = Main.email;
//
//                    insert task. query
                    String sql = "INSERT INTO user_task (task_name,email) VALUES (?, ?)";
                    PreparedStatement preparedStatement = null;
                    try {
                        // Create a PreparedStatement for the INSERT query
                        preparedStatement = conn.prepareStatement(sql);
                        preparedStatement.setString(1, tName);
                        preparedStatement.setString(2, email);

                        // Execute the query
                        int rowsAffected = preparedStatement.executeUpdate();
                        if (rowsAffected > 0) {
                            System.out.println("Task added successfully!!!");
                            updateTableModel(conn);
                        } else {
                            System.out.println("Failed to add Task.");
                        }
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }

                }
            }
        });



//        Table logic


        String sql = "SELECT task_name FROM user_task WHERE email = ?";
        try  {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, Main.email); // fetching email of current user.

            ResultSet rs = statement.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();


            DefaultTableModel model = (DefaultTableModel) table.getModel();

            int cols = rsmd.getColumnCount();
            String []colsName = {"srNo.", "Task"};
            model.setColumnIdentifiers(colsName);

//            read data from table
            String task;
            int srNo = 1;
            while(rs.next()) {
                task = rs.getString(1);

                String[] row = {String.valueOf((srNo++)),task};
                model.addRow(row);
            }
            // Set the model back to the JTable
            table.setModel(model);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Add the label to the top of the panel
        dashboardPanel.add(titleLabel, BorderLayout.PAGE_START);

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        dashboardPanel.add(scrollPane, BorderLayout.CENTER);
//          Add create task button
        dashboardPanel.add(taskButton, BorderLayout.PAGE_END);

        System.out.println("Hey table");

        return dashboardPanel;
    }


//    Update Table Model after adding new task.
    public static void updateTableModel(Connection conn) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // Clear the existing rows

        String sql = "SELECT task_name FROM user_task WHERE email = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, Main.email); // fetching email of current user.

            ResultSet rs = statement.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();

            int srNo = 1;
            while (rs.next()) {
                String task = rs.getString(1);
                String[] row = {String.valueOf(srNo++), task};
                model.addRow(row);
            }

            // Set the model back to the JTable
            table.setModel(model);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}