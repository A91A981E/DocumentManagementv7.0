package cs.whut.common;

import java.sql.Timestamp;

/**
 * Created on 17:26 2020/10/24
 */
public class PrintMessageWithThreadNum {
    public static void print(String message, long num) {
        System.out.println(new Timestamp(System.currentTimeMillis()).toString() + "\t\tThread " + num + ": " + message);
    }
}
