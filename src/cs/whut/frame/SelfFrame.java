package cs.whut.frame;

import cs.whut.common.CurrentUser;
import cs.whut.common.PrintMessage;
import cs.whut.server_client.Client;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Created on 20:41 2020/9/28
 */
public class SelfFrame extends JFrame {
    private JPanel UserPanel;
    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmNewPasswordField;
    private JLabel charactorLabel;
    private JLabel confirmLabel;
    private JLabel newLabel;
    private JLabel oldLabel;
    private JLabel nameLabel;
    private JButton confirmButton;
    private JButton cancelButton;
    private JTextField nameTextField;
    private JTextField roleTextField;
    static JFrame frame;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                frame = new JFrame("Self Frame");
                frame.setContentPane(new SelfFrame().UserPanel);
                frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            } catch (Exception e) {
                frame.dispose();
                e.printStackTrace();
            }
        });
    }

    public SelfFrame() {
        nameTextField.setText(CurrentUser.getUser().getName());
        nameTextField.setEditable(false);
        roleTextField.setText(CurrentUser.getUser().getRole());
        roleTextField.setEditable(false);

        confirmButton.addActionListener(e -> {
            if (JOptionPane.showOptionDialog(null, "Are you sure to save?", "Notice",
                    JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null) == 1) {
                frame.dispose();
                return;
            }
            Client.sendData("CHANGE");
            try {
                if (Client.getData().equals("CHANGE")) {
                    String name1 = nameTextField.getText().trim();
                    String oldPassword = new String(oldPasswordField.getPassword());
                    String newPassword = new String(newPasswordField.getPassword());
                    String confirmPassword = new String(confirmNewPasswordField.getPassword());

                    oldPasswordField.setText("");
                    newPasswordField.setText("");
                    confirmNewPasswordField.setText("");

                    Client.sendData(name1);
                    Client.sendData(oldPassword);
                    Client.sendData(newPassword);
                    Client.sendData(confirmPassword);
                    Client.sendData(CurrentUser.getUser().getRole());

                    CurrentUser.getUser().setPassword(newPassword);

                    String message = Client.getData();
                    if (!message.equals("USER_UPDATE")) {
                        PrintMessage.print("Password has been changed.");
                        JOptionPane.showMessageDialog(null, "Password has been changed.");
                    } else {
                        JOptionPane.showMessageDialog(null, message);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        cancelButton.addActionListener(e -> frame.dispose());
    }
}
