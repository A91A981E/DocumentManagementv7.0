package cs.whut.common;

/**
 * Created on 23:34 2020/10/19
 */
public class CurrentUser {
    private static User user;

    public static User getUser() {
        return user;
    }

    public static void setUser(User user) {
        CurrentUser.user = user;
    }
}
