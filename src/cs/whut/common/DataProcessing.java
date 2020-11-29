package cs.whut.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.*;
import java.util.Enumeration;
import java.util.Hashtable;

public class DataProcessing {
    private static Connection connection;
    private static Statement statement;
    private static ResultSet resultSet;

    public static boolean isConnectedToDatabase() {
        return connectedToDatabase;
    }

    private static boolean connectedToDatabase = false;
    public static final Hashtable<String, Doc> docs;
    public static final Hashtable<String, User> users;
    public static String filename;

    static {
        docs = new Hashtable<>();
        users = new Hashtable<>();
    }

    synchronized public static void changeStatus(String name) {
        try {
            String sql = "select login from user_info where username = '" + name + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            boolean status = resultSet.getBoolean("login");
            sql = "update user_info set login = " + !status + " where username ='" + name + "'";
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    synchronized public static boolean getUserStatus(String name) {
        try {
            String sql = "select login from user_info where username = '" + name + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            return resultSet.getBoolean("login");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    synchronized public static void connectToDatabase(String driverName, String url, String name, String password) {
        try {
            Class.forName(driverName);
            connection = DriverManager.getConnection(url, name, password);
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            connectedToDatabase = true;
            PrintMessage.print("Thread Main:Successfully connect to Database.");
        } catch (ClassNotFoundException e) {
            PrintMessage.print("Thread Main:Data loading error.");
            PrintMessage.print(e.getLocalizedMessage());
            return;
        } catch (SQLException e) {
            PrintMessage.print("Thread Main:SQL error");
            PrintMessage.print(e.getLocalizedMessage());
            return;
        }
        try {
            loadAllUsers();
            loadAllDoc();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    synchronized public static void disconnectFromDatabase() {
        if (connectedToDatabase) {
            try {
                resultSet.close();
                statement.close();
                connection.close();
                connectedToDatabase = false;
                PrintMessage.print("Thread Main：Successfully disconnected from database.");
            } catch (SQLException e) {
                PrintMessage.print("Thread Main：Failed in disconnecting from database.");
                e.printStackTrace();
            }
            connectedToDatabase = false;
        }
    }

    synchronized private static void loadAllUsers() throws SQLException {
        users.clear();
        User temp;
        if (!connectedToDatabase) {
            throw new SQLException("Not connect to database.");
        }

        String sql = "select * from user_info";
        resultSet = statement.executeQuery(sql);

        while (resultSet.next()) {
            String username = resultSet.getString("username");
            String password = resultSet.getString("password");
            String role = resultSet.getString("role");
            temp = new User(username, password, role);
            users.put(username, temp);
        }
    }

    synchronized private static void loadAllDoc() throws SQLException {
        docs.clear();
        Doc temp;
        if (!connectedToDatabase) {
            throw new SQLException("Not connect to database.");
        }

        String sql = "select * from doc_info";
        resultSet = statement.executeQuery(sql);

        while (resultSet.next()) {
            String ID = resultSet.getString("Id");
            String creator = resultSet.getString("creator");
            Timestamp timestamp = resultSet.getTimestamp("timestamp");
            String description = resultSet.getString("description");
            String filename = resultSet.getString("filename");
            ID = String.format("%04d", Integer.valueOf(ID));
            temp = new Doc(ID, creator, timestamp, description, filename);
            docs.put(ID, temp);
        }
    }

    synchronized public static Enumeration<Doc> getAllDocs() {
        return docs.elements();
    }

    synchronized public static Enumeration<User> getAllUser() {
        return users.elements();
    }

    synchronized public static boolean insertDoc(long threadNum, String id, String creator, Timestamp timestamp, String description, String filename, File inFile) {
        Doc doc;

        if (docs.containsKey(id)) {
            PrintMessageWithThreadNum.print("File id " + id + " has existed.", threadNum);
            return false;
        } else {
            doc = new Doc(id, creator, timestamp, description, filename);
            docs.put(id, doc);
            PrintMessageWithThreadNum.print("File " + id + " upload successfully.", threadNum);
            try {
                PrintMessageWithThreadNum.print("File " + id + " upload to database.", threadNum);
                String sql = "INSERT INTO doc_info VALUES(?,?,?,?,?,?)";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, id);
                preparedStatement.setString(2, creator);
                preparedStatement.setString(3, timestamp.toString());
                preparedStatement.setString(4, description);
                preparedStatement.setString(5, filename);
                FileInputStream fileInputStream = new FileInputStream(inFile);
                preparedStatement.setBinaryStream(6, fileInputStream, inFile.length());
                preparedStatement.executeUpdate();
                preparedStatement.close();
                PrintMessageWithThreadNum.print("File " + id + " upload successfully to database.", threadNum);
                loadAllDoc();
            } catch (SQLException | FileNotFoundException e) {
                PrintMessage.print("File " + id + " upload ERROR.");
                PrintMessage.print(e.getLocalizedMessage());
            }
            return true;
        }
    }

    synchronized public static InputStream downloadDoc(String fileID) {
        try {
            String sql = "SELECT * FROM doc_info WHERE Id='" + fileID + "'";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            DataProcessing.filename = resultSet.getString("filename");
            return resultSet.getBlob("file").getBinaryStream();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    synchronized public static User searchUser(long threadNum, String name) {
        if (users.containsKey(name)) {
            PrintMessageWithThreadNum.print("Search user " + name + "...", threadNum);
            return users.get(name);
        }
        PrintMessageWithThreadNum.print("User " + name + " is not found.", threadNum);
        return null;
    }

    synchronized public static User searchUser(long threadNum, String name, String password) {
        if (users.containsKey(name)) {
            User temp = users.get(name);
            PrintMessageWithThreadNum.print("Search user [" + name + "] with password [" + password + "]...", threadNum);
            if ((temp.getPassword()).equals(password))
                return temp;
        }
        PrintMessageWithThreadNum.print("User [" + name + "] with password [" + password + "] is not found.", threadNum);
        return null;
    }

    synchronized public static boolean update(long threadNum, String name, String password, String role) {
        User user;
        if (users.containsKey(name)) {
            users.remove(name);
            if (role.equalsIgnoreCase("administrator"))
                user = new Administrator(name, password, role);
            else if (role.equalsIgnoreCase("operator"))
                user = new Operator(name, password, role);
            else
                user = new Browser(name, password, role);
            users.put(name, user);
            PrintMessageWithThreadNum.print("Successfully changed [" + name + "]'s password to [" + password + "].", threadNum);
            try {
                String sql = "update user_info set password='" + password + "',role='" + role + "' where username='" + name + "'";
                statement.executeUpdate(sql);
                loadAllUsers();
                PrintMessageWithThreadNum.print("Successfully changed [" + name + "]'s password to [" + password + "] in database.", threadNum);
            } catch (SQLException e) {
                PrintMessageWithThreadNum.print("Failed in changing [" + name + "]'s password to [" + password + "] in database.", threadNum);
                PrintMessageWithThreadNum.print(e.getLocalizedMessage(), threadNum);
            }
            return true;
        } else {
            PrintMessageWithThreadNum.print("User [" + name + "] does not exist.", threadNum);
            return false;
        }
    }

    synchronized public static boolean insert(long threadNum, String name, String password, String role) {
        User user;
        if (users.containsKey(name)) {
            PrintMessageWithThreadNum.print("User [" + name + "] has existed. Add user failed.", threadNum);
            return false;
        } else {
            if (role.equalsIgnoreCase("administrator"))
                user = new Administrator(name, password, role);
            else if (role.equalsIgnoreCase("operator"))
                user = new Operator(name, password, role);
            else
                user = new Browser(name, password, role);
            users.put(name, user);
            PrintMessageWithThreadNum.print("Successfully add user name[" + name + "] password[" + password + "] role[" + role + "].", threadNum);
            try {
                String sql = "insert into user_info (username,password,role) values(?,?,?)";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, password);
                preparedStatement.setString(3, role);
                preparedStatement.executeUpdate();
                loadAllUsers();
                PrintMessageWithThreadNum.print("Successfully add user name[" + name + "] password[" + password + "] role[" + role + "] into database.", threadNum);
            } catch (SQLException e) {
                PrintMessageWithThreadNum.print("Failed in adding user name[" + name + "] password[" + password + "] role[" + role + "] into database.", threadNum);
                PrintMessageWithThreadNum.print(e.getLocalizedMessage(), threadNum);
                e.printStackTrace();
            }
            return true;
        }
    }

    synchronized public static boolean delete(long threadNum, String name) {
        if (users.containsKey(name)) {
            PrintMessageWithThreadNum.print("Delete user [" + name + "]", threadNum);
            users.remove(name);
            try {
                String sql = "delete from user_info where username='" + name + "'";
                statement.executeUpdate(sql);
                loadAllUsers();
                PrintMessageWithThreadNum.print("Successfully delete user [" + name + "] from database.", threadNum);
            } catch (SQLException e) {
                PrintMessageWithThreadNum.print("Failed in deleting user [" + name + "] from database.", threadNum);
                PrintMessageWithThreadNum.print(e.getLocalizedMessage(), threadNum);
                e.printStackTrace();
            }
            return true;
        }
        PrintMessageWithThreadNum.print("User [" + name + "] does not exist. Delete failed.", threadNum);
        PrintMessageWithThreadNum.print("User [" + name + "] does not exist. Delete failed.", threadNum);
        return false;
    }
}