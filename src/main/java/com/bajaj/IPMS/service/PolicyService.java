package com.bajaj.IPMS.service;

import com.bajaj.IPMS.DTO.Request.CreatePolicyRequest;
import com.bajaj.IPMS.DTO.Response.PolicyAuditLogDTO;
import com.bajaj.IPMS.DTO.Response.PolicyDTO;
import com.bajaj.IPMS.exception.ForbiddenException;
import com.bajaj.IPMS.exception.InvalidRequestException;
import com.bajaj.IPMS.exception.ResourceNotFoundException;
import com.bajaj.IPMS.model.*;
import com.bajaj.IPMS.repository.*;
import com.bajaj.IPMS.security.PolicySecurity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PolicyService {

    @Autowired
    PolicyRepository policyRepository;

    @Autowired
    PolicyAuditLogRepository policyAuditLogRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    AgentRepository agentRepository;

    @Autowired
    UserRepository userRepository;

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

    public ResponseEntity<?> createPolicy(CreatePolicyRequest request){
        Policy policy = new Policy();

        String prefix = "IMPS-";
        String year = String.valueOf(LocalDate.now().getYear());
        String code = String.format("%015d", policyRepository.count()+1);
        String policyNumber = prefix + year + "-" + code;
        policy.setPolicyNumber(policyNumber);

        String policyType = request.getPolicyType();
        policy.setPolicyType(policyType);
        policy.setStatus("DRAFT");

        Customer customer = customerRepository.findByCustomerCode(request.getCustomerCode());
        if (customer == null){
            throw new ResourceNotFoundException("Customer Not Found");
        }
        policy.setCustomer(customer);

        Agent agent = agentRepository.findByAgentCode(request.getAgentCode());
        if (agent == null){
            throw new ResourceNotFoundException("Agent Not Found");
        }
        policy.setAgent(agent);

        BigDecimal sumInsured = new BigDecimal(request.getSumInsured());
        policy.setSumInsured(sumInsured);

        String startDateString = request.getStartDate();
        LocalDate startDate = LocalDate.parse(startDateString);
        policy.setStartDate(startDate);

        String endDateString = request.getEndDate();
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
        policy.setDescription(request.getDescription());

        User user = userService.getCurrUser();
        policy.setCreatedBy(user.getId());

        PolicyAuditLog policyAuditLog = new PolicyAuditLog();
        policyAuditLog.setPolicy(policy);
        policyAuditLog.setCreatedBy(user.getId());
        policyAuditLog.setChangedBy(null);
        policyAuditLog.setRemarks("Policy Created");
        policyAuditLog.setNewStatus("DRAFT");
        policyAuditLog.setPreviousStatus("NULL");

        Notification notification1 = new Notification();
        notification1.setCreatedAt(OffsetDateTime.now());
        notification1.setMessage("Policy in DRAFT state");
        notification1.setUser(user);
        notification1.setTitle("Policy State Change");
        notification1.setCreatedBy(user.getId());

        User custUser = userRepository.findById(customer.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer User Not Found"));
        Notification notification2 = new Notification();
        notification2.setCreatedAt(OffsetDateTime.now());
        notification2.setMessage("Policy in DRAFT state");
        notification2.setUser(custUser);
        notification2.setTitle("Policy State Change");
        notification2.setCreatedBy(user.getId());

        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
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
            throw new ForbiddenException("Not Authorised");
        }
    }

    public ResponseEntity<?> updatePolicy(String policyNumber, Map<String, String> request) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber);
        if (policy == null){
            throw new ResourceNotFoundException("Policy Not Found");
        }
        if(!policy.getStatus().equals("DRAFT")){
            throw new InvalidRequestException("Policy can only be updated when in DRAFT status");
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
                if (customer == null){
                    throw new ResourceNotFoundException("Customer Not Found");
                }
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

        User user = userService.getCurrUser();

        PolicyAuditLog policyAuditLog = new PolicyAuditLog();
        policyAuditLog.setPolicy(policy);
        policyAuditLog.setCreatedBy(null);
        policyAuditLog.setChangedBy(user.getId());
        policyAuditLog.setRemarks("Policy Updated");
        policyAuditLog.setNewStatus("DRAFT");
        policyAuditLog.setPreviousStatus("NULL");
        policyAuditLog.setChangedAt(OffsetDateTime.now());

        Notification notification1 = new Notification();
        notification1.setCreatedAt(OffsetDateTime.now());
        notification1.setMessage("Policy Updated Successfully");
        notification1.setUser(user);
        notification1.setTitle("Policy Updated");
        notification1.setCreatedBy(user.getId());

        Customer customer = customerRepository.findById(policy.getCustomer().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer User Not Found"));
        User custUser = userRepository.findById(customer.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer User Not Found"));
        Notification notification2 = new Notification();
        notification2.setCreatedAt(OffsetDateTime.now());
        notification2.setMessage("Policy Updated Successfully");
        notification2.setUser(custUser);
        notification2.setTitle("Policy Updated");
        notification2.setCreatedBy(user.getId());

        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
        policyRepository.save(policy);
        policyAuditLogRepository.save(policyAuditLog);
        PolicyDTO policyDTO = new PolicyDTO(policy);

        return ResponseEntity.ok(policyDTO);
    }

    public ResponseEntity<?> activatePolicy(String policyNumber) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber);
        if (policy == null){
            log.warn("Policy activation failed. Policy not found: {}", policyNumber);
            throw new ResourceNotFoundException("Policy Not Found");
        }

        if (policy.getStatus().equals("DRAFT")) {
            policy.setStatus("ACTIVE");
            log.info(
                    "Policy status changed from DRAFT to ACTIVE. Policy Number: {}",
                    policyNumber
            );
        } else {
            log.warn(
                    "Policy activation rejected. Policy {} is in {} status",
                    policyNumber,
                    policy.getStatus()
            );
            throw new  InvalidRequestException("Policy can be activated only from DRAFT status");
        }

        User user = userService.getCurrUser();

        PolicyAuditLog policyAuditLog = new PolicyAuditLog();
        policyAuditLog.setPolicy(policy);
        policyAuditLog.setCreatedBy(null);
        policyAuditLog.setChangedBy(user.getId());
        policyAuditLog.setRemarks("Policy Activated");
        policyAuditLog.setNewStatus("ACTIVE");
        policyAuditLog.setPreviousStatus("DRAFT");
        policyAuditLog.setChangedAt(OffsetDateTime.now());

        Notification notification1 = new Notification();
        notification1.setCreatedAt(OffsetDateTime.now());
        notification1.setMessage("Policy Activated");
        notification1.setUser(user);
        notification1.setTitle("Policy State Change");
        notification1.setCreatedBy(user.getId());

        Customer customer = customerRepository.findById(policy.getCustomer().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer User Not Found"));
        User custUser = userRepository.findById(customer.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer User Not Found"));
        Notification notification2 = new Notification();
        notification2.setCreatedAt(OffsetDateTime.now());
        notification2.setMessage("Policy Activated");
        notification2.setUser(custUser);
        notification2.setTitle("Policy State Change");
        notification2.setCreatedBy(user.getId());

        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
        policyRepository.save(policy);
        policyAuditLogRepository.save(policyAuditLog);

        return ResponseEntity.ok(Map.of(
                "message", "Policy Activated"
        ));
    }

    public ResponseEntity<?> renewPolicy(String policyNumber) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber);
        if (policy == null){
            log.warn("Policy renewal failed. Policy not found: {}", policyNumber);
            throw new ResourceNotFoundException("Policy Not Found");
        }
        if (policy.getStatus().equals("ACTIVE")){
            policy.setStatus("RENEWED");
            log.info(
                    "Policy state transition: ACTIVE -> RENEWED. Policy Number: {}",
                    policyNumber
            );
        } else {
            log.warn(
                    "Policy renewal rejected. Policy {} is in {} status",
                    policyNumber,
                    policy.getStatus()
            );
            throw new InvalidRequestException("Policy can only be renewed from ACTIVE status");
        }

        User user = userService.getCurrUser();

        PolicyAuditLog policyAuditLog = new PolicyAuditLog();
        policyAuditLog.setPolicy(policy);
        policyAuditLog.setCreatedBy(null);
        policyAuditLog.setChangedBy(user.getId());
        policyAuditLog.setRemarks("Policy Renewed");
        policyAuditLog.setNewStatus("RENEWED");
        policyAuditLog.setPreviousStatus("ACTIVE");
        policyAuditLog.setChangedAt(OffsetDateTime.now());

        Notification notification1 = new Notification();
        notification1.setCreatedAt(OffsetDateTime.now());
        notification1.setMessage("Policy Renewed");
        notification1.setUser(user);
        notification1.setTitle("Policy State Change");
        notification1.setCreatedBy(user.getId());

        Customer customer = customerRepository.findById(policy.getCustomer().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer User Not Found"));
        User custUser = userRepository.findById(customer.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer User Not Found"));
        Notification notification2 = new Notification();
        notification2.setCreatedAt(OffsetDateTime.now());
        notification2.setMessage("Policy Renewed");
        notification2.setUser(custUser);
        notification2.setTitle("Policy State Change");
        notification2.setCreatedBy(user.getId());

        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
        policyRepository.save(policy);
        policyAuditLogRepository.save(policyAuditLog);

        return ResponseEntity.ok(Map.of(
                "message", "Policy Renewed"
        ));
    }

    public ResponseEntity<?> suspendPolicy(String policyNumber) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber);
        if (policy == null){
            log.warn("Policy suspension failed. Policy not found: {}", policyNumber);
            throw new ResourceNotFoundException("Policy Not Found");
        }

        if (policy.getStatus().equals("ACTIVE")){
            policy.setStatus("SUSPENDED");
            log.info(
                    "Policy state transition: ACTIVE -> SUSPENDED. Policy Number: {}",
                    policyNumber
            );
        } else {
            throw new InvalidRequestException("Policy can only be SUSPENDED from ACTIVE status");
        }

        User user = userService.getCurrUser();

        PolicyAuditLog policyAuditLog = new PolicyAuditLog();
        policyAuditLog.setPolicy(policy);
        policyAuditLog.setCreatedBy(null);
        policyAuditLog.setChangedBy(user.getId());
        policyAuditLog.setRemarks("Policy Suspended");
        policyAuditLog.setNewStatus("SUSPENDED");
        policyAuditLog.setPreviousStatus("ACTIVE");
        policyAuditLog.setChangedAt(OffsetDateTime.now());

        Notification notification1 = new Notification();
        notification1.setCreatedAt(OffsetDateTime.now());
        notification1.setMessage("Policy Suspended");
        notification1.setUser(user);
        notification1.setTitle("Policy State Change");
        notification1.setCreatedBy(user.getId());

        Customer customer = customerRepository.findById(policy.getCustomer().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer User Not Found"));
        User custUser = userRepository.findById(customer.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer User Not Found"));
        Notification notification2 = new Notification();
        notification2.setCreatedAt(OffsetDateTime.now());
        notification2.setMessage("Policy Suspended");
        notification2.setUser(custUser);
        notification2.setTitle("Policy State Change");
        notification2.setCreatedBy(user.getId());

        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
        policyRepository.save(policy);
        policyAuditLogRepository.save(policyAuditLog);

        return ResponseEntity.ok(Map.of(
                "message", "Policy Suspended"
        ));
    }

    public ResponseEntity<?> cancelPolicy(String policyNumber) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber);
        if (policy == null){
            log.warn("Policy cancellation failed. Policy not found: {}", policyNumber);
            throw new ResourceNotFoundException("Policy Not Found");
        }

        String previousStatus = policy.getStatus();

        PolicyAuditLog policyAuditLog = new PolicyAuditLog();
        policyAuditLog.setPreviousStatus(previousStatus);

        policy.setStatus("CANCELLED");
        log.info(
                "Policy state transition: {} -> CANCELLED. Policy Number: {}",
                previousStatus,
                policyNumber
        );

        User user = userService.getCurrUser();

        policyAuditLog.setPolicy(policy);
        policyAuditLog.setCreatedBy(null);
        policyAuditLog.setChangedBy(user.getId());
        policyAuditLog.setRemarks("Policy Cancelled");
        policyAuditLog.setNewStatus("CANCELLED");
        policyAuditLog.setChangedAt(OffsetDateTime.now());

        Notification notification1 = new Notification();
        notification1.setCreatedAt(OffsetDateTime.now());
        notification1.setMessage("Policy Cancelled");
        notification1.setUser(user);
        notification1.setTitle("Policy State Change");
        notification1.setCreatedBy(user.getId());

        Customer customer = customerRepository.findById(policy.getCustomer().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer User Not Found"));
        User custUser = userRepository.findById(customer.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer User Not Found"));
        Notification notification2 = new Notification();
        notification2.setCreatedAt(OffsetDateTime.now());
        notification2.setMessage("Policy Cancelled");
        notification2.setUser(custUser);
        notification2.setTitle("Policy State Change");
        notification2.setCreatedBy(user.getId());

        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
        policyRepository.save(policy);
        policyAuditLogRepository.save(policyAuditLog);

        return ResponseEntity.ok(Map.of(
                "message", "Policy Cancelled"
        ));
    }

    public ResponseEntity<?> getAudit(String policyNumber) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber);
        if (policy == null){
            throw new ResourceNotFoundException("Policy Not Found");
        }

        List<PolicyAuditLog> policyAuditLogs = policyAuditLogRepository.findAllByPolicyId(policy.getId());
        List<PolicyAuditLogDTO> policyAuditLogDTOs = new ArrayList<>();

        for(PolicyAuditLog policyAuditLog: policyAuditLogs) {
            policyAuditLogDTOs.add(new PolicyAuditLogDTO(policyAuditLog));
        }

        return ResponseEntity.ok(policyAuditLogDTOs);
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
                orElseThrow(() -> new ResourceNotFoundException("Policy Not Found"));
        String policyNumber = policy.getPolicyNumber();
        if(policySecurity.checkAuth(policyNumber)){
            return documentService.listDocs(policyId);
        } else {
            throw new ForbiddenException("Not Authorised");
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
