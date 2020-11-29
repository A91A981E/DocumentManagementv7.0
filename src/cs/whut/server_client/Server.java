package cs.whut.server_client;

import cs.whut.common.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Objects;

public class Server extends Thread {
    private static ServerSocket server;
    private long threadNum;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private final Socket connection;

    public static void main(String[] args) {
        int count = 0;
        try {
            server = new ServerSocket(12345);
            initialSystem();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            while (true) {
                PrintMessage.print("Thread Main: Waiting for connection...");
                Socket connection = server.accept();
                PrintMessage.print("Thread Main: Connection " + count + " linked:" + connection.getInetAddress() + ":"
                        + connection.getPort());
                Server thread = new Server(connection);
                thread.setThreadNum(thread.getId());
                thread.start();
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        DataProcessing.disconnectFromDatabase();
    }

    public static void initialSystem() {
        String url = "jdbc:mysql://127.0.0.1:3306/document";
        String user = "root";
        String password = "123456";
        String driverName = "com.mysql.cj.jdbc.Driver";
        DataProcessing.connectToDatabase(driverName, url, user, password);
    }

    public Server(Socket connection) throws IOException {
        this.connection = connection;
        input = new ObjectInputStream(connection.getInputStream());
    }

    public void setThreadNum(long threadNum) {
        this.threadNum = threadNum;
    }

    public void sendMessage(String message) throws IOException {
        output.writeObject(message);
        output.flush();
    }

    @Override
    public void run() {
        String message = "";
        try {
            output = new ObjectOutputStream(connection.getOutputStream());
            PrintMessageWithThreadNum.print("IO Constructed successfully", threadNum);
        } catch (IOException e) {
            e.printStackTrace();
        }
        do {
            try {
                message = (String) input.readObject();
                PrintMessageWithThreadNum.print("CLIENT>>> " + message, threadNum);
                switch (message) {
                    case "TEST_CONNECTION":
                        if (DataProcessing.isConnectedToDatabase()) {
                            sendMessage("CONNECT_TO_DATABASE");
                        } else {
                            sendMessage("DIDN'T_CONNECT_TO_DATABASE");
                        }
                        break;
                    case "LOGIN": {
                        sendMessage("LOGIN");
                        String name, password;
                        name = (String) input.readObject();
                        password = (String) input.readObject();
                        login(name, password);
                        break;
                    }
                    case "CHANGE": {
                        sendMessage("CHANGE");
                        String name, oldPassword, newPassword, confirmPassword, role;
                        name = (String) input.readObject();
                        oldPassword = (String) input.readObject();
                        newPassword = (String) input.readObject();
                        confirmPassword = (String) input.readObject();
                        role = (String) input.readObject();
                        change(name, oldPassword, newPassword, confirmPassword, role);
                        break;
                    }
                    case "ADD": {
                        sendMessage("ADD");
                        String name, password, role;
                        name = (String) input.readObject();
                        password = (String) input.readObject();
                        role = (String) input.readObject();
                        add(name, password, role);
                        break;
                    }
                    case "DELETE": {
                        sendMessage("DELETE");
                        String name;
                        name = (String) input.readObject();
                        delete(name);
                        break;
                    }
                    case "UPDATE": {
                        sendMessage("UPDATE");
                        String name, password, role;
                        name = (String) input.readObject();
                        password = (String) input.readObject();
                        role = (String) input.readObject();
                        update(name, password, role);
                        break;
                    }
                    case "GET_USER":
                        sendUser();
                        break;
                    case "GET_DOC":
                        sendDoc();
                        break;
                    case "UPLOAD":
                        sendMessage("UPLOAD");
                        receiveFile();
                        break;
                    case "DOWNLOAD":
                        sendMessage("DOWNLOAD");
                        message = (String) input.readObject();
                        sendFile(message);
                        break;
                    case "CHANGE_STATUS":
                        sendMessage("USER_CHANGE_STATUS");
                        String name;
                        name = (String) input.readObject();
                        changeStatus(name);
                        break;
                }
            } catch (ClassNotFoundException | IOException classNotFoundException) {
                PrintMessageWithThreadNum.print("Unknown object type received", threadNum);
            }
        } while (!Objects.equals(message, "EXIT") && !Objects.equals(message, "USER_LOGOUT"));
        closeConnection();
    }

    synchronized private void changeStatus(String name) {
        DataProcessing.changeStatus(name);
    }

    synchronized private void update(String name, String password, String role)
            throws IOException, ClassNotFoundException {
        if (DataProcessing.update(threadNum, name, password, role)) {
            sendMessage("USER_UPDATE");
            input.readObject();
            sendUser();
            input.readObject();
            sendUser();
        } else {
            sendMessage("User doesn't exist.");
        }
    }

    synchronized private void delete(String name) throws IOException {
        if (DataProcessing.delete(threadNum, name)) {
            sendMessage("USER_DELETE");
        } else {
            sendMessage("user doesn't exist");
        }
    }

    synchronized private void add(String name, String password, String role) throws IOException {
        if (DataProcessing.insert(threadNum, name, password, role)) {
            sendMessage("USER_ADD");
            PrintMessageWithThreadNum.print("USER_ADD", threadNum);
        } else {
            sendMessage("User has existed.");
        }
    }

    synchronized private void change(String name, String oldPassword, String newPassword, String confirmPassword,
            String role) throws IOException {
        if (DataProcessing.searchUser(threadNum, name) == null) {
            PrintMessageWithThreadNum.print("Cannot find " + name, threadNum);
            sendMessage("User don't exist.");
            return;
        }

        if (DataProcessing.searchUser(threadNum, name, oldPassword) == null) {
            PrintMessageWithThreadNum.print("The previous password is incorrect.", threadNum);
            sendMessage("Old Password is incorrect.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            PrintMessageWithThreadNum.print("Two new passwords are different.", threadNum);
            sendMessage("New Passwords are different.");
            return;
        }

        if (DataProcessing.update(threadNum, name, newPassword, role)) {
            sendMessage("USER_UPDATE");
        } else {
            sendMessage("Password change fail.");
        }
    }

    synchronized private void login(String name, String password) throws IOException {
        User user;
        if (DataProcessing.searchUser(threadNum, name) == null) {
            sendMessage("user don't exist.");
            return;
        }

        if (DataProcessing.getUserStatus(name)) {
            sendMessage("User has login.");
            return;
        }

        if ((user = DataProcessing.searchUser(threadNum, name, password)) == null) {
            sendMessage("username or password is incorrect.");
            return;
        }
        changeStatus(name);
        sendMessage("USER_LOGIN:" + name);
        output.writeObject(user);
        output.flush();
    }

    synchronized public void closeConnection() {
        try {
            output.close();
            input.close();
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized public void sendFile(String fileID) {
        try {
            InputStream inputStream = DataProcessing.downloadDoc(fileID);
            if (inputStream == null) {
                return;
            }
            String filename = DataProcessing.filename;
            output.writeUTF(filename);
            output.flush();

            byte[] bytes = new byte[1024];
            int length;
            long size = 0;
            while ((length = inputStream.read(bytes, 0, bytes.length)) != -1) {
                output.write(bytes, 0, length);
                size += length;
                output.flush();
            }

            inputStream.close();
            PrintMessageWithThreadNum.print(
                    "======== File has been sent [File Name：" + fileID + "] [Size：" + size + "]=======", threadNum);
            sendMessage("USER_DOWNLOAD");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    synchronized public void sendUser() throws IOException {
        Enumeration<User> users = DataProcessing.getAllUser();
        ArrayList<User> u = new ArrayList<>();

        while (users.hasMoreElements()) {
            u.add(users.nextElement());
        }

        User[] user = new User[u.size()];
        int i = 0;
        for (User temp : u) {
            user[i] = temp;
            i++;
        }
        output.writeObject(user);
        output.flush();
    }

    synchronized public void sendDoc() throws IOException {
        Enumeration<Doc> docs = DataProcessing.getAllDocs();
        ArrayList<Doc> d = new ArrayList<>();

        while (docs.hasMoreElements()) {
            d.add(docs.nextElement());
        }
        Doc[] doc = new Doc[d.size()];
        int i = 0;
        for (Doc temp : d) {
            doc[i] = temp;
            i++;
        }
        output.writeObject(doc);
        output.flush();
    }

    synchronized public void receiveFile() {
        try {
            String fileName = input.readUTF();
            long length = input.readLong();
            File file = (File) input.readObject();
            String id, description, creator;
            id = (String) input.readObject();
            description = (String) input.readObject();
            creator = (String) input.readObject();

            if (DataProcessing.insertDoc(threadNum, id, creator, new Timestamp(System.currentTimeMillis()), description,
                    fileName, file)) {
                sendMessage("USER_UPLOAD");
            } else {
                sendMessage("Upload failed.");
            }
            PrintMessageWithThreadNum.print("======== File has been received [File Name：" + fileName + "] [Size："
                    + getFormatFileSize(length) + "] ========", threadNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    synchronized private String getFormatFileSize(long length) {
        double size = ((double) length) / (1 << 30);
        if (size >= 1) {
            return size + "GB";
        }
        size = ((double) length) / (1 << 20);
        if (size >= 1) {
            return size + "MB";
        }
        size = ((double) length) / (1 << 10);
        if (size >= 1) {
            return size + "KB";
        }
        return length + "B";
    }
}
