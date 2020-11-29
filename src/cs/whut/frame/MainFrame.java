package cs.whut.frame;

import cs.whut.common.*;
import cs.whut.server_client.Client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

public class MainFrame extends JFrame {
    private final JMenuItem addUserMenuItem;
    private final JMenuItem deleteUserMenuItem;
    private final JMenuItem updateUserMenuItem;
    private final JMenuItem uploadFileMenuItem;
    private final JMenuItem downloadFileMenuItem;
    private final JMenuItem changeSelfInfoMenuItem;
    private final JMenuItem exitMenuItem;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Create the frame.
     */
    public MainFrame() {

        setResizable(false);
        this.mySetTitle(CurrentUser.getUser().getRole());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(400, 300, 700, 200);

        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPanel);
        contentPanel.setLayout(null);

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBounds(0, 0, 1165, 33);
        contentPanel.add(menuBar);

        JMenu userManagerMenu = new JMenu("User_Manage");
        userManagerMenu.setFont(new Font("Consolas", Font.PLAIN, 18));
        menuBar.add(userManagerMenu);

        addUserMenuItem = new JMenuItem("Add");
        addUserMenuItem.addActionListener(e -> addUserActionPerformed());
        addUserMenuItem.setFont(new Font("Consolas", Font.PLAIN, 16));
        userManagerMenu.add(addUserMenuItem);

        updateUserMenuItem = new JMenuItem("Update");
        updateUserMenuItem.addActionListener(e -> updateUserActionPerformed());
        updateUserMenuItem.setFont(new Font("Consolas", Font.PLAIN, 16));
        userManagerMenu.add(updateUserMenuItem);

        deleteUserMenuItem = new JMenuItem("Delete");
        deleteUserMenuItem.addActionListener(e -> delUserActionPerformed());
        deleteUserMenuItem.setFont(new Font("Consolas", Font.PLAIN, 16));
        userManagerMenu.add(deleteUserMenuItem);

        JMenu fileManageMenu = new JMenu("File_Manager");
        fileManageMenu.setFont(new Font("Consolas", Font.PLAIN, 18));
        menuBar.add(fileManageMenu);

        downloadFileMenuItem = new JMenuItem("Download");
        downloadFileMenuItem.addActionListener(e -> downloadFileActionPerformed());
        downloadFileMenuItem.setFont(new Font("Consolas", Font.PLAIN, 16));
        fileManageMenu.add(downloadFileMenuItem);

        uploadFileMenuItem = new JMenuItem("Upload");
        uploadFileMenuItem.addActionListener(e -> uploadFileActionPerformed());
        uploadFileMenuItem.setFont(new Font("Consolas", Font.PLAIN, 16));
        fileManageMenu.add(uploadFileMenuItem);

        JMenu selfInfoMenu = new JMenu("Self_information");
        selfInfoMenu.setFont(new Font("Consolas", Font.PLAIN, 18));
        menuBar.add(selfInfoMenu);

        changeSelfInfoMenuItem = new JMenuItem("Change_Password");
        changeSelfInfoMenuItem.addActionListener(e -> changeSelfActionPerformed());
        changeSelfInfoMenuItem.setFont(new Font("Consolas", Font.PLAIN, 16));
        selfInfoMenu.add(changeSelfInfoMenuItem);

