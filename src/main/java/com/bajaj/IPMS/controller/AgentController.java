package com.bajaj.IPMS.controller;

import com.bajaj.IPMS.DTO.Request.CreateAgentRequest;
import com.bajaj.IPMS.DTO.Response.AgentDTO;
import com.bajaj.IPMS.model.Agent;
import com.bajaj.IPMS.service.AgentService;
import jakarta.validation.Valid;
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
    public ResponseEntity<?> createAgent( @Valid @RequestBody CreateAgentRequest request){
        return agentService.createAgent(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<?> getAgent(@PathVariable("id") Long id){
        Agent agent = agentService.getAgent(id);
        return ResponseEntity.ok(agent);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public List<AgentDTO> searchAgentsByCode(@RequestParam String agentCode) {
        return agentService.searchByCode(agentCode);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAgent(@PathVariable("id") String agentCode){
        return agentService.deleteAgent(agentCode);
    }

    @GetMapping("/policies")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<?> getPolicies(){
        return agentService.getPolicies();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<?> getCurrAgent(){
        return agentService.getCurrAgent();
    }
}
