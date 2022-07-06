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
import java.util.regex.Pattern;

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
        UserDetails userDetails = new UserDetails();
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
        response.setUserDetails(userDetails);
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "deleteUserDetailsRequest")
    @ResponsePayload
    public DeleteUserDetailsResponse deleteUser(@RequestPayload DeleteUserDetailsRequest request) {
        User user = userService.findByLogin(request.getLogin());
        ServiceStatus serviceStatus = new ServiceStatus();
        if (user == null ) {
            serviceStatus.setStatus("FALSE");
            serviceStatus.setMessage("Content Not Available");
        } else {
            userService.delete(user);
            serviceStatus.setStatus("SUCCESS");
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

        UserDetails userDetails = new UserDetails();
        userDetails.setLogin(request.getLogin());
        userDetails.setUsername(request.getName());
        userDetails.setPassword(request.getPassword());

        List<String> stringListException = verifyForm(userDetails);

        if (stringListException.isEmpty()) {
            User userBD = userService.findByLogin(request.getLogin());
            if (userBD == null) {
                User user = new User();
                user.setLogin(request.getLogin());
                user.setUsername(request.getName());
                user.setPassword(request.getPassword());
                List<Long> listRolesIds = request.getRolesIds();
                if ((listRolesIds == null) || (listRolesIds.isEmpty())) {
                    user.setRoles(new HashSet<>());
                    userService.save(user);
                } else {
                    userService.save(user, listRolesIds);
                }
                response.setUserDetails(userDetails);
                serviceStatus.setStatus("SUCCESS");
                serviceStatus.setMessage("Content Added Successfully");
                response.setServiceStatus(serviceStatus);
            } else {
                serviceStatus.setStatus("CONFLICT");
                serviceStatus.setMessage("Content Already Available");
                response.setServiceStatus(serviceStatus);
            }
        }else {
            for (String s :stringListException) {
                ErrorSOAP errorSOAP = new ErrorSOAP();
                errorSOAP.setMessage(s);
                serviceStatus.getError().add(errorSOAP);
            }
            serviceStatus.setStatus("FALSE");
            serviceStatus.setMessage("Received Data Verification Error. Added Failed");
            response.setServiceStatus(serviceStatus);
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "updateUserDetailsRequest")
    @ResponsePayload
    public UpdateUserDetailsResponse updateUser(@RequestPayload UpdateUserDetailsRequest request) {
        User user = new User();
        ServiceStatus serviceStatus = new ServiceStatus();
        UserDetails userAllDetails = request.getUserDetails();
        List<String> stringListException = verifyForm(userAllDetails);
        if (stringListException.isEmpty()){
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
                serviceStatus.setStatus("SUCCESS");
                serviceStatus.setMessage("Content Updated Successfully");
            }else {
                serviceStatus.setStatus("FALSE");
                serviceStatus.setMessage("Content Not Available. Updated Failed");
            }
        }else {
            for (String s :stringListException) {
                ErrorSOAP errorSOAP = new ErrorSOAP();
                errorSOAP.setMessage(s);
                serviceStatus.getError().add(errorSOAP);
            }
            serviceStatus.setStatus("FALSE");
            serviceStatus.setMessage("Received Data Verification Error. Updated Failed");
        }

        UpdateUserDetailsResponse response = new UpdateUserDetailsResponse();
        response.setServiceStatus(serviceStatus);
        return response;
    }

    public List<String> verifyPassword(String password){
        //password содержит букву в заглавном регистре и цифру
        System.out.println(password);
        List<String> stringListException = new ArrayList<>();
        if(!Pattern.matches(".*[0-9].*", password)){
            stringListException.add("Пароль должен содержать минимум 1 цифру");
        }
        if (!Pattern.matches(".*[A-ZА-ЯЁ].*", password)){
            stringListException.add("Пароль должен содержать минимум 1 букву в заглавном регистре");
        }
        return stringListException;
    }

    public List<String> verifyForm(UserDetails userDetails){
        List<String> stringListException = new ArrayList<>();
        if(userDetails.getLogin().isEmpty() || userDetails.getLogin() == null){
            stringListException.add("поле Login обязательно к заполнению");
        }
        if(userDetails.getUsername().isEmpty() || userDetails.getUsername() == null){
            stringListException.add("поле Username обязательно к заполнению");
        }
        if(userDetails.getPassword().isEmpty() || userDetails.getPassword() == null){
            stringListException.add("поле Password обязательно к заполнению");
        }else {
            List<String> passwordException = verifyPassword(userDetails.getPassword());
            if (!passwordException.isEmpty()){
                stringListException.addAll(passwordException);
            }
        }
        return stringListException;
    }
}
