package com.bajaj.IPMS.security;

import com.bajaj.IPMS.model.Agent;
import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.AgentRepository;
import com.bajaj.IPMS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgentSecurity {

    @Autowired
    UserService userService;

    @Autowired
    AgentRepository agentRepository;

    public boolean checkAuth(Long agentId){
        User currUser = userService.getCurrUser();
        Long currUserId = currUser.getId();
        String role = currUser.getRole();

        if("ADMIN".equals(role)){
            return true;
        }

        Agent agent = agentRepository.findByUserId(currUserId);
        if (agent != null){
            return agentId.equals(agent.getId());
        }

        return false;
    }
}
