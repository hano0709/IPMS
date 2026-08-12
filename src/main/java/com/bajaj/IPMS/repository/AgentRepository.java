package com.bajaj.IPMS.repository;

import com.bajaj.IPMS.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, Long> {

    Agent findByAgentCode(String agentCode);
}
