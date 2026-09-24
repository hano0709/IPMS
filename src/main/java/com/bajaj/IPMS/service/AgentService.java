package com.bajaj.IPMS.service;

import com.bajaj.IPMS.DTO.Request.CreateAgentRequest;
import com.bajaj.IPMS.DTO.Response.AgentDTO;
import com.bajaj.IPMS.DTO.Response.PolicyDTO;
import com.bajaj.IPMS.exception.ForbiddenException;
import com.bajaj.IPMS.exception.ResourceNotFoundException;
import com.bajaj.IPMS.model.Agent;
import com.bajaj.IPMS.model.Policy;
import com.bajaj.IPMS.model.RegisterRequest;
import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.AgentRepository;
import com.bajaj.IPMS.repository.PolicyRepository;
import com.bajaj.IPMS.repository.UserRepository;
import com.bajaj.IPMS.security.AgentSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AgentService {

    @Autowired
    AgentRepository agentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PolicyRepository policyRepository;

    @Autowired
    AuthService authService;

    @Autowired
    UserService userService;

    @Autowired
    AgentSecurity agentSecurity;

    public ResponseEntity<?> createAgent(CreateAgentRequest request){
        Agent agent = new Agent();
        RegisterRequest registerRequest = new RegisterRequest();

        String email = request.getEmail();
        String password = request.getPassword();
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        registerRequest.setRole("AGENT");

        User user = authService.register(registerRequest);

        agent.setUser(user);

        String prefix = "AGT-";
        String code = String.format("%016d", agentRepository.count()+1);
        String agentCode = prefix + code;
        agent.setAgentCode(agentCode);

        agent.setFullName(request.getFullName());
        agent.setLicenseNumber(request.getLicenseNumber());

        User adminUser = userService.getCurrUser();
        agent.setCreatedBy(adminUser.getId());

        agent.setActive(true);

        agentRepository.save(agent);

        return ResponseEntity.ok(Map.of(
                "Message", "Agent created Successfully",
                "Agent Code", agentCode
        ));
    }

    public Agent getAgent(Long id){
        if(agentSecurity.checkAuth(id)){
            return agentRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Agent Not Found"));
        } else {
            throw new ForbiddenException("User Not Authorised");
        }
    }

    public ResponseEntity<?> deleteAgent(String agentCode){
        Agent agent = agentRepository.findByAgentCode(agentCode);
        if (agent == null){
            throw new ResourceNotFoundException("Agent Not Found");
        }
        Long id = agent.getId();

        agentRepository.delete(agent);
        userRepository.deleteById(id);

        return ResponseEntity.ok("Agent Deleted " + agentCode);
    }

    public List<AgentDTO> searchByCode(String agentCode) {
        return agentRepository.findByAgentCodeContainingIgnoreCase(agentCode)
                .stream()
                .map(AgentDTO::new)
                .limit(10)
                .toList();
    }

    public ResponseEntity<?> getPolicies() {
        Long userId = userService.getCurrUser().getId();
        Long agentId = agentRepository.findByUserId(userId).getId();

        List<Policy> policyList =  policyRepository.findAllByAgentId(agentId);
        List<PolicyDTO> policyDTOList = new ArrayList<>();

        for (Policy policy: policyList){
            policyDTOList.add(new PolicyDTO(policy));
        }

        return ResponseEntity.ok(policyDTOList);
    }

    public ResponseEntity<?> getCurrAgent() {
        User user = userService.getCurrUser();
        Agent agent = agentRepository.findByUserId(user.getId());
        AgentDTO agentDTO = new AgentDTO(agent);

        return ResponseEntity.ok(agentDTO);
    }
}
