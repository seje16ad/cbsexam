package cache;

import controllers.UserController;
import java.util.ArrayList;
import model.User;
import utils.Config;

//TODO: Build this cache and use it.
public class UserCache {

    private ArrayList<User> users;

    private long ttl;

    private long created;

    public UserCache() {this.ttl = Config.getUserTtl(); }

    public ArrayList<User> getUsers(Boolean forceUpdate) {
        if (forceUpdate
                || ((this.created + this.ttl) >= (System.currentTimeMillis() / 1000L))
                || (this.users == null)) {
            ArrayList<User> users = UserController.getUsers();

            this.users = users;
            this.created = System.currentTimeMillis() / 1000L;
        }

        return this.users;
    }
}
