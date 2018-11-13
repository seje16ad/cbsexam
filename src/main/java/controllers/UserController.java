package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import cache.UserCache;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import model.User;
import utils.Hashing;
import utils.Log;

public class UserController {

  private static DatabaseController dbCon;

  public UserController() {
    dbCon = new DatabaseController();
  }

  public static User getUser(int id) {

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where id=" + id;

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user = null;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));

        // return the create object
        return user;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return user;
  }

  /**
   * Get all users in database
   *
   * @return
   */
  public static ArrayList<User> getUsers() {

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL
    String sql = "SELECT * FROM user";

    // Do the query and initialyze an empty list for use if we don't get results
    ResultSet rs = dbCon.query(sql);
    ArrayList<User> users = new ArrayList<User>();

    try {
      // Loop through DB Data
      while (rs.next()) {
        User user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));

        // Add element to list
        users.add(user);
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return the list of users
    return users;
  }

  public static User createUser(User user) {

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);

    // Set creation time for user.
    user.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the user in the DB
    // TODO: Hash the user password before saving it.: FIX
    int userID = dbCon.insert(
            "INSERT INTO user(first_name, last_name, password, email, created_at) VALUES('"
                    + user.getFirstname()
                    + "', '"
                    + user.getLastname()
                    + "', '"
                    //setSalt er metoden der er taget fra Hashing-klassen.
                    + Hashing.setSaltMd5(user.getPassword())
                    + "', '"
                    + user.getEmail()
                    + "', "
                    + user.getCreatedTime()
                    + ")");

    if (userID != 0) {
      //Update the userid of the user before returning
      user.setId(userID);
    } else {
      // Return null if user has not been inserted into database
      return null;
    }

    // Return user
    return user;
  }

  public static boolean deleteUsers(int id) {

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }
    User user = UserController.getUser(id);
    if (user != null) {
      dbCon.deleteUpdate("DELETE from user WHERE id=" + id);
      return true;
    } else {
      return false;
    }
  }

  public static boolean updateUsers(User user, int id) {
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    if (user != null) {
      dbCon.deleteUpdate("UPDATE user SET first_name '" + user.getFirstname() +
              "', last_name = '" + user.getLastname() +
              "', email = '" + user.getEmail() +
              "', password = '" + user.getPassword() +
              "', WHERE id = " + id);
      return true;
    } else {
      return false;
    }

  }
// Login metode
  public static String loginUsers(User userLogin) {
    //Log skal også implementeres i deleteUsers og UpdateUsers. Ellers skal den slettes.
    Log.writeLog(UserController.class.getName(), userLogin,"Login",0);

    //check for db connection
    if (dbCon == null){
      dbCon = new DatabaseController();
    }

    UserCache userCache = new UserCache();
    ArrayList<User> users = userCache.getUsers(false);
  //Timestamp for at skabe en dynamisk variabel - tildeler ny token hver gang.
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    for (User user : users) {
      //Verifier
      if (user.getEmail().equals(userLogin.getEmail()) && user.getPassword().equals(Hashing.setSaltMd5(userLogin.getPassword()))) {

        try {
          //https://github.com/auth0/java-jwt
          Algorithm algorithm = Algorithm.HMAC256("secret");
          String token = JWT.create().withClaim("SEBBEKEY", timestamp).sign(algorithm);
          return token;
        } catch (JWTCreationException exception) {
          //Invalid Signing configuration / Couldn't convert Claims.
        }
      }
    } return null;
  }

}

