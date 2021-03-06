package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.User;
import models.UserGroup;
import play.Logger;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import repositories.UserRepository;
import util.ActionAuthenticator;
import util.JsonKeys;
import util.JsonUtil;
import util.exceptions.InvalidInputException;
import util.exceptions.NotAuthorizedException;

import java.util.List;
import java.util.Map;

public class UserController extends Controller {

    /**
     * Return all users in the database.
     *
     * @return HTTP Status OK with a list of all users.
     */
    public Result getUserList() {
        Map<String, String[]> urlParams = Controller.request().queryString();
        List<User> users = UserRepository.getUsers(urlParams);
        return ok(JsonUtil.toJson(users));
    }


    public Result getUserGroups(Long id) {
        try {
            List<UserGroup> group = User.find.byId(id).getUserGroups();
            return ok(JsonUtil.toJson(group));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, ""));
    }

    /**
     * Either PATCHes single values or PUTs all values into the entity with the specified id.
     *
     * @return HTTP Status ok when everything works out or badRequest if not.
     */
    @Security.Authenticated(ActionAuthenticator.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateUser(Long id) {
        try {
            JsonNode json = request().body().asJson();
            Map<String, String[]> urlParams = Controller.request().queryString();
            String updateMethod = request().method();

            User u = UserRepository.changeUser(id, request().username(), json, urlParams, updateMethod);

        } catch (NullPointerException e) {
            e.printStackTrace();
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, no user with the specified id exists.", id));
        } catch (InvalidInputException e) {
            e.printStackTrace();
            if (JsonKeys.debugging) {
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, e.getMessage() + " | cause: " + e.getCause()));
            } else {
                return badRequest(JsonUtil
                        .prepareJsonStatus(
                                BAD_REQUEST, e.getMessage()));
            }
        } catch (NotAuthorizedException e) {
            e.printStackTrace();
            return unauthorized(JsonUtil.prepareJsonStatus(UNAUTHORIZED, e.getMessage(), id));
        }
        return ok(JsonUtil.prepareJsonStatus(OK, "User has been changed.", id));

    }

    /**
     * Returns the user with a specific ID.
     *
     * @param id of the user
     * @return HTTP Status Result OK if found or NOT_FOUND if not found.
     */
    public Result getUser(Long id) {
        User u = UserRepository.findById(id);
        if (u == null)
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, no user with the specified id exists.", id));

        if (JsonKeys.debugging) if (JsonKeys.debugging) Logger.debug(u + "| USER_NAME Key=" + JsonKeys.USER_NAME);
        return ok(JsonUtil.toJson(u));
    }

    /**
     * Returns the user with a specific (unique!) Email.
     *
     * @param email of the user
     * @return OK when found, NOT_FOUND if it doesnt exist.
     */
    public Result getUserByEmail(String email) {
        // Find a task by ID
        User u = UserRepository.findUserByEmail(email);
        if (u == null)
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND,
                    "The user with the email=" + email + " could not be found."));
        return ok(JsonUtil.toJson(u));
    }


    /**
     * Deletes a user with the given id.
     *
     * @param id of the user
     * @return ok if found else not found or unauthorized
     */
    @Security.Authenticated(ActionAuthenticator.class)
    public Result deleteUser(Long id) {
        try {
            UserRepository.deleteUserById(id, request().username());

            return ok(JsonUtil.prepareJsonStatus(OK, "The user has been deleted. All produced content now will be unlinked from this account (author set to null).", id));
        } catch (NullPointerException e) {
            return notFound(JsonUtil.prepareJsonStatus(NOT_FOUND, "Error, user does not exist.", id));
        } catch (NotAuthorizedException e) {
            return unauthorized(JsonUtil.prepareJsonStatus(UNAUTHORIZED, e.getMessage(), id));
        }
    }

    /**
     * Adds a new user to the database, throws an error if the email, name or
     * password are missing.
     *
     * @return appropriate response depending on errors or ok with user id.
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result addUser() {
        JsonNode json = request().body().asJson();
        User u;
        try {
            u = UserRepository.createUser(json);
        } catch (InvalidInputException e) {
            return badRequest(JsonUtil
                    .prepareJsonStatus(
                            BAD_REQUEST,
                            e.getMessage()));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return internalServerError(JsonUtil
                    .prepareJsonStatus(
                            INTERNAL_SERVER_ERROR,
                            "Error in PasswordUtil."));
        } catch (IllegalArgumentException e) {
            return badRequest(JsonUtil
                    .prepareJsonStatus(
                            BAD_REQUEST,
                            "Body did contain elements that are not allowed/expected. A user can contain: " + JsonKeys.USER_JSON_ELEMENTS));
        } catch (Exception e) {
            e.printStackTrace();
            return forbidden(JsonUtil.prepareJsonStatus(FORBIDDEN,
                    "The user could not be created, a user group has to be set via PATCH or PUT. It may not be content of POST."));
        }
        return created(JsonUtil.prepareJsonStatus(CREATED, "User has been created.", u.getId()));
    }

}
