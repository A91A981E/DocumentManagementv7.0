package cs.whut.common;

import java.io.*;
import java.sql.Timestamp;


public class User implements Serializable{
    private String name;
    private String password;
    private final String role;

    User(String name, String password, String role) {
        this.name = name;
        this.password = password;
        this.role = role;
    }

    public void exitSystem() {
        System.out.println(new Timestamp(System.currentTimeMillis()).toString() + "\t\t" + "System exits.");
        System.exit(0);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

}
