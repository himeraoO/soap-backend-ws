package com.example.soapbackendws.webservice;

import com.example.soapbackendws.repository.entity.Role;
import com.example.soapbackendws.repository.entity.User;
import com.example.soapbackendws.service.UserService;
import com.example.soapbackendws.soapws.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Endpoint
public class UsersEndpoint {
    private static final String NAMESPACE_URI = "http://www.example.com/soapbackendws/soapws";

    private UserService userService;

    @Autowired
    public UsersEndpoint(UserService userService) {
        this.userService = userService;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getUsersRequest")
    @ResponsePayload
    public GetUsersResponse getAllUsers() {
        GetUsersResponse response = new GetUsersResponse();
        List<UserDetails> userDetailsList = new ArrayList<>();
        List<User> userListList = userService.findAll();
        for (int i = 0; i < userListList.size(); i++) {
            UserDetails ob = new UserDetails();
            User user = userListList.get(i);
            ob.setLogin(user.getLogin());
            ob.setUsername(user.getUsername());
            userDetailsList.add(ob);
        }
        response.getUserDetails().addAll(userDetailsList);
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getUserDetailsByLoginRequest")
    @ResponsePayload
    public GetUserDetailsByLoginResponse getUser(@RequestPayload GetUserDetailsByLoginRequest request) {
        GetUserDetailsByLoginResponse response = new GetUserDetailsByLoginResponse();
        UserDetailsWithRoles userDetails = new UserDetailsWithRoles();
        User user = userService.findByLogin(request.getLogin());
        userDetails.setLogin(user.getLogin());
        userDetails.setUsername(user.getUsername());
        Set<Role> roleSet = user.getRoles();
        for (Role r: roleSet) {
            RoleDetails roleDetails = new RoleDetails();
            roleDetails.setId(r.getId());
            roleDetails.setName(r.getName());
            userDetails.getRoles().add(roleDetails);
        }
        response.setUserDetailsWithRoles(userDetails);
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "deleteUserDetailsRequest")
    @ResponsePayload
    public DeleteUserDetailsResponse deleteUser(@RequestPayload DeleteUserDetailsRequest request) {
        User user = userService.findByLogin(request.getLogin());
        ServiceStatus serviceStatus = new ServiceStatus();
        if (user == null ) {
            serviceStatus.setStatusCode("FALSE");
            serviceStatus.setMessage("Content Not Available");
        } else {
            userService.delete(user);
            serviceStatus.setStatusCode("SUCCESS");
            serviceStatus.setMessage("Content Deleted Successfully");
        }
        DeleteUserDetailsResponse response = new DeleteUserDetailsResponse();
        response.setServiceStatus(serviceStatus);
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "addUserDetailsRequest")
    @ResponsePayload
    public AddUserDetailsResponse addUser(@RequestPayload AddUserDetailsRequest request) {
        AddUserDetailsResponse response = new AddUserDetailsResponse();
        ServiceStatus serviceStatus = new ServiceStatus();
        User userBD = userService.findByLogin(request.getLogin());
        if(userBD == null){
            User user = new User();
            user.setLogin(request.getLogin());
            user.setUsername(request.getName());
            user.setPassword(request.getPassword());
            List<Long> listRolesIds = request.getRolesIds();
            if ((listRolesIds == null) || (listRolesIds.isEmpty()) ){
                user.setRoles(new HashSet<>());
                userService.save(user);
            }else {
                userService.save(user, listRolesIds);
            }
            UserDetails userDetails = new UserDetails();
            userDetails.setLogin(user.getLogin());
            userDetails.setUsername(user.getUsername());
            response.setUserDetails(userDetails);
            serviceStatus.setStatusCode("SUCCESS");
            serviceStatus.setMessage("Content Added Successfully");
            response.setServiceStatus(serviceStatus);
        }else {
            serviceStatus.setStatusCode("CONFLICT");
            serviceStatus.setMessage("Content Already Available");
            response.setServiceStatus(serviceStatus);
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "updateUserDetailsRequest")
    @ResponsePayload
    public UpdateUserDetailsResponse updateUser(@RequestPayload UpdateUserDetailsRequest request) {
        User user = new User();
        ServiceStatus serviceStatus = new ServiceStatus();
        UserAllDetails userAllDetails = request.getUserAllDetails();
        if(userService.findByLogin(userAllDetails.getLogin()) != null) {
            user.setLogin(userAllDetails.getLogin());
            user.setUsername(userAllDetails.getUsername());
            user.setPassword(userAllDetails.getPassword());
            List<Long> rolesIds = request.getRolesIds();
            if ((rolesIds == null) || (rolesIds.isEmpty())) {
                userService.update(user);
            } else {
                userService.update(user, rolesIds);
            }
            serviceStatus.setStatusCode("SUCCESS");
            serviceStatus.setMessage("Content Updated Successfully");
        }else {
            serviceStatus.setStatusCode("FALSE");
            serviceStatus.setMessage("Content Not Available. Updated Failed");
        }
        UpdateUserDetailsResponse response = new UpdateUserDetailsResponse();
        response.setServiceStatus(serviceStatus);
        return response;
    }
}
