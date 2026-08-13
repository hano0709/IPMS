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
import java.time.Period;
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

        String policyType = request.get("policyType");
        policy.setPolicyType(policyType);
        policy.setStatus("DRAFT");

        Customer customer = customerRepository.findByCustomerCode(request.get("customerCode"));
        policy.setCustomer(customer);

        Agent agent = agentRepository.findByAgentCode(request.get("agentCode"));
        policy.setAgent(agent);

        BigDecimal sumInsured = new BigDecimal(request.get("sumInsured"));
        policy.setSumInsured(sumInsured);

        String startDateString = request.get("startDate");
        LocalDate startDate = LocalDate.parse(startDateString);
        policy.setStartDate(startDate);

        String endDateString = request.get("endDate");
        LocalDate endDate = LocalDate.parse(endDateString);
        policy.setEndDate(endDate);

        BigDecimal premiumAmount;
        if(policyType.equals("LIFE")){
            double baseRate = 0.5/100;
            LocalDate dob = customer.getDateOfBirth();
            int age = Period.between(dob, LocalDate.now()).getYears();
            double ageFactor;

            if(age < 30) ageFactor = 1.0;
            else if(age <= 50) ageFactor = 1.2;
            else ageFactor = 1.5;

            premiumAmount = (sumInsured.multiply(BigDecimal.valueOf(baseRate))).multiply(BigDecimal.valueOf(ageFactor));
        } else if(policyType.equals("HEALTH")){
            double baseRate = 1.2/100;
            int duration = Period.between(startDate, endDate).getYears();
            double durationFactor;

            if(duration == 1) durationFactor = 1.0;
            else if (duration == 2) durationFactor = 0.95;
            else durationFactor = 0.90;

            premiumAmount = sumInsured.multiply(BigDecimal.valueOf(baseRate)).multiply(BigDecimal.valueOf(durationFactor));
        } else if(policyType.equals("MOTOR")){
            double baseRate = 2.0/100;
            double ageFactor = 1.0;

            premiumAmount = (sumInsured.multiply(BigDecimal.valueOf(baseRate))).multiply(BigDecimal.valueOf(ageFactor));
        } else {
            double baseRate = 0.8/100;
            double ageFactor = 1.0;

            premiumAmount = (sumInsured.multiply(BigDecimal.valueOf(baseRate))).multiply(BigDecimal.valueOf(ageFactor));
        }

        policy.setPremiumAmount(premiumAmount);

        policy.setDescription(request.get("description"));

        User user = userService.getCurrUser();
        policy.setCreatedBy(user.getId());

        policyRepository.save(policy);

        return ResponseEntity.ok(Map.of("Policy created Successfully", policy.getPolicyNumber()));
    }
}
