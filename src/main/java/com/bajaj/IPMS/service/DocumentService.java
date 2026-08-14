package com.bajaj.IPMS.service;

import com.bajaj.IPMS.DTO.PolicyDocumentsDTO;
import com.bajaj.IPMS.model.Policy;
import com.bajaj.IPMS.model.PolicyDocuments;
import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.PolicyDocumentsRepository;
import com.bajaj.IPMS.repository.PolicyRepository;
import com.bajaj.IPMS.security.PolicySecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DocumentService {

    private final String filePath = "C:/IPMS_Files";

    @Autowired
    PolicyRepository policyRepository;

    @Autowired
    PolicyDocumentsRepository policyDocumentsRepository;

    @Autowired
    UserService userService;

    @Autowired
    PolicySecurity policySecurity;

    public ResponseEntity<?> uploadDocs(Long policyId, MultipartFile file) {
        try {
            Path path = Paths.get(filePath, file.getOriginalFilename());
            file.transferTo(path.toFile());

            Policy policy = policyRepository.findById(policyId)
                    .orElseThrow();
            User user = userService.getCurrUser();

            PolicyDocuments policyDocuments = new PolicyDocuments();
            policyDocuments.setFileName(file.getOriginalFilename());
            policyDocuments.setFilePath(path.toString());
            policyDocuments.setPolicy(policy);
            policyDocuments.setUploadedBy(user.getId());

            policyDocumentsRepository.save(policyDocuments);

            return ResponseEntity.ok("Document Uploaded Successfully");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error while uploading file");
        }
    }

    public ResponseEntity<?> listDocs(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow();

        if (policySecurity.checkAuth(policy.getPolicyNumber())){
            List<PolicyDocuments> policyDocumentsList = policyDocumentsRepository.findAllByPolicyId(policyId);
            List<PolicyDocumentsDTO> policyDocumentsDTOs = new ArrayList<>();
            for (PolicyDocuments policyDocuments: policyDocumentsList){
                policyDocumentsDTOs.add(new PolicyDocumentsDTO(policyDocuments));
            }

            return ResponseEntity.ok(policyDocumentsDTOs);
        }

        return ResponseEntity.badRequest().body(Map.of("Error", "User Not Authorised"));
    }


    public ResponseEntity<?> downloadFile(Long id) {
        PolicyDocuments policyDocuments = policyDocumentsRepository.findById(id)
                .orElse(null);

        if (policyDocuments == null){
            return ResponseEntity.badRequest().body(Map.of("Error", "Document not Found"));
        }

        String fileName = policyDocuments.getFileName();

        File file = new File(filePath, fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .contentType(MediaType.APPLICATION_PDF)
                .body(new FileSystemResource(file));
    }
}
