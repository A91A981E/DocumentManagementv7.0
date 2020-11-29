package cs.whut.frame;

import cs.whut.common.CurrentUser;
import cs.whut.common.Doc;
import cs.whut.common.User;
import cs.whut.server_client.Client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Created on 14:43 2020/9/29
 */
public class FileFrame extends JFrame {
    private JTabbedPane downloadTabbedPane;
    private JTabbedPane uploadTabbedPane;
    private JPanel fileFrame;
    private JPanel downloadPanel;
    private JPanel uploadPanel;
    private JTable downloadTable;
    private JScrollPane downloadScrollPane;
    private JButton downloadButton;
    private JButton cancelButton;
    private JButton browseButton;
    private JButton confirmButton;
    private JButton cancelButton1;
    private JTextField idTextField;
    private JTextField descriptionTextField;
    private JTextField pathTextField;
    private final User user;
    private JLabel idLabel;
    static JFrame frame;

    public FileFrame(String pane) throws IOException, ClassNotFoundException {
        SetPane(pane);
        user= CurrentUser.getUser();
        downloadButton.addActionListener(e -> downloadActionPerformed());
        cancelButton.addActionListener(e -> frame.dispose());

        constructTable();
        confirmButton.addActionListener(e -> {
            try {
                uploadActionPerformed();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        cancelButton1.addActionListener(e -> frame.dispose());
        browseButton.addActionListener(e -> {
            String path = getOpenPath();
            pathTextField.setText(path);
        });

        if (!user.getRole().equalsIgnoreCase("operator")) {
            idTextField.setEditable(false);
            idTextField.setEnabled(false);
            pathTextField.setEditable(false);
            pathTextField.setEnabled(false);
            descriptionTextField.setEnabled(false);
            descriptionTextField.setEditable(false);
            browseButton.setEnabled(false);
            confirmButton.setEnabled(false);
        }
    }

    private void SetPane(String value) {
        if (value.equals("0")) {
            downloadTabbedPane.setSelectedComponent(downloadPanel);
        } else {
            downloadTabbedPane.setSelectedComponent(uploadTabbedPane);
        }
    }

    private String getOpenPath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath() + "\\";
        }
        return null;
    }

    private boolean hasId(String id) throws IOException, ClassNotFoundException {
        Client.sendData("GET_DOC");
        Doc[] doc = Client.getDocSet();
        for(Doc temp: doc){
            if(temp.getID().equals(id)){
                return true;
            }
        }
        return false;
    }

    private void uploadActionPerformed() throws IOException, ClassNotFoundException {
        String path = pathTextField.getText();
        String id = idTextField.getText();
        String description = descriptionTextField.getText();
        if (path.equals("")) {
            JOptionPane.showMessageDialog(null, "No file has been selected.");
            return;
        }
        idTextField.setText("");
        pathTextField.setText("");
        descriptionTextField.setText("");
        if (hasId(id)) {
            JOptionPane.showMessageDialog(null,"ID has existed.");
            return ;
        }
        Client.sendData("UPLOAD");
        if (Client.getData().equals("UPLOAD")) {
            Client.sendFile(new File(path));
            Client.sendData(id);
            Client.sendData(description);
            Client.sendData(user.getName());

            String message = Client.getData();
            if (message.equals("USER_UPLOAD")) {
                JOptionPane.showMessageDialog(null,"Upload success");
            }else{
                JOptionPane.showMessageDialog(null,message);
            }
        }
        JOptionPane.showMessageDialog(null, "Upload successfully.");
        constructTable();
    }

    private void downloadActionPerformed() {
        int selectedRow = downloadTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "No file has been selected.");
        } else {
            String fileId = (String) downloadTable.getValueAt(selectedRow, 0);
            if (fileId == null) {
                JOptionPane.showMessageDialog(null, "You choose a blink line.");
                return;
            }
            int value = JOptionPane.showConfirmDialog(null, "Are you sure to download this file?", "Confirm", JOptionPane.OK_CANCEL_OPTION);
            if (value == 0) {
                try {
                    Client.sendData("DOWNLOAD");
                    if (Client.getData().equals("DOWNLOAD")) {
                        Client.sendData(fileId);
                        Client.receivedFile(this);
                        String message=Client.getData();
                        if(message.equals("USER_DOWNLOAD")){
                            constructTable();
                            JOptionPane.showMessageDialog(null, "Download succeed.");
                        }else{
                            JOptionPane.showMessageDialog(null,message);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "File has existed.\nDownload failed.");
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    JOptionPane.showMessageDialog(null, "NullPointerException");
                }
            }
        }
    }

    private void constructTable() throws IOException, ClassNotFoundException {
        String[] header = {"Document ID", "Creator", "Time", "Document name", "Description"};
        String[][] files = new String[50][5];
        Client.sendData("GET_DOC");
        Doc[] doc = Client.getDocSet();
        int row = 0;
        for(Doc temp: doc) {
            files[row][0] = temp.getID();
            files[row][1] = temp.getCreator();
            files[row][2] = temp.getTimestamp().toString();
            files[row][3] = temp.getFilename();
            files[row][4] = temp.getDescription();
            row++;
        }

        downloadTable.setModel(new DefaultTableModel(files, header) {
            final boolean[] columnEditable = new boolean[]{false, false, false, false, false};
            public boolean isCellEditable(int row, int column) {
                return columnEditable[column];
            }
        });
        downloadTable.getTableHeader().setReorderingAllowed(false);
        downloadScrollPane.setViewportView(downloadTable);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                frame = new JFrame("FileFrame");
                frame.setContentPane(new FileFrame(args[1]).fileFrame);
                frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}