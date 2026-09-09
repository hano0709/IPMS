package com.bajaj.IPMS.controller;

import com.bajaj.IPMS.DTO.CustomerDTO;
import com.bajaj.IPMS.service.CustomerService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customers")
@EnableMethodSecurity
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public List<CustomerDTO> getAllCustomer(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String kycStatus,
            @RequestParam(required = false) String searchName
    ){
        Pageable pageable = PageRequest.of(page, size);

        return customerService.getAll(pageable, kycStatus, searchName).getContent();
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<?> getCustomerCount(){
        return customerService.getCustomerCount();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomer(@PathVariable("id") Long customerId){
        try{
            CustomerDTO customerDTO =  customerService.getCustomer(customerId);
            return ResponseEntity.ok(customerDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "Error", e.getMessage()
            ));
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<?> createCustomer(@RequestBody Map<String, String> request){
        return customerService.createCustomer(request);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<?> updateCustomer(@RequestBody Map<String, String> request){
        return customerService.updateCustomers(request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCustomer(@PathVariable("id") String customerCode){
        return customerService.deleteCustomer(customerCode);
    }

    @GetMapping("/{id}/policies")
    public ResponseEntity<?> getAllPolicies(@PathVariable("id") Long customerId){
        return customerService.getAllPolicies(customerId);
    }
}
