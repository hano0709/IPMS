package com.bajaj.IPMS.service;

import com.bajaj.IPMS.model.Customer;
import com.bajaj.IPMS.model.RegisterRequest;
import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.CustomerRepository;
import com.bajaj.IPMS.security.CustomerSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    CustomerSecurity customerSecurity;

    @Autowired
    UserService userService;

    @Autowired
    AuthService authService;

    public Page<Customer> getAll(Pageable pageable){

        return customerRepository.findAll(pageable);
    }

    public Customer getCustomer(Long customerId){
        if(customerSecurity.checkAuth(customerId)) {
            return customerRepository.findById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("Customer Not Found"));
        } else {
            throw new IllegalArgumentException("No Authorisation");
        }
    }

    public ResponseEntity<?> createCustomer(@RequestBody Map<String, String> request){
        Customer customer = new Customer();
        RegisterRequest registerRequest = new RegisterRequest();

        String email = request.get("email");
        String password = request.get("password");
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);

        User user = authService.register(registerRequest);

        customer.setUser(user);

        String prefix = "CUST-";
        String code = String.format("%015d", customerRepository.count()+1);
        String customerCode = prefix + code;
        customer.setCustomerCode(customerCode);

        customer.setFullName(request.get("fullName"));

        String dobString = request.get("dateOfBirth");
        LocalDate dob = LocalDate.parse(dobString); // expects yyyy-MM-dd
        customer.setDateOfBirth(dob);

        customer.setGender(request.get("gender"));
        customer.setPhone(request.get("phone"));
        customer.setAddress(request.get("address"));

        User currUser = userService.getCurrUser();
        customer.setCreatedBy(currUser.getId());

        customerRepository.save(customer);

        return ResponseEntity.ok(Map.of(
                "Customer created with email", email,
                "Customer code", customer.getCustomerCode()
        ));
    }
}
