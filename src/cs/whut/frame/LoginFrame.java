package cs.whut.frame;

import cs.whut.common.CurrentUser;
import cs.whut.common.DataProcessing;
import cs.whut.common.PrintMessage;
import cs.whut.common.User;
import cs.whut.server_client.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.sql.Timestamp;

/**
 * Created on 19:18 2020/9/28
 */
public class LoginFrame extends JFrame {
    private JLabel nameLabel;
    private JLabel passwordLabel;
    private JTextField nameTextField;
    private JPasswordField passwordField;
    private JButton confirmButton;
    private JButton cancelButton;
    private JPanel loginPanel;
    static JFrame frame;

    public LoginFrame() {
        nameTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    passwordField.requestFocus();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        passwordField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        loginActionPerformed();
                    } catch (IOException | ClassNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        confirmButton.addActionListener(e -> {
            try {
                loginActionPerformed();
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        });
        cancelButton.addActionListener(e -> {
            frame.dispose();
            //DataProcessing.disconnectFromDatabase();
            try {
                Client.sendData("EXIT");
                Client.closeConnection();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.out.println(new Timestamp(System.currentTimeMillis()).toString() + "\t\t" + "Successfully disconnected from database.");
            System.out.println(new Timestamp(System.currentTimeMillis()).toString() + "\t\t" + "System exit.");
            System.exit(0);
        });
        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    Client.sendData("EXIT");
                    Client.closeConnection();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                //DataProcessing.disconnectFromDatabase();
            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
    }

    public void loginActionPerformed() throws IOException, ClassNotFoundException {
        String name = nameTextField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (name.equals("")) {
            frame.dispose();
            PrintMessage.print("User name is empty");
            JOptionPane.showMessageDialog(null, "User name cannot be empty.");
            PrintMessage.print("Restart login.");
            LoginFrame.main(null);
        }

        Client.sendData("LOGIN");
        if (Client.getData().equals("LOGIN")) {//服务器响应
            Client.sendData(name);
            Client.sendData(password);
            String message = Client.getData();
            if (message.contains("USER_LOGIN:")) {
                User user = Client.getUser();
                if (user != null) {
                    Client.sendData("CLIENT_LOGIN " + user.getName());
                    frame.dispose();
                    CurrentUser.setUser(user);
                    MainFrame mainFrame = new MainFrame();
                    mainFrame.setVisible(true);
                    PrintMessage.print(user.getRole() + " user [" + user.getName() + "] login successfully.");
                } else {
                    frame.dispose();
                    JOptionPane.showMessageDialog(null, message);
                    LoginFrame.main(null);
                }
            } else {
                frame.dispose();
                JOptionPane.showMessageDialog(null, message);
                LoginFrame.main(null);
            }
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                frame = new JFrame("System Login");
                frame.setContentPane(new LoginFrame().loginPanel);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(380, 200);
                UserFrame.setToCenter(frame);
                frame.setAlwaysOnTop(false);
                frame.setResizable(false);
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
