package wdh15.emocracy;

/**
 * Created by madhavajay on 20/06/15.
 */
class UserModel {
    // {"class":"emocracy.User","id":2,"username":"madhava"}
    int id;
    String username;

    public String toString() {
        return " id: " + this.id + " username: " + username;
    }
}