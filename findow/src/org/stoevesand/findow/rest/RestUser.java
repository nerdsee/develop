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
import org.stoevesand.finapi.MandatorAdminService;
import org.stoevesand.finapi.UsersService;
import org.stoevesand.finapi.model.Token;
import org.stoevesand.finapi.model.User;
import org.stoevesand.finapi.model.UserInfo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/user")
@Api(value="user")
public class RestUser {

	@Path("/{id}")
	@GET
	@Produces("application/json")
	public String getUser(@PathParam("id") String id, @HeaderParam("password") String password) {

		String result = "";

		User user = new User(id, password);
		try {
			Token userToken = user.getToken(RestUtils.getClientToken());
			result = RestUtils.generateJsonResponse(userToken);
		} catch (ErrorHandler e) {
			result = e.getResponse();
		}

		return result;
	}

	@Path("/{id}")
	@POST
	@Produces("application/json")
	public String createUser(@PathParam("id") String id, @HeaderParam("password") String password) {

		String result = "";
		try {
			User user = UsersService.createUser(RestUtils.getClientToken(), id, password);
			Token userToken = user.getToken(RestUtils.getClientToken());

			result = RestUtils.generateJsonResponse(userToken);
		} catch (ErrorHandler e) {
			result = e.getResponse();
		}

		return result;
	}

	@Path("/{id}")
	@DELETE
	@Produces("application/json")
	public String deleteUser(@PathParam("id") String id) {

		String result = "";
		try {
			result = MandatorAdminService.deleteUser(RestUtils.getAdminToken(), id);
		} catch (ErrorHandler e) {
			result = e.getResponse();
		}

		return result;
	}
	
	@Path("/")
	@GET
	@Produces("application/json")
	@ApiOperation(value="Get UserInfos of all available users.")
	public String getUserInfos() {
		List<UserInfo> userInfos = MandatorAdminService.getUsers(RestUtils.getAdminToken());

		String result = RestUtils.generateJsonResponse(userInfos, "userinfos");

		return result;
	}

}