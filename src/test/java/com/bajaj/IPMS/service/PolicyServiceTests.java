package com.bajaj.IPMS.service;

import com.bajaj.IPMS.DTO.Request.CreatePolicyRequest;
import com.bajaj.IPMS.exception.ForbiddenException;
import com.bajaj.IPMS.exception.InvalidRequestException;
import com.bajaj.IPMS.model.*;
import com.bajaj.IPMS.repository.*;
import com.bajaj.IPMS.security.PolicySecurity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PolicyServiceTests {

    @InjectMocks
    PolicyService policyService;

    @Mock
    UserService userService;

    @Mock
    DocumentService documentService;

    @Mock
    PolicySecurity policySecurity;

    @Mock
    PolicyRepository policyRepository;

    @Mock
    CustomerRepository customerRepository;

    @Mock
    PolicyAuditLogRepository policyAuditLogRepository;

    @Mock
    AgentRepository agentRepository;

    @Mock
    NotificationRepository notificationRepository;

    @Test
    public void testGetAllPolicies(){
        List<Policy> policies = new ArrayList<>();
        Policy policy = new Policy();
        Customer customer = new Customer();
        customer.setId(1L);
        Agent agent = new Agent();
        agent.setId(1L);
        policy.setCustomer(customer);
        policy.setAgent(agent);
        policies.add(policy);
        when(policyRepository.findAll()).thenReturn(policies);

        assertNotNull(policyService.getAllPolicies());
    }

    @Test
    public void testCreatePolicy(){
        CreatePolicyRequest request = new CreatePolicyRequest();
        request.setPolicyType("LIFE");
        request.setCustomerCode("CUST-12323123");
        request.setAgentCode("AGT-1232331");
        request.setSumInsured("5000000");
        request.setStartDate("2026-08-14");
        request.setEndDate("2027-08-13");

        when(policyRepository.count()).thenReturn(0L);

        Customer customer = new Customer();
        customer.setDateOfBirth(LocalDate.now());
        when(customerRepository.findByCustomerCode(request.getCustomerCode())).thenReturn(customer);

        Agent agent = new Agent();
        when(agentRepository.findByAgentCode(request.getAgentCode())).thenReturn(agent);

        User user = new User();
        when(userService.getCurrUser()).thenReturn(user);

        Policy policy = new Policy();
        policy.setStartDate(LocalDate.parse(request.getStartDate()));
        policy.setEndDate(LocalDate.parse(request.getEndDate()));
        when(policyRepository.save(any())).thenReturn(policy);

        PolicyAuditLog policyAuditLog = new PolicyAuditLog();
        when(policyAuditLogRepository.save(any())).thenReturn(policyAuditLog);

        assertNotNull(policyService.createPolicy(request));

        request.setPolicyType("HEALTH");
        assertNotNull(policyService.createPolicy(request));

        request.setPolicyType("MOTOR");
        assertNotNull(policyService.createPolicy(request));

        request.setPolicyType("PROPERTY");
        assertNotNull(policyService.createPolicy(request));
    }

    @Test
    public void testGetPolicy(){
        when(policySecurity.checkAuth(any()))
                .thenReturn(true)
                .thenReturn(false);

        Policy policy = new Policy();
        Customer customer = new Customer();
        customer.setId(1L);
        Agent agent = new Agent();
        agent.setId(1L);
        policy.setCustomer(customer);
        policy.setAgent(agent);

        when(policyRepository.findByPolicyNumber(any())).thenReturn(policy);

        assertNotNull(policyService.getPolicy(any()));
        assertThrows(ForbiddenException.class, () -> policyService.getPolicy(any()));
    }

    @Test
    public void testUpdatePolicy(){
        Policy policy = new Policy();
        policy.setStatus("DRAFT");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setDateOfBirth(LocalDate.now());
        Agent agent = new Agent();
        agent.setId(1L);
        policy.setCustomer(customer);
        policy.setAgent(agent);

        Map<String, String> request = new HashMap<>();
        request.put("policyType", "LIFE");
        request.put("sumInsured", "5000000");
        request.put("startDate", "2026-08-14");
        request.put("endDate", "2027-08-13");
        request.put("description", "hello");

        PolicyAuditLog policyAuditLog = new PolicyAuditLog();

        User user = new User();
        user.setId(0L);

        when(policyRepository.findByPolicyNumber(any())).thenReturn(policy);
        when(customerRepository.findByCustomerCode(any())).thenReturn(customer);
        when(userService.getCurrUser()).thenReturn(user);
        when(policyRepository.save(any())).thenReturn(policy);
        when(policyAuditLogRepository.save(any())).thenReturn(policyAuditLog);

        assertNotNull(policyService.updatePolicy(any(), request));

        request.put("policyType", "HEALTH");
        assertNotNull(policyService.updatePolicy(any(), request));

        request.put("policyType", "MOTOR");
        assertNotNull(policyService.updatePolicy(any(), request));

        request.put("policyType", "PROPERTY");
        assertNotNull(policyService.updatePolicy(any(), request));
    }

    @Test
    public void testActivatePolicy(){
        Policy policy = new Policy();
        policy.setStatus("DRAFT");
        User user = new User();
        user.setId(1L);

        PolicyAuditLog policyAuditLog = new PolicyAuditLog();

        when(policyRepository.findByPolicyNumber(any())).thenReturn(policy);
        when(policyRepository.save(any())).thenReturn(policy);
        when(policyAuditLogRepository.save(any())).thenReturn(policyAuditLog);
        when(userService.getCurrUser()).thenReturn(user);

        assertNotNull(policyService.activatePolicy(any()));

        policy.setStatus("ACTIVE");
        assertThrows(InvalidRequestException.class, () -> policyService.activatePolicy(any()));
    }

    @Test
    public void testRenewPolicy(){
        Policy policy = new Policy();
        policy.setStatus("ACTIVE");
        User user = new User();
        user.setId(1L);

        PolicyAuditLog policyAuditLog = new PolicyAuditLog();

        when(policyRepository.findByPolicyNumber(any())).thenReturn(policy);
        when(policyRepository.save(any())).thenReturn(policy);
        when(policyAuditLogRepository.save(any())).thenReturn(policyAuditLog);
        when(userService.getCurrUser()).thenReturn(user);

        assertNotNull(policyService.renewPolicy(any()));

        policy.setStatus("DRAFT");
        assertThrows(InvalidRequestException.class, () -> policyService.renewPolicy(any()));
    }

    @Test
    public void testSuspendPolicy(){
        Policy policy = new Policy();
        policy.setStatus("ACTIVE");
        User user = new User();
        user.setId(1L);

        PolicyAuditLog policyAuditLog = new PolicyAuditLog();

        when(policyRepository.findByPolicyNumber(any())).thenReturn(policy);
        when(policyRepository.save(any())).thenReturn(policy);
        when(policyAuditLogRepository.save(any())).thenReturn(policyAuditLog);
        when(userService.getCurrUser()).thenReturn(user);

        assertNotNull(policyService.suspendPolicy(any()));

        policy.setStatus("DRAFT");
        assertThrows(InvalidRequestException.class, () -> policyService.suspendPolicy(any()));
    }

    @Test
    public void testCancelPolicy(){
        Policy policy = new Policy();
        User user = new User();
        user.setId(1L);

        PolicyAuditLog policyAuditLog = new PolicyAuditLog();

        when(policyRepository.findByPolicyNumber(any())).thenReturn(policy);
        when(policyRepository.save(any())).thenReturn(policy);
        when(policyAuditLogRepository.save(any())).thenReturn(policyAuditLog);
        when(userService.getCurrUser()).thenReturn(user);

        assertNotNull(policyService.cancelPolicy(any()));
    }

    @Test
    public void testGetAudit(){
        Policy policy = new Policy();

        PolicyAuditLog policyAuditLog = new PolicyAuditLog();

        when(policyRepository.findByPolicyNumber(any())).thenReturn(policy);

        assertNotNull(policyService.getAudit(any()));
    }

    @Test
    public void testGetExpiringSoon(){
        Policy policy = new Policy();

        List<Policy> policies = new ArrayList<>();
        Customer customer = new Customer();
        customer.setId(1L);
        Agent agent = new Agent();
        agent.setId(1L);
        policy.setCustomer(customer);
        policy.setAgent(agent);
        policies.add(policy);

        when(policyRepository.findAllByExpiringSoon(any(), any())).thenReturn(policies);

        assertNotNull(policyService.getExpiringSoon());
    }

    @Test
    public void testUploadDocs(){
        ResponseEntity<?> response = ResponseEntity.ok("Document Uploaded Successfully");
        when(documentService.uploadDocs(anyLong(), any(MultipartFile.class)))
                .thenAnswer(invocationOnMock ->  response);

        ResponseEntity<?> result = policyService.uploadDocs(1L, mock(MultipartFile.class));

        assertNotNull(result);
        assertEquals("Document Uploaded Successfully", result.getBody());
    }

    @Test
    public void testListDocs(){
        ResponseEntity<?> response = ResponseEntity.ok("Document Uploaded Successfully");
        Policy policy = new Policy();

        when(policyRepository.findById(any())).thenReturn(Optional.of(policy));
        when(policySecurity.checkAuth(any())).thenReturn(true);
        when(documentService.listDocs(anyLong()))
                .thenAnswer(invocationOnMock ->  response);

        ResponseEntity<?> result = policyService.listDocs(1L);

        assertNotNull(result);
    }
}
