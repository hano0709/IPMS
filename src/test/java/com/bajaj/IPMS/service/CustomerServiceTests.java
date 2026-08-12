package com.bajaj.IPMS.service;

import com.bajaj.IPMS.model.Customer;
import com.bajaj.IPMS.model.RefreshToken;
import com.bajaj.IPMS.model.RegisterRequest;
import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.CustomerRepository;
import com.bajaj.IPMS.repository.RefreshTokenRepository;
import com.bajaj.IPMS.repository.UserRepository;
import com.bajaj.IPMS.security.CustomerSecurity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTests {

    @InjectMocks
    CustomerService customerService;

    @Mock
    CustomerRepository customerRepository;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CustomerSecurity customerSecurity;

    @Mock
    AuthService authService;

    @Mock
    UserService userService;

    @Test
    public void testGetAll(){
        Pageable pageable = PageRequest.of(0,5);
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFullName("test123");

        Page<Customer> mockPage = new PageImpl<>(List.of(customer));

        when(customerRepository.findAll(pageable)).thenReturn(mockPage);

        Page<Customer> result = customerService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("test123", result.getContent().get(0).getFullName());
    }

    @Test
    public void testGetCustomer(){
        when(customerSecurity.checkAuth(1L))
                .thenReturn(false)
                .thenReturn(true);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(new Customer()));

        assertThrows(IllegalArgumentException.class,() -> customerService.getCustomer(1L));
        assertNotNull(customerService.getCustomer(1L));
    }

    @Test
    public void testCreateCustomer(){
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("password", "Strongpass1!");
        request.put("dateOfBirth", "2005-09-07");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(request.get("email"));
        registerRequest.setPassword(request.get("password"));

        User user = new User();
        user.setId(1L);

        when(authService.register(any(RegisterRequest.class))).thenReturn(new User());
        when(customerRepository.count()).thenReturn(0L);
        when(userService.getCurrUser()).thenReturn(user);
        when(customerRepository.save(any())).thenReturn(new Customer());

        ResponseEntity<?> response = customerService.createCustomer(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testUpdateCustomer(){
        Map<String, String> request = new HashMap<>();
        request.put("customerCode", "CUST-00004234");
        request.put("fullName", "test123");
        request.put("dateOfBirth", "2005-09-07");
        request.put("gender", "MALE");
        request.put("phone", "7774020743");
        request.put("address", "Pune");
        request.put("kycStatus", "VERIFIED");

        Customer customer = new Customer();
        customer.setId(1L);

        when(customerRepository.findByCustomerCode(request.get("customerCode")))
                .thenReturn(null)
                .thenReturn(customer);

        when(customerRepository.save(any())).thenReturn(new Customer());

        ResponseEntity<?> response1 = customerService.updateCustomers(request);
        assertEquals(HttpStatus.BAD_REQUEST, response1.getStatusCode());

        ResponseEntity<?> response2 = customerService.updateCustomers(request);
        assertEquals(HttpStatus.OK, response2.getStatusCode());
    }

    @Test
    public void testDeleteCustomer() {
        String customerCode = "CUST-12323234";
        Customer customer = new Customer();

        when(customerRepository.findByCustomerCode(customerCode)).thenReturn(customer);
        when(refreshTokenRepository.findByUser(any())).thenReturn(new RefreshToken());
        doNothing().when(refreshTokenRepository).delete(any());
        doNothing().when(customerRepository).delete(any());
        doNothing().when(userRepository).delete(any());

        ResponseEntity<?> response = customerService.deleteCustomer(customerCode);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}