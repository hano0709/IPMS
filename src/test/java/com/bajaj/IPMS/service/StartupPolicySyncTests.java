package com.bajaj.IPMS.service;

import com.bajaj.IPMS.repository.PolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StartupPolicySyncTests {

    @InjectMocks
    StartupPolicySync startupPolicySync;

    @Mock
    PolicyRepository policyRepository;

    @Test
    public void testCheckAndExpirePoliciesOnStartupSuccess() {
        when(policyRepository.expireOverduePolicies()).thenReturn(5);

        startupPolicySync.checkAndExpirePoliciesOnStartup();

        verify(policyRepository, times(1)).expireOverduePolicies();
    }
}
