package cs.whut.frame;

import cs.whut.common.CurrentUser;
import cs.whut.common.PrintMessage;
import cs.whut.common.User;
import cs.whut.server_client.Client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * Created on 6:19 2020/9/29
 */
public class UserFrame extends JFrame {
    private JPanel userPanel;
    private JTabbedPane addTabbedPane;
    private JTabbedPane UpdateTabbedPane;
    private JTabbedPane deleteTabbedPane;
    private JPanel addPanel;
    private JPanel editPane;
    private JPanel deletePanel;
    private JTextField nameTextFieldInAdd;
    private JPasswordField passwordFieldInAdd;
    private JComboBox<String> comboBoxInAdd;
    private JButton confirmButtonInAdd;
    private JButton cancelButtonInAdd;
    private JComboBox<String> nameComboBoxInUpdate;
    private JPasswordField passwordFieldInUpdate;
    private JComboBox<String> comboBoxInUpdate;
    private JTable tableInDelete;
    private JScrollPane scrollPaneInDelete;
    private JButton confirmButtonInUpdate;
    private JButton cancelButtonInUpdate;
    private JButton deleteButtonInDelete;
    private JButton cancelButtonInDelete;
    private final String[] pane;
    static JFrame frame;

    public UserFrame(String pane) throws IOException, ClassNotFoundException {
        this.pane = new String[2];
        setPane(pane);
        confirmButtonInAdd.addActionListener(e -> {
            try {
                addActionPerformed();
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        });
        cancelButtonInAdd.addActionListener(this::cancelButtonActionPerformed);
        confirmButtonInUpdate.addActionListener(e -> {
            try {
                updateActionPerformed();
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        });
        cancelButtonInUpdate.addActionListener(this::cancelButtonActionPerformed);
        deleteButtonInDelete.addActionListener(e -> {
            try {
                deleteActionPerformed();
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        });
        cancelButtonInDelete.addActionListener(this::cancelButtonActionPerformed);
        constructUserTable();
        addUserToCombobox();
    }

    private void deleteActionPerformed() throws IOException, ClassNotFoundException {
        int selectedRow = tableInDelete.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "No User has been selected!");
        } else {
            String username = (String) tableInDelete.getValueAt(selectedRow, 0);
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(null, "You choose a blink line.");
                return;
            }
            if (username.equals(CurrentUser.getUser().getName())) {
                JOptionPane.showMessageDialog(null, "You cannot delete yourself.");
                return;
            }
            int value = JOptionPane.showConfirmDialog(null, "Are you sure to delete this user?", "Confirm", JOptionPane.OK_CANCEL_OPTION);
            if (value == 0) {
                Client.sendData("DELETE");
                if (Client.getData().equals("DELETE")) {
                    Client.sendData(username);
                    String message = Client.getData();
                    if (message.equals("USER_DELETE")) {
                        constructUserTable();
                        addUserToCombobox();
                        JOptionPane.showMessageDialog(null, "Delete succeed.");
                    } else {
                        JOptionPane.showMessageDialog(null, message);
                    }
                }
            } else if (value == 1) {
                JOptionPane.showMessageDialog(null, "Nothing changed.");
            }
        }
    }

    private void addUserToCombobox() throws IOException, ClassNotFoundException {
        nameComboBoxInUpdate.removeAllItems();
        Client.sendData("GET_USER");
        User[] users = Client.getUserSet();
        for (User u : users) {
            nameComboBoxInUpdate.addItem(u.getName());
        }
    }

    private void updateActionPerformed() throws IOException, ClassNotFoundException {
        String name = (String) nameComboBoxInUpdate.getSelectedItem();
        String password = new String(passwordFieldInUpdate.getPassword());
        String role = (String) comboBoxInUpdate.getSelectedItem();

        passwordFieldInUpdate.setText("");

        Client.sendData("UPDATE");
        if (Client.getData().equals("UPDATE")) {
            Client.sendData(name);
            Client.sendData(password);
            Client.sendData(role);
            String message = Client.getData();
            if (message.equals("USER_UPDATE")) {
                addUserToCombobox();
                constructUserTable();
                JOptionPane.showMessageDialog(null, "Update success.");
            } else {
                JOptionPane.showMessageDialog(null, message);
            }
        }
    }

    private void addActionPerformed() throws IOException, ClassNotFoundException {
        String name = nameTextFieldInAdd.getText();
        String password = new String(passwordFieldInAdd.getPassword());
        String role = (String) comboBoxInAdd.getSelectedItem();

        nameTextFieldInAdd.setText("");
        passwordFieldInAdd.setText("");

        if (name.trim().isEmpty()) {
            frame.dispose();
            JOptionPane.showMessageDialog(null, "User name cannot be empty.");
            UserFrame.main(pane);
        } else {
            Client.sendData("ADD");
            if (Client.getData().equals("ADD")) {
                Client.sendData(name);
                Client.sendData(password);
                Client.sendData(role);
                String message = Client.getData();
                if (message.equals("USER_ADD")) {
                    JOptionPane.showMessageDialog(null, "Add user success.");
                    PrintMessage.print("Add user success.");
                } else {
                    JOptionPane.showMessageDialog(null, message);
                }
            }
        }
        addUserToCombobox();
        constructUserTable();
    }

    private void cancelButtonActionPerformed(ActionEvent e) {
        frame.dispose();
    }

    private void constructUserTable() throws IOException, ClassNotFoundException {
        String[] header = {"Name", "Password", "Role"};
        String[][] usersData = new String[20][3];
        Client.sendData("GET_USER");
        User[] users = Client.getUserSet();
        int row = 0;
        for (User u : users) {
            usersData[row][0] = u.getName();
            usersData[row][1] = u.getPassword();
            usersData[row][2] = u.getRole();
            row++;
        }
        tableInDelete.setModel(new DefaultTableModel(usersData, header) {
            final boolean[] columnEditable = new boolean[]{false, false, false};

            public boolean isCellEditable(int row, int column) {
                return columnEditable[column];
            }
        });
        tableInDelete.getTableHeader().setReorderingAllowed(false);
        scrollPaneInDelete.setViewportView(tableInDelete);
    }

    private void setPane(String value) {
        this.pane[1] = value;
        switch (value) {
            case "0":
                addTabbedPane.setSelectedComponent(addPanel);
                break;
            case "1":
                addTabbedPane.setSelectedComponent(UpdateTabbedPane);
                break;
            case "2":
                addTabbedPane.setSelectedComponent(deleteTabbedPane);
                break;
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                String pane = args[1];
                frame = new JFrame("UserFrame");
                frame.setContentPane(new UserFrame(pane).userPanel);
                frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                frame.setSize(500, 480);
                setToCenter(frame);
                frame.setVisible(true);
            } catch (Exception e) {
                frame.dispose();
                JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
                e.printStackTrace();
            }
        });
    }

    static void setToCenter(JFrame frame) {
        Toolkit toolkit = frame.getToolkit();
        Dimension dimension = toolkit.getScreenSize();
        int screenHeight = dimension.height;
        int screenWidth = dimension.width;
        int frmHeight = frame.getHeight();
        int frmWidth = frame.getWidth();
        frame.setLocation((screenWidth - frmWidth) / 2, (screenHeight - frmHeight) / 2);
    }
}
