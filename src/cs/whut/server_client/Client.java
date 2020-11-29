package cs.whut.server_client;

import cs.whut.common.Doc;
import cs.whut.common.PrintMessage;
import cs.whut.common.User;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class Client {

    private static Socket client;
    private static ObjectInputStream input;
    private static ObjectOutputStream output;

    public static String host = "127.0.0.1";
    public static int port = 12345;

    public static void connectToServer() {
        PrintMessage.print("Try to connect to server...");
        try {
            client = new Socket(host, port);
            PrintMessage.print("Connected to :" + client.getInetAddress().getHostName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
            System.exit(0);
        }
    }

    public static void getStreams() throws IOException {
        output = new ObjectOutputStream(client.getOutputStream());
        output.flush();
        input = new ObjectInputStream(client.getInputStream());
        PrintMessage.print("IO constructed successfully");
    }
  
    public static void closeConnection() throws IOException {
        output.close();
        input.close();
        client.close();
    }

    public static void sendData(String message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getData() throws IOException {
        String message = null;
        try {
            message = (String) input.readObject();
        } catch (ClassNotFoundException e) {
            e.getStackTrace();
        }

        PrintMessage.print("SERVER>>> " + message);
        return message;
    }

    public static User getUser() throws IOException, ClassNotFoundException {
        User user;
        user = (User) input.readObject();
        return user;
    }

    public static User[] getUserSet() throws IOException, ClassNotFoundException {
        User[] e;
        e = (User[]) input.readObject();
        return e;
    }

    public static Doc[] getDocSet() throws IOException, ClassNotFoundException {
        Doc[] e;
        e = (Doc[]) input.readObject();
        return e;
    }

    private static String getPath(JFrame frame, String name) {
        FileDialog fileDialog = new FileDialog(frame, "Choose path");
        fileDialog.setMode(FileDialog.SAVE);
        fileDialog.setFile(name);
        fileDialog.setVisible(true);
        return fileDialog.getDirectory();
    }

    public static void receivedFile(JFrame frame) {
        try {
            String fileName = input.readUTF();
            String path = getPath(frame, fileName);
            File directory = new File(path);
            if (!directory.exists()) {
                if (directory.mkdir()) {
                    PrintMessage.print("Create file directory");
                }
            }
            FileOutputStream fileOutputStream = new FileOutputStream(new File(directory.getAbsolutePath() + File.separatorChar + fileName));

            PrintMessage.print("======== Start transport ========");
            byte[] bytes = new byte[1024];
            int length;
            while ((length = input.read(bytes, 0, bytes.length)) != -1) {
                fileOutputStream.write(bytes, 0, length);
                fileOutputStream.flush();
            }
            PrintMessage.print("============= Succeed ============");
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendFile(File file) {
        try (FileInputStream ignored = new FileInputStream(file)) {
            output.writeUTF(file.getName());
            output.flush();
            output.writeLong(file.length());
            output.flush();
            PrintMessage.print("======== Start transport ========");
            output.writeObject(file);
            PrintMessage.print("========== Succeed ==========");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}