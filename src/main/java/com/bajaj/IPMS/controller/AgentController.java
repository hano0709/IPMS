package com.bajaj.IPMS.controller;

import com.bajaj.IPMS.model.Agent;
import com.bajaj.IPMS.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
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
}
