package com.bajaj.IPMS.service;

import com.bajaj.IPMS.model.Agent;
import com.bajaj.IPMS.model.Customer;
import com.bajaj.IPMS.model.Policy;
import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.AgentRepository;
import com.bajaj.IPMS.repository.CustomerRepository;
import com.bajaj.IPMS.repository.PolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class PolicyService {

    @Autowired
    PolicyRepository policyRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    AgentRepository agentRepository;

    @Autowired
    UserService userService;

    public ResponseEntity<?> getAllPolicies(){
        List<Policy> policies = policyRepository.findAll();

        return ResponseEntity.ok(policies);
    }

    public ResponseEntity<?> createPolicy(Map<String, String> request){
        Policy policy = new Policy();

        String prefix = "IMPS-";
        String year = String.valueOf(LocalDate.now().getYear());
        String code = String.format("%015d", policyRepository.count()+1);
        String policyNumber = prefix + year + "-" + code;
        policy.setPolicyNumber(policyNumber);

        policy.setPolicyType(request.get("policyType"));
        policy.setStatus(request.get("status"));

        Customer customer = customerRepository.findByCustomerCode(request.get("customerCode"));
        policy.setCustomer(customer);

        Agent agent = agentRepository.findByAgentCode(request.get("agentCode"));
        policy.setAgent(agent);

        policy.setSumInsured(new BigDecimal(request.get("sumInsured")));
        policy.setPremiumAmount(new BigDecimal(request.get("premiumAmount")));

        String startDateString = request.get("startDate");
        LocalDate startDate = LocalDate.parse(startDateString);
        policy.setStartDate(startDate);

        String endDateString = request.get("endDate");
        LocalDate endDate = LocalDate.parse(endDateString);
        policy.setEndDate(endDate);

        policy.setDescription(request.get("description"));

        User user = userService.getCurrUser();
        policy.setCreatedBy(user.getId());

        policyRepository.save(policy);

        return ResponseEntity.ok(Map.of("Policy created Successfully", policy.getPolicyNumber()));
    }
}