        JMenu otherMenu = new JMenu("Other");
        otherMenu.setFont(new Font("Consolas", Font.PLAIN, 18));
        menuBar.add(otherMenu);

        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(e -> {
            try {
                exitActionPerformed();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        exitMenuItem.setFont(new Font("Consolas", Font.PLAIN, 16));
        otherMenu.add(exitMenuItem);

        JMenuItem logoffMenuItem = new JMenuItem("Log_out");
        logoffMenuItem.addActionListener(e -> {
            try {
                logoutActionPerformed();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        logoffMenuItem.setFont(new Font("Consolas", Font.PLAIN, 16));
        otherMenu.add(logoffMenuItem);

        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                //DataProcessing.disconnectFromDatabase();
                try {
                    Client.sendData("CHANGE_STATUS");
                    Client.getData();
                    Client.sendData(CurrentUser.getUser().getName());
                    Client.sendData("EXIT");
                    Client.closeConnection();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, ex.getLocalizedMessage());
                }
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
        setRights(CurrentUser.getUser().getRole());
    }

    private void logoutActionPerformed() throws IOException {
        this.dispose();
        Client.sendData("CHANGE_STATUS");
        Client.getData();
        Client.sendData(CurrentUser.getUser().getName());
        Client.sendData("USER_LOGOUT");
        PrintMessage.print(CurrentUser.getUser().getName() + " log out.");
        Client.closeConnection();
        ServerFrame.main(null);
    }

    private void addUserActionPerformed() {
        PrintMessage.print(CurrentUser.getUser().getName() + " adds user.");
        String[] pane = new String[2];
        pane[1] = "0";
        UserFrame.main(pane);
    }

    private void delUserActionPerformed() {
        PrintMessage.print(CurrentUser.getUser().getName() + " deletes user.");
        String[] pane = new String[2];
        pane[1] = "2";
        UserFrame.main(pane);
    }

    private void updateUserActionPerformed() {
        PrintMessage.print(CurrentUser.getUser().getName() + " update user information.");
        String[] pane = new String[2];
        pane[1] = "1";
        UserFrame.main(pane);
    }

    private void uploadFileActionPerformed() {
        PrintMessage.print(CurrentUser.getUser().getName() + "upload file.");
        String[] pane = new String[2];
        pane[1] = "1";
        FileFrame.main(pane);
    }

    private void downloadFileActionPerformed() {
        PrintMessage.print(CurrentUser.getUser().getName() + " download file.");
        String[] pane = new String[2];
        pane[1] = "0";
        FileFrame.main(pane);
    }

    private void changeSelfActionPerformed() {
        PrintMessage.print(CurrentUser.getUser().getName() + " change self password.");
        SelfFrame.main(null);
    }

    private void mySetTitle(String role) {
        if (role.equalsIgnoreCase("administrator")) {
            setTitle("Administrator " + CurrentUser.getUser().getName() + " Pane");
        } else if (role.equalsIgnoreCase("browser")) {
            setTitle("Browser " + CurrentUser.getUser().getName() + " Pane");
        } else if (role.equalsIgnoreCase("operator")) {
            setTitle("Operator " + CurrentUser.getUser().getName() + " Pane");
        }
    }

    private void setRights(String role) {
        if (role.equalsIgnoreCase("administrator")) {
            PrintMessage.print("User is administrator.");
            addUserMenuItem.setEnabled(true);
            deleteUserMenuItem.setEnabled(true);
            updateUserMenuItem.setEnabled(true);
            downloadFileMenuItem.setEnabled(true);
            uploadFileMenuItem.setEnabled(false);
            changeSelfInfoMenuItem.setEnabled(true);
            exitMenuItem.setEnabled(true);
        } else if (role.equalsIgnoreCase("browser")) {
            PrintMessage.print("User is browser.");
            addUserMenuItem.setEnabled(false);
            deleteUserMenuItem.setEnabled(false);
            updateUserMenuItem.setEnabled(false);
            downloadFileMenuItem.setEnabled(true);
            uploadFileMenuItem.setEnabled(false);
            changeSelfInfoMenuItem.setEnabled(true);
            exitMenuItem.setEnabled(true);
        } else if (role.equalsIgnoreCase("operator")) {
            PrintMessage.print("User is operator.");
            addUserMenuItem.setEnabled(false);
            deleteUserMenuItem.setEnabled(false);
            updateUserMenuItem.setEnabled(false);
            downloadFileMenuItem.setEnabled(true);
            uploadFileMenuItem.setEnabled(true);
            changeSelfInfoMenuItem.setEnabled(true);
            exitMenuItem.setEnabled(true);
        }
    }

    private void exitActionPerformed() throws IOException {
        this.dispose();
        Client.sendData("CHANGE_STATUS");
        Client.getData();
        Client.sendData(CurrentUser.getUser().getName());
        Client.sendData("EXIT");
        Client.closeConnection();
        JOptionPane.showMessageDialog(null, "System exit. Thanks for utilizing.");
        CurrentUser.getUser().exitSystem();
        LoginFrame.main(null);
    }
}
