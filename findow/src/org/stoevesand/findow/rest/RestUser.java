package org.stoevesand.findow.rest;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.stoevesand.finapi.ErrorHandler;
import org.stoevesand.finapi.TokenService;
import org.stoevesand.finapi.UsersService;
import org.stoevesand.finapi.model.FinapiUser;
import org.stoevesand.finapi.model.Token;
import org.stoevesand.findow.model.User;
import org.stoevesand.findow.persistence.PersistanceManager;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/user")
@Api(value = "user")
public class RestUser {

	@Path("/{id}")
	@GET
	@Produces("application/json")
	public String getUser(@PathParam("id") String id, @HeaderParam("password") String password) {

		String result = "";

		User user = PersistanceManager.getInstance().getUserByName(id);
		if ((user != null) && (user.getPassword().equals(password))) {

			try {
				Token userToken = TokenService.requestUserToken(RestUtils.getClientToken(), user.getBackendName(), user.getBackendSecret());
				user.setToken(userToken);
				result = RestUtils.generateJsonResponse(user, "user");
			} catch (ErrorHandler e) {
				result = e.getResponse();
			}

		} else {
			result = RestUtils.generateJsonResponse(Response.USER_OR_PASSWORD_INVALID);
		}

		return result;
	}

	@Path("/{id}")
	@POST
	@Produces("application/json")
	public String createUser(@PathParam("id") String id, @HeaderParam("password") String password) {

		String result = "";
		try {
			User user = PersistanceManager.getInstance().getUserByName(id);

			if (user == null) {
				FinapiUser finapiUser = UsersService.createUser(RestUtils.getClientToken(), null, null);
				user = new User(id, password, finapiUser.getId(), finapiUser.getPassword());
				PersistanceManager.getInstance().store(user);
				result = RestUtils.generateJsonResponse(Response.OK);
			} else {
				result = RestUtils.generateJsonResponse(Response.USER_ALREADY_USED);
			}

		} catch (ErrorHandler e) {
			result = e.getResponse();
		}

		return result;
	}

	@Path("/{id}")
	@DELETE
	@Produces("application/json")
	public String deleteUser(@PathParam("id") String id, @HeaderParam("userToken") String userToken) {

		try {
			User user = PersistanceManager.getInstance().getUserByName(id);
			if (user != null) {
				UsersService.deleteUser(userToken);
			} else {
				return RestUtils.generateJsonResponse(Response.USER_UNKNOWN);
			}

			PersistanceManager.getInstance().deleteUserByName(id);
		} catch (ErrorHandler e) {
			System.out.println(e);
			return e.getResponse();
		}
		return RestUtils.generateJsonResponse(Response.OK);
	}

	@Path("/")
	@GET
	@Produces("application/json")
	@ApiOperation(value = "Get UserInfos of all available users.")
	public String getUserInfos() {
		List<User> userInfos = PersistanceManager.getInstance().getUsers();
		// MandatorAdminService.getUsers(RestUtils.getAdminToken());

		String result = RestUtils.generateJsonResponse(userInfos, "users");

		return result;
	}

}