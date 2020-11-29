package cs.whut.common;

import java.sql.Timestamp;

/**
 * Created on 21:18 2020/10/19
 */
public class PrintMessage {
    public static void print(String message) {
        System.out.println(new Timestamp(System.currentTimeMillis()).toString() + "\t\t" + message);
    }
}
