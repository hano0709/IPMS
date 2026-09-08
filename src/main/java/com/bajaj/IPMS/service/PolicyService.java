package com.bajaj.IPMS.service;

import com.bajaj.IPMS.DTO.PolicyAuditLogDTO;
import com.bajaj.IPMS.DTO.PolicyDTO;
import com.bajaj.IPMS.DTO.PolicyDocumentsDTO;
import com.bajaj.IPMS.model.*;
import com.bajaj.IPMS.repository.*;
import com.bajaj.IPMS.security.PolicySecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PolicyService {

    @Autowired
    PolicyRepository policyRepository;

    @Autowired
    PolicyAuditLogRepository policyAuditLogRepository;

    @Autowired
    PolicyDocumentsRepository policyDocumentsRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    AgentRepository agentRepository;

    @Autowired
    UserService userService;

    @Autowired
    DocumentService documentService;

    @Autowired
    PolicySecurity policySecurity;

    public ResponseEntity<?> getAllPolicies(){
        List<Policy> policies = policyRepository.findAll();
        List<PolicyDTO> policyDTOs = new ArrayList<>();
        for (Policy policy: policies){
            policyDTOs.add(new PolicyDTO(policy));
        }
        return ResponseEntity.ok(policyDTOs);
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

        PolicyAuditLog policyAuditLog = new PolicyAuditLog();
        policyAuditLog.setPolicy(policy);
        policyAuditLog.setCreatedBy(user.getId());
        policyAuditLog.setChangedBy(null);
        policyAuditLog.setRemarks("Policy Created");
        policyAuditLog.setNewStatus("DRAFT");
        policyAuditLog.setPreviousStatus("NULL");

        policyRepository.save(policy);
        policyAuditLogRepository.save(policyAuditLog);

        return ResponseEntity.ok(Map.of("Policy created Successfully", policy.getPolicyNumber()));
    }

    public ResponseEntity<?> getPolicy(String policyNumber){
        if(policySecurity.checkAuth(policyNumber)){
            Policy policy = policyRepository.findByPolicyNumber(policyNumber);
            if (policy == null) return ResponseEntity.badRequest().body(Map.of("Error", "PolicyNot Found"));
            PolicyDTO policyDTO = new PolicyDTO(policy);
            return ResponseEntity.ok(policyDTO);
        } else {
            return ResponseEntity.badRequest().body(Map.of("Error", "Not Authorised"));
        }
    }

    public ResponseEntity<?> updatePolicy(String policyNumber, Map<String, String> request) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber);
        if(!policy.getStatus().equals("DRAFT")){
            return ResponseEntity.badRequest().body("Policy can only be updated when in DRAFT status");
        }

        for (Map.Entry<String, String> entry: request.entrySet()){
            String key = entry.getKey();;
            String value = entry.getValue();
            boolean calculatePremium = false;
            switch (key){
                case "policyType":
                    policy.setPolicyType(value);
                    calculatePremium = true;
                    break;
                case "sumInsured":
                    policy.setSumInsured(new BigDecimal(value));
                    calculatePremium = true;
                    break;
                case "startDate":
                    LocalDate startDate = LocalDate.parse(value);
                    policy.setStartDate(startDate);
                    calculatePremium = true;
                    break;
                case "endDate":
                    LocalDate endDate = LocalDate.parse(value);
                    policy.setEndDate(endDate);
                    calculatePremium = true;
                    break;
                case "description":
                    policy.setDescription(value);
                    break;
            }

            if (calculatePremium) {
                BigDecimal premiumAmount;
                String policyType = request.get("policyType");
                Customer customer = customerRepository.findByCustomerCode(request.get("customerCode"));
                BigDecimal sumInsured = policy.getSumInsured();
                LocalDate startDate = policy.getStartDate();
                LocalDate endDate = policy.getEndDate();
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
            }
        }

        PolicyAuditLog policyAuditLog = policyAuditLogRepository.findByPolicyId(policy.getId());
        User user = userService.getCurrUser();
        policyAuditLog.setChangedBy(user.getId());
        policyAuditLog.setRemarks("Policy Updated");

        policyRepository.save(policy);
        policyAuditLogRepository.save(policyAuditLog);
        PolicyDTO policyDTO = new PolicyDTO(policy);

        return ResponseEntity.ok(policyDTO);
    }

    public ResponseEntity<?> activatePolicy(String policyNumber) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber);
        PolicyAuditLog policyAuditLog = policyAuditLogRepository.findByPolicyId(policy.getId());

        if (policy.getStatus().equals("DRAFT")) {
            policy.setStatus("ACTIVE");
        } else {
            return ResponseEntity.badRequest().body(Map.of("Error", "Policy can be activated only from DRAFT status"));
        }

        User user = userService.getCurrUser();

        policyAuditLog.setChangedBy(user.getId());
        policyAuditLog.setPreviousStatus("DRAFT");
        policyAuditLog.setNewStatus("ACTIVE");

        policyRepository.save(policy);
        policyAuditLogRepository.save(policyAuditLog);

        return ResponseEntity.ok("Policy Activated");
    }

    public ResponseEntity<?> renewPolicy(String policyNumber) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber);
        PolicyAuditLog policyAuditLog = policyAuditLogRepository.findByPolicyId(policy.getId());

        if (policy.getStatus().equals("ACTIVE")){
            policy.setStatus("RENEWED");
            policyAuditLog.setPreviousStatus("ACTIVE");
            policyAuditLog.setNewStatus("RENEWED");
        } else {
            return ResponseEntity.badRequest().body(Map.of("Error", "Policy can only be renewd from ACTIVE status"));
        }

        User user = userService.getCurrUser();
        policyAuditLog.setChangedBy(user.getId());

        policyRepository.save(policy);
        policyAuditLogRepository.save(policyAuditLog);

        return ResponseEntity.ok("Policy Renewed");
    }

    public ResponseEntity<?> suspendPolicy(String policyNumber) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber);
        PolicyAuditLog policyAuditLog = policyAuditLogRepository.findByPolicyId(policy.getId());

        if (policy.getStatus().equals("ACTIVE")){
            policy.setStatus("SUSPENDED");
            policyAuditLog.setPreviousStatus("ACTIVE");
            policyAuditLog.setNewStatus("SUSPENDED");
        } else {
            return ResponseEntity.badRequest().body(Map.of("Error", "Policy can only be SUSPENDED from ACTIVE status"));
        }

        User user = userService.getCurrUser();
        policyAuditLog.setChangedBy(user.getId());

        policyRepository.save(policy);
        policyAuditLogRepository.save(policyAuditLog);

        return ResponseEntity.ok("Policy Suspended");
    }

    public ResponseEntity<?> cancelPolicy(String policyNumber) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber);
        PolicyAuditLog policyAuditLog = policyAuditLogRepository.findByPolicyId(policy.getId());

        policyAuditLog.setPreviousStatus(policy.getStatus());
        policy.setStatus("CANCELLED");
        policyAuditLog.setNewStatus(policy.getStatus());

        User user = userService.getCurrUser();
        policyAuditLog.setChangedBy(user.getId());

        policyRepository.save(policy);
        policyAuditLogRepository.save(policyAuditLog);

        return ResponseEntity.ok("Policy Cancelled");
    }

    public ResponseEntity<?> getAudit(String policyNumber) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber);
        PolicyAuditLog policyAuditLog = policyAuditLogRepository.findByPolicyId(policy.getId());

        PolicyAuditLogDTO policyAuditLogDTO = new PolicyAuditLogDTO(policyAuditLog);

        return ResponseEntity.ok(policyAuditLogDTO);
    }

    public ResponseEntity<?> getExpiringSoon() {
        LocalDate today = LocalDate.now();
        LocalDate expiry = LocalDate.now().plusDays(30);

        List<Policy> policies = policyRepository.findAllByExpiringSoon(today, expiry);

        List<PolicyDTO> policyDTOs = new ArrayList<>();
        for (Policy policy:policies){
            policyDTOs.add(new PolicyDTO(policy));
        }

        return ResponseEntity.ok(policyDTOs);
    }

    public ResponseEntity<?> uploadDocs(Long policyId, MultipartFile file) {
        return documentService.uploadDocs(policyId, file);
    }

    public ResponseEntity<?> listDocs(Long policyId) {
        Policy policy = policyRepository.findById(policyId).
                orElseThrow();
        String policyNumber = policy.getPolicyNumber();
        if(policySecurity.checkAuth(policyNumber)){
            return documentService.listDocs(policyId);
        } else {
            return ResponseEntity.badRequest().body(Map.of("Error", "Not Authorised"));
        }
    }

    public ResponseEntity<?> getStateChanges() {
        List<PolicyAuditLog> last10 = policyAuditLogRepository.findLast10Changes(PageRequest.of(0, 10));
        List<PolicyAuditLogDTO> last10DTO = new ArrayList<>();

        for(PolicyAuditLog policyAuditLog: last10){
            last10DTO.add(new PolicyAuditLogDTO(policyAuditLog));
        }

        return ResponseEntity.ok(last10DTO);
    }
}
