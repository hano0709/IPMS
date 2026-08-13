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
}
