package com.cbsexam;

import cache.UserCache;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.sun.webkit.dom.MediaListImpl;
import controllers.UserController;
import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.User;
import utils.Encryption;
import utils.Log;

@Path("user")
public class UserEndpoints {

  //Implementer UserCache
  private static UserCache userCache = new UserCache();

  /**
   * @param idUser
   * @return Responses
   */
  @GET
  @Path("/{idUser}")
  public Response getUser(@PathParam("idUser") int idUser) {

    // Use the ID to get the user from the controller.
    User user = UserController.getUser(idUser);

    // TODO: Add Encryption to JSON FIX
    // Convert the user object to json in order to return the object
    String json = new Gson().toJson(user);
    ////Tilføjer kryptering, hvor der benyttes XOR
    json = Encryption.encryptDecryptXOR(json);

    // Return the user with the status code 200
    // TODO: What should happen if something breaks down? FIX
    if (user != null) {
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
      // returner en statusfejl 400
    } else {
      return Response.status(400).entity("Could not get user").build();
    }
  }



  /**
   * @return Responses
   */
  @GET
  @Path("/")
  public Response getUsers() {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

    // Get a list of users
    //ArrayList<User> users = UserController.getUsers();
    // Call our controller-layer in order to get the order from the DB
    // Cachen bruges som mellemlager, så data kan hentes hurtigere i browser
    ArrayList<User> users = userCache.getUsers(false);

    // TODO: Add Encryption to JSON :FIX
    // Transfer users to json in order to return it to the user
    String json = new Gson().toJson(users);
    ////Tilføjer kryptering, hvor der benyttes XOR
    json = Encryption.encryptDecryptXOR(json);

    // Return the users with the status code 200
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();

  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createUser(String body) {

    // Read the json from body and transfer it to a user class
    User newUser = new Gson().fromJson(body, User.class);

    // Use the controller to add the user
    User createUser = UserController.createUser(newUser);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createUser);

    // Return the data to the user
    if (createUser != null) {
      userCache.getUsers(true);
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not create user").build();
    }
  }

  // TODO: Make the system able to login users and assign them a token to use throughout the system FIX
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)

  public Response loginUser(String body) {

    // Read the json from body and transfer it to a user class
    User userLogin = new Gson().fromJson(body, User.class);

    //Bruger UserController til at tilføje en token til brugeren når vedkommende logger ind
    String token = UserController.loginUsers(userLogin);

    if (token != null) {
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("logged in with token " + token).build();
    } else {
      return Response.status(400).entity("Could not log in").build();
    }
  }

  // TODO: Make the system able to delete users FIX
  @DELETE
  @Path("/delete/{userID}")
  public Response deleteUser(@PathParam("userID") int id, String body) {

    // Verifier til at tjekke om den token der er givet ved log ind passer
    DecodedJWT token = UserController.verifier(body);

    //Boolean metode til at slette en bruger vha. UserController
    Boolean delete = UserController.deleteUsers(token.getClaim("test").asInt());


    if (delete) {
      userCache.getUsers(true);
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("The user was deleted with id: " + id).build();
    } else {
      return Response.status(400).entity("Could not delete user").build();
    }
  }
  //User user = new Gson().fromJson(body, User.class);
  //boolean update = UserController.updateUsers(user, id);


  // TODO: Make the system able to update users
  @POST
  @Path("/update/{userID}/{token}")

  // Når vi opdaterer en bruger benytter vi brugerens ID + token
  public Response updateUser(@PathParam("userID") int id, @PathParam("token") String token, String body) {

    // Read the json from body and transfer it to a user class
    User user = new Gson().fromJson(body, User.class);

    //Verifier den token som brugeren er tildelt
    DecodedJWT jwt = UserController.verifier(token);
    //Opdaterer brugeren vha. UserControlleren
    Boolean update = UserController.updateUsers(user, jwt.getClaim("test").asInt());




    if (update) {
      userCache.getUsers(true);
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("User was updated " + id).build();
    } else {
      return Response.status(400).entity("Could not update users").build();

    }
  }
}
