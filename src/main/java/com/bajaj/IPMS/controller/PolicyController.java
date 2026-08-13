package com.bajaj.IPMS.controller;

import com.bajaj.IPMS.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/policies")
public class PolicyController {

    @Autowired
    PolicyService policyService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<?> getAllPolicies(){
        return policyService.getAllPolicies();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPolicy(@PathVariable("id") String policyNumber){
        return policyService.getPolicy(policyNumber);
    }

    @PostMapping
    public ResponseEntity<?> createPolicy(@RequestBody Map<String, String> request){
        return policyService.createPolicy(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<?> updatePolicy(@PathVariable("id") String policyNumber, @RequestBody Map<String, String> request){
        return policyService.updatePolicy(policyNumber, request);
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<?> activatePolicy(@PathVariable("id") String policyNumber){
        return policyService.activatePolicy(policyNumber);
    }

    @PatchMapping("/{id}/renew")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<?> renewPolicy(@PathVariable("id") String policyNumber){
        return policyService.renewPolicy(policyNumber);
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<?> suspendPolicy(@PathVariable("id") String policyNumber){
        return policyService.suspendPolicy(policyNumber);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<?> cancelPolicy(@PathVariable("id") String policyNumber){
        return policyService.cancelPolicy(policyNumber);
    }

    @GetMapping("/{id}/audit")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<?> getAudit(@PathVariable("id") String policyNumber){
        return policyService.getAudit(policyNumber);
    }

    @GetMapping("/expiring-soon")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<?> getExpiring(){
        return policyService.getExpiringSoon();
    }
}
