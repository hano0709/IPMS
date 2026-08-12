package com.bajaj.IPMS.service;

import com.bajaj.IPMS.model.Customer;
import com.bajaj.IPMS.model.RefreshToken;
import com.bajaj.IPMS.model.RegisterRequest;
import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.CustomerRepository;
import com.bajaj.IPMS.repository.RefreshTokenRepository;
import com.bajaj.IPMS.repository.UserRepository;
import com.bajaj.IPMS.security.CustomerSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.Map;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

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

    public ResponseEntity<?> updateCustomers(@RequestBody Map<String, String> request){
        String customerCode = request.get("customerCode");
        Customer customer = customerRepository.findByCustomerCode(customerCode);
        if(customer == null){
            return ResponseEntity.badRequest().body(Map.of("Error", "Customer not Found"));
        }

        for(Map.Entry<String, String> entry: request.entrySet()){
            String key = entry.getKey();;
            String value = entry.getValue();

            switch (key){
                case "fullName":
                    customer.setFullName(value);
                    break;
                case "dateOfBirth":
                    LocalDate dob = LocalDate.parse(value);
                    customer.setDateOfBirth(dob);
                    break;
                case "gender":
                    customer.setGender(value);
                    break;
                case "phone":
                    customer.setPhone(value);
                    break;
                case "address":
                    customer.setAddress(value);
                    break;
                case "kycStatus":
                    customer.setKycStatus(value);
            }
        }

        customerRepository.save(customer);

        return ResponseEntity.ok(Map.of(
                "Message", "Customer updated successfully",
                "CustomerCode", customerCode
        ));
    }

    public ResponseEntity<?> deleteCustomer(String customerCode){
        Customer customer = customerRepository.findByCustomerCode(customerCode);

        User user = customer.getUser();

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user);
        if(refreshToken != null) {
            refreshTokenRepository.delete(refreshToken);
        }
        customerRepository.delete(customer);
        userRepository.delete(user);

        return ResponseEntity.ok(Map.of("Message", "Customer deleted successfully"));
    }
}
