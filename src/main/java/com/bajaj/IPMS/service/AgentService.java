package com.bajaj.IPMS.service;

import com.bajaj.IPMS.model.Agent;
import com.bajaj.IPMS.model.RegisterRequest;
import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.AgentRepository;
import com.bajaj.IPMS.repository.UserRepository;
import com.bajaj.IPMS.security.AgentSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AgentService {

    @Autowired
    AgentRepository agentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthService authService;

    @Autowired
    UserService userService;

    @Autowired
    AgentSecurity agentSecurity;

    public ResponseEntity<?> createAgent(Map<String, String> request){
        Agent agent = new Agent();
        RegisterRequest registerRequest = new RegisterRequest();

        String email = request.get("email");
        String password = request.get("password");
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);

        User user = authService.register(registerRequest);

        agent.setUser(user);

        String prefix = "AGT-";
        String code = String.format("%016d", agentRepository.count()+1);
        String agentCode = prefix + code;
        agent.setAgentCode(agentCode);

        agent.setFullName(request.get("fullName"));
        agent.setLicenseNumber(request.get("licenseNumber"));

        User adminUser = userService.getCurrUser();
        agent.setCreatedBy(adminUser.getId());

        agentRepository.save(agent);

        return ResponseEntity.ok(Map.of(
                "Message", "Agent created Successfully",
                "Agent Code", agentCode
        ));
    }

    public Agent getAgent(Long id){
        if(agentSecurity.checkAuth(id)){
            return agentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agent Not Found"));
        } else {
            throw new IllegalArgumentException("User Not Authorised");
        }
    }

    public ResponseEntity<?> deleteAgent(String agentCode){
        Agent agent = agentRepository.findByAgentCode(agentCode);
        Long id = agent.getId();

        agentRepository.delete(agent);
        userRepository.deleteById(id);

        return ResponseEntity.ok("Agent Deleted " + agentCode);
    }
}
