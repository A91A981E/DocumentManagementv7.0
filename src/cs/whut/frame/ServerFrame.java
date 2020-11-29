package cs.whut.frame;

import cs.whut.common.MainGUI;
import cs.whut.server_client.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

/**
 * Created on 21:46 2020/10/15
 */
public class ServerFrame extends JFrame{
    private JPanel serverFormPanel;
    private JTextField hostTextField;
    private JTextField portTextField;
    private JButton confirmButton;
    private JButton cancelButton;
    private static JFrame frame;
    static String[] args;


    public ServerFrame() {
        confirmButton.addActionListener(e -> {
            confirmButton.setText("Connecting...");
            MainGUI.host = Client.host = hostTextField.getText();
            Client.port = Integer.parseInt(portTextField.getText());
            try {
                MainGUI.run(args);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            frame.dispose();
        });
        cancelButton.addActionListener(e -> frame.dispose());
        hostTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_ENTER){
                    portTextField.requestFocus();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        portTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_ENTER){
                    confirmButton.setText("Connecting...");
                    MainGUI.host = Client.host = hostTextField.getText();
                    Client.port = Integer.parseInt(portTextField.getText());
                    try {
                        MainGUI.run(args);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    frame.dispose();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        hostTextField.setText("127.0.0.1");
        portTextField.setText("12345");
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                ServerFrame.args = args;
                frame = new JFrame("ServerForm");
                frame.setContentPane(new ServerFrame().serverFormPanel);
                frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                frame.setSize(400,200);
                frame.setLocation(500,300);
                frame.setVisible(true);
                frame.setResizable(false);
            } catch (HeadlessException e) {
                e.printStackTrace();
            }
        });
    }
}
