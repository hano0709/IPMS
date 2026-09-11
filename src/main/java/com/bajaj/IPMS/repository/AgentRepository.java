package com.bajaj.IPMS.repository;

import com.bajaj.IPMS.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgentRepository extends JpaRepository<Agent, Long> {

    List<Agent> findByAgentCodeContainingIgnoreCase(String agentCode);

    Agent findByAgentCode(String agentCode);
}
