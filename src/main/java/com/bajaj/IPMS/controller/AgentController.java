package com.bajaj.IPMS.controller;

import com.bajaj.IPMS.DTO.AgentDTO;
import com.bajaj.IPMS.DTO.PolicyDTO;
import com.bajaj.IPMS.model.Agent;
import com.bajaj.IPMS.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agents")
public class AgentController {

    @Autowired
    AgentService agentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAgent(@RequestBody Map<String, String> request){
        return agentService.createAgent(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAgent(@PathVariable("id") Long id){
        try {
            Agent agent = agentService.getAgent(id);
            return ResponseEntity.ok(agent);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "Error", e.getMessage()
            ));
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public List<AgentDTO> searchCustomersByCode(@RequestParam String agentCode) {
        return agentService.searchByCode(agentCode);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAgent(@PathVariable("id") String agentCode){
        return agentService.deleteAgent(agentCode);
    }
}
