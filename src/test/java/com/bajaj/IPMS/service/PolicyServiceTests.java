package com.bajaj.IPMS.service;

import com.bajaj.IPMS.model.*;
import com.bajaj.IPMS.repository.AgentRepository;
import com.bajaj.IPMS.repository.CustomerRepository;
import com.bajaj.IPMS.repository.PolicyAuditLogRespository;
import com.bajaj.IPMS.repository.PolicyRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    PolicyAuditLogRespository policyAuditLogRespository;

    @Mock
    AgentRepository agentRepository;

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
        Map<String, String> request = new HashMap<>();
        request.put("policyType", "LIFE");
        request.put("customerCode", "CUST-12323123");
        request.put("agentCode", "AGT-1232331");
        request.put("sumInsured", "5000000");
        request.put("startDate", "2026-08-14");
        request.put("endDate", "2027-08-13");

        when(policyRepository.count()).thenReturn(0L);

        Customer customer = new Customer();
        customer.setDateOfBirth(LocalDate.now());
        when(customerRepository.findByCustomerCode(request.get("customerCode"))).thenReturn(customer);

        Agent agent = new Agent();
        when(agentRepository.findByAgentCode(request.get("agentCode"))).thenReturn(agent);

        User user = new User();
        when(userService.getCurrUser()).thenReturn(user);

        Policy policy = new Policy();
        policy.setStartDate(LocalDate.parse(request.get("startDate")));
        policy.setEndDate(LocalDate.parse(request.get("endDate")));
        when(policyRepository.save(any())).thenReturn(policy);

        PolicyAuditLog policyAuditLog = new PolicyAuditLog();
        when(policyAuditLogRespository.save(any())).thenReturn(policyAuditLog);

        assertNotNull(policyService.createPolicy(request));

        request.put("policyType", "HEALTH");
        assertNotNull(policyService.createPolicy(request));

        request.put("policyType", "MOTOR");
        assertNotNull(policyService.createPolicy(request));

        request.put("policyType", "PROPERTY");
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
        assertNotNull(policyService.getPolicy(any()));
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
        when(policyAuditLogRespository.findByPolicyId(any())).thenReturn(policyAuditLog);
        when(userService.getCurrUser()).thenReturn(user);
        when(policyRepository.save(any())).thenReturn(policy);
        when(policyAuditLogRespository.save(any())).thenReturn(policyAuditLog);

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

        PolicyAuditLog policyAuditLog = new PolicyAuditLog();

        when(policyRepository.findByPolicyNumber(any())).thenReturn(policy);
        when(policyAuditLogRespository.findByPolicyId(any())).thenReturn(policyAuditLog);
        when(policyRepository.save(any())).thenReturn(policy);
        when(policyAuditLogRespository.save(any())).thenReturn(policyAuditLog);

        assertNotNull(policyService.activatePolicy(any()));

        policy.setStatus("ACTIVE");
        assertNotNull(policyService.activatePolicy(any()));
    }

    @Test
    public void testRenewPolicy(){
        Policy policy = new Policy();
        policy.setStatus("ACTIVE");

        PolicyAuditLog policyAuditLog = new PolicyAuditLog();

        when(policyRepository.findByPolicyNumber(any())).thenReturn(policy);
        when(policyAuditLogRespository.findByPolicyId(any())).thenReturn(policyAuditLog);
        when(policyRepository.save(any())).thenReturn(policy);
        when(policyAuditLogRespository.save(any())).thenReturn(policyAuditLog);

        assertNotNull(policyService.renewPolicy(any()));

        policy.setStatus("DRAFT");
        assertNotNull(policyService.renewPolicy(any()));
    }

    @Test
    public void testSuspendPolicy(){
        Policy policy = new Policy();
        policy.setStatus("ACTIVE");

        PolicyAuditLog policyAuditLog = new PolicyAuditLog();

        when(policyRepository.findByPolicyNumber(any())).thenReturn(policy);
        when(policyAuditLogRespository.findByPolicyId(any())).thenReturn(policyAuditLog);
        when(policyRepository.save(any())).thenReturn(policy);
        when(policyAuditLogRespository.save(any())).thenReturn(policyAuditLog);

        assertNotNull(policyService.suspendPolicy(any()));

        policy.setStatus("DRAFT");
        assertNotNull(policyService.suspendPolicy(any()));
    }

    @Test
    public void testCancelPolicy(){
        Policy policy = new Policy();

        PolicyAuditLog policyAuditLog = new PolicyAuditLog();

        when(policyRepository.findByPolicyNumber(any())).thenReturn(policy);
        when(policyAuditLogRespository.findByPolicyId(any())).thenReturn(policyAuditLog);
        when(policyRepository.save(any())).thenReturn(policy);
        when(policyAuditLogRespository.save(any())).thenReturn(policyAuditLog);

        assertNotNull(policyService.cancelPolicy(any()));
    }

    @Test
    public void testGetAudit(){
        Policy policy = new Policy();

        PolicyAuditLog policyAuditLog = new PolicyAuditLog();

        when(policyRepository.findByPolicyNumber(any())).thenReturn(policy);
        when(policyAuditLogRespository.findByPolicyId(any())).thenReturn(policyAuditLog);

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
        when(documentService.listDocs(anyLong()))
                .thenAnswer(invocationOnMock ->  response);

        ResponseEntity<?> result = policyService.listDocs(1L);

        assertNotNull(result);
    }
}
