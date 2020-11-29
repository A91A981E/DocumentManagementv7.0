package cs.whut.common;

import cs.whut.frame.LoginFrame;
import cs.whut.frame.ServerFrame;
import cs.whut.server_client.Client;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Created on 16:37 2020/9/29
 */
public class MainGUI {
    public static String host = "127.0.0.1";

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                PrintMessage.print("Initial system...");
                PrintMessage.print("User login.");
                ServerFrame.main(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void run(String[] args) throws IOException {
        Client.connectToServer();
        Client.getStreams();

        Client.sendData("TEST_CONNECTION");
        String message = Client.getData();
        if (message.equals("CONNECT_TO_DATABASE")) {
            LoginFrame.main(args);
        } else {
            JOptionPane.showMessageDialog(null, "Client: Unconnected to database.");
        }
    }
}