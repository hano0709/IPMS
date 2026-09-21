package com.bajaj.IPMS.service;

import com.bajaj.IPMS.repository.PolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StartupPolicySync {

    private static final Logger logger = LoggerFactory.getLogger(StartupPolicySync.class);
    private final PolicyRepository policyRepository;

    public StartupPolicySync(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void checkAndExpirePoliciesOnStartup() {
        logger.info("Dev server running. Syncing expired records via database native timestamps...");
        try {
            // No parameter passing required anymore!
            int rowsUpdated = policyRepository.expireOverduePolicies();

            logger.info("Policy sync completed successfully. Expired {} overdue records.", rowsUpdated);
        } catch (Exception e) {
            logger.error("An error occurred during the startup policy expiration sweep: ", e);
        }
    }
}
