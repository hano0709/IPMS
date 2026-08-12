package com.bajaj.IPMS.security;

import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgentSecurity {

    @Autowired
    UserService userService;

    public boolean checkAuth(Long agentId){
        User currUser = userService.getCurrUser();
        Long currUserId = currUser.getId();
        String role = currUser.getRole();

        if("ADMIN".equals(role) || agentId.equals(currUserId)){
            return true;
        }

        return false;
    }
}
