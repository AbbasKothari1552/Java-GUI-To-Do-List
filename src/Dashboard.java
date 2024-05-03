import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class Dashboard {
    static JTable table = new JTable();

    public static JPanel createDashboardPanel(Connection conn) {
        JPanel dashboardPanel = new JPanel();
        dashboardPanel.setLayout(new BorderLayout());
        dashboardPanel.setSize(700, 500);

        // Create a custom font with bold style and size 16
        Font customFont = new Font("Arial", Font.BOLD, 50);

        // Create the label with the custom font
        JLabel titleLabel = new JLabel("To Do List");
        titleLabel.setFont(customFont);

        // New Task Button
        JButton taskButton = new JButton("Create Task");
        taskButton.setPreferredSize(new Dimension(120, 30)); // Set the preferred size
        taskButton.addActionListener(e -> {
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
                String username = Main.username;

                // insert task. query
                String sql = "INSERT INTO user_task (task_name,username) VALUES (?, ?)";
                try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                    preparedStatement.setString(1, tName);
                    preparedStatement.setString(2, username);

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
        });

        // Table logic
        String sql = "SELECT id,task_name FROM user_task WHERE username = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, Main.username); // fetching email of current user.
            try (ResultSet rs = statement.executeQuery()) {
                DefaultTableModel model = new DefaultTableModel(new Object[][]{}, new String[]{"ID", "Task", "Actions"});
                table.setModel(model);

                int id;
                String task;
                while (rs.next()) {
                    id = rs.getInt(1);
                    task = rs.getString(2);
                    model.addRow(new Object[]{id, task});
                }

                // Set the cell renderer and editor for the "Actions" column
                table.getColumnModel().getColumn(2).setCellRenderer(new ButtonRenderer());
                table.getColumnModel().getColumn(2).setCellEditor(new ButtonEditor(new JCheckBox(), conn));

                // Add the label to the top of the panel
                dashboardPanel.add(titleLabel, BorderLayout.PAGE_START);

                // Add the table to a scroll pane
                JScrollPane scrollPane = new JScrollPane(table);
                dashboardPanel.add(scrollPane, BorderLayout.CENTER);
                // Add create task button
                dashboardPanel.add(taskButton, BorderLayout.PAGE_END);

                System.out.println("Hey table");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return dashboardPanel;
    }

    // Update Table Model after adding new task.
    public static void updateTableModel(Connection conn) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // Clear the existing rows

        String sql = "SELECT id, task_name FROM user_task WHERE username = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, Main.username); // fetching email of current user.

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String task = rs.getString(2);
                    model.addRow(new Object[]{id, task});
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static class ButtonRenderer implements TableCellRenderer {

        private final JPanel panel;
        private final JButton updateButton;
        private final JButton deleteButton;

        public ButtonRenderer() {
            panel = new JPanel();
            panel.setLayout(new GridLayout(1, 2));
            updateButton = new JButton("Update");
            deleteButton = new JButton("Delete");
            panel.add(updateButton);
            panel.add(deleteButton);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return panel;
        }
    }

    static class ButtonEditor extends DefaultCellEditor {

        private final JPanel panel;
        private final JButton updateButton;
        private final JButton deleteButton;
        private String clickedButton;
        private int selectedRow;
        private final Connection conn;

        public ButtonEditor(JCheckBox checkBox, Connection conn) {
            super(checkBox);
            this.conn = conn;
            panel = new JPanel();
            panel.setLayout(new GridLayout(1, 2));
            updateButton = new JButton("Update");
            updateButton.setFocusable(false);
            updateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Get the selected row
                    int row = table.convertRowIndexToModel(selectedRow);
                    // Get the ID of the task from the first column of the selected row
                    int taskId = (int) table.getModel().getValueAt(row, 0);
                    // Perform update operation here, e.g., show a dialog to edit the task
                    String newTaskName = JOptionPane.showInputDialog("Enter new task name:");
                    if (newTaskName != null && !newTaskName.isEmpty()) {
                        updateTask(conn, taskId, newTaskName); // Pass conn as a parameter
                        updateTableModel(conn);
                    }
                }
            });
            deleteButton = new JButton("Delete");
            deleteButton.setFocusable(false);
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Get the selected row
                    int row = table.convertRowIndexToModel(selectedRow);
                    // Get the ID of the task from the first column of the selected row
                    int taskId = (int) table.getModel().getValueAt(row, 0);
                    // Perform delete operation here
                    int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this task?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        deleteTask(conn, taskId); // Pass conn as a parameter
                        updateTableModel(conn);
                    }
                }
            });
            panel.add(updateButton);
            panel.add(deleteButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            selectedRow = row;
            clickedButton = "";
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return clickedButton;
        }
    }

    // Method to update a task
    private static void updateTask(Connection conn,int taskId, String newTaskName) {
        String sql = "UPDATE user_task SET task_name = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, newTaskName);
            preparedStatement.setInt(2, taskId);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Task updated successfully!!!");
            } else {
                System.out.println("Failed to update task.");
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    // Method to delete a task
    private static void deleteTask(Connection conn,int taskId) {
        String sql = "DELETE FROM user_task WHERE id = ?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, taskId);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Task deleted successfully!!!");
            } else {
                System.out.println("Failed to delete task.");
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}