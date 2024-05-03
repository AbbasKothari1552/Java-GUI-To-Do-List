import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ToDoListApp extends Component {

    private static JPanel cardPanel;
    private static CardLayout cardLayout;
    public void createAndShowGUI(Connection conn, Statement stmt) {
        JFrame frame = new JFrame("To-Do List App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);

        // Create a panel with CardLayout to switch between different panels
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Create the welcome panel
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new GridLayout(3, 1));

        // Create a custom font with bold style and size 16
        Font customFont = new Font("Arial", Font.BOLD, 50);

        // Create the label with the custom font
        JLabel titleLabel = new JLabel("Welcome to ToDo List App");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(customFont);

        // Add the label to the panel
        welcomePanel.add(titleLabel);


        // Assuming dashboardPanel is using GridBagLayout
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 1; // Column index
        constraints.gridy = 1; // Row index
        constraints.gridwidth = 1; // Number of columns the component will span
        constraints.gridheight = 1; // Number of rows the component will span
        constraints.fill = GridBagConstraints.NONE; // Do not resize the component
        constraints.anchor = GridBagConstraints.LINE_END; // Align the component to the end of the line


        // Registeration Button
        JButton registerButton = new JButton("Register");
        registerButton.setPreferredSize(new Dimension(100, 30)); // Set preferred size (width, height) for the button
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create a custom panel for the registration form
                JPanel registrationPanel = new JPanel(new GridLayout(15, 5));

                // Create Form Field objects
                JLabel firstName = new JLabel("First Name:");
                JTextField firstNameField = new JTextField();
                JLabel lastName = new JLabel("Last Name:");
                JTextField lastNameField = new JTextField();
                JLabel email = new JLabel("Email:");
                JTextField emailField = new JTextField();
                JLabel password1 = new JLabel("Enter password:");
                JPasswordField password1Field = new JPasswordField();
                JLabel password2 = new JLabel("Re-enter Password:");
                JPasswordField password2Field = new JPasswordField();

                // Add Fields.
                registrationPanel.add(firstName);
                registrationPanel.add(firstNameField);
                registrationPanel.add(lastName);
                registrationPanel.add(lastNameField);
                registrationPanel.add(email);
                registrationPanel.add(emailField);
                registrationPanel.add(password1);
                registrationPanel.add(password1Field);
                registrationPanel.add(password2);
                registrationPanel.add(password2Field);

                // Add more fields as needed

                int result = JOptionPane.showConfirmDialog(null, registrationPanel, "Registration Form", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    // Process registration data
                    String fName = firstNameField.getText();
                    String lName = lastNameField.getText();
                    String eMail = emailField.getText();
                    char[] passwordChars = password1Field.getPassword();
                    String password = new String(passwordChars);
                    char[] passwordChars2 = password1Field.getPassword();
                    String password_2 = new String(passwordChars2);

                    if(!password.equals(password_2)) {
                        JOptionPane.showMessageDialog(frame, "Password do not match. Re-register.");
                    }
                    else {

                        // save the data to a file or database
                        String sql = "INSERT INTO user_registration (first_name, last_name, email, password) VALUES (?, ?, ?, ?)";
                        PreparedStatement preparedStatement = null;
                        try {
                            // Create a PreparedStatement for the INSERT query
                            preparedStatement = conn.prepareStatement(sql);
                            preparedStatement.setString(1, fName);
                            preparedStatement.setString(2, lName);
                            preparedStatement.setString(3, eMail);
                            preparedStatement.setString(4, password);


                            // Execute the query
                            int rowsAffected = preparedStatement.executeUpdate();
                            if (rowsAffected > 0) {
                                System.out.println("User registered successfully!");
                                JOptionPane.showMessageDialog(frame, "User registered successfully!");
                            } else {
                                System.out.println("Failed to register user.");
                                JOptionPane.showMessageDialog(frame, "Failed to register. Please try again.");
                            }
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
        });
        welcomePanel.add(registerButton, constraints);

//        Login Button.
        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(120, 30)); // Set the preferred size
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create a custom panel for the login form
                JPanel loginPanel = new JPanel(new GridLayout(15, 5));

                // Declare fields
                JLabel email = new JLabel("Email:");
                JTextField emailField = new JTextField();
                JLabel password1 = new JLabel("Enter password:");
                JPasswordField password1Field = new JPasswordField();
//                Add Fields
                loginPanel.add(email);
                loginPanel.add(emailField);
                loginPanel.add(password1);
                loginPanel.add(password1Field);

                int result = JOptionPane.showConfirmDialog(null, loginPanel, "login Form", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    String eMail = emailField.getText();
                    char[] passwordChars = password1Field.getPassword();
                    String password = new String(passwordChars);

//                    prepare sql query to check the user registeration.
                    String sql = "SELECT count(*) FROM user_registration WHERE email = ? AND password = ?";
                    PreparedStatement preparedStatement = null;
                    try {
                        // Create a PreparedStatement for the INSERT query
                        preparedStatement = conn.prepareStatement(sql);
                        preparedStatement.setString(1, eMail);
                        preparedStatement.setString(2, password);

                        // Execute the query
                        ResultSet rs = preparedStatement.executeQuery();
                        if (rs.next()) {
                            int count = rs.getInt(1);
                            if (count > 0) {
                                Main.email = eMail;
                                System.out.println("User log-in successfully!");
                                JOptionPane.showMessageDialog(frame, "User log-in successfully!");
                                // Add the dashboard panel to the cardPanel
                                cardPanel.add(Dashboard.createDashboardPanel(conn, stmt), "dashboard");
                                cardLayout.show(cardPanel, "dashboard");
                            } else {
                                System.out.println("Failed to login user.");
                                JOptionPane.showMessageDialog(frame, "Failed to login user.Please try again");
                            }
                        }
                    }
                    catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
        welcomePanel.add(loginButton, constraints);

        // Add the welcome panel to the cardPanel
        cardPanel.add(welcomePanel, "welcome");


        // Add the cardPanel to the frame
        frame.add(cardPanel);

        // Show the welcome panel by default
        cardLayout.show(cardPanel, "welcome");

        // Make the frame visible
        frame.setVisible(true);

        // Add a window listener to detect when the window is closing
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Close database connection
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                    System.out.println("Connection Closed!!");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
