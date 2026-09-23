package com.bajaj.IPMS.service;

import com.bajaj.IPMS.model.Policy;
import com.bajaj.IPMS.model.PolicyDocuments;
import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.PolicyDocumentsRepository;
import com.bajaj.IPMS.repository.PolicyRepository;
import com.bajaj.IPMS.security.PolicySecurity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTests {

    @InjectMocks
    DocumentService documentService;

    @Mock
    MultipartFile multipartFile;

    @Mock
    PolicyRepository policyRepository;

    @Mock
    PolicyDocumentsRepository policyDocumentsRepository;

    @Mock
    UserService userService;

    @Mock
    PolicySecurity policySecurity;

    @Test
    public void testUploadDocs() throws IOException {
        Policy policy = new Policy();

        User user = new User();
        user.setId(99L);

        when(multipartFile.getOriginalFilename()).thenReturn("test.pdf");
        doNothing().when(multipartFile).transferTo(any(File.class));
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(userService.getCurrUser()).thenReturn(user);

        ResponseEntity<?> response = documentService.uploadDocs(1L, multipartFile);

        assertEquals("Document Uploaded Successfully", response.getBody());
    }

    @Test
    public void testListDocs() {
        Policy policy = new Policy();
        policy.setPolicyNumber("POL123");

        PolicyDocuments doc = new PolicyDocuments();
        doc.setFileName("test.pdf");
        doc.setPolicy(policy);

        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policySecurity.checkAuth("POL123")).thenReturn(true);
        when(policyDocumentsRepository.findAllByPolicyId(1L)).thenReturn(List.of(doc));

        ResponseEntity<?> response = documentService.listDocs(1L);

        assertTrue(response.getBody() instanceof List);
        List<?> result = (List<?>) response.getBody();
        assertEquals(1, result.size());
    }

    @Test
    public void testDownloadFileSuccess() {
        PolicyDocuments doc = new PolicyDocuments();
        doc.setFileName("test.pdf");

        when(policyDocumentsRepository.findById(1L)).thenReturn(Optional.of(doc));

        ResponseEntity<?> response = documentService.downloadFile(1L);

        assertEquals("attachment; filename=test.pdf",
                response.getHeaders().getFirst("Content-Disposition"));
        assertEquals(org.springframework.http.MediaType.APPLICATION_PDF,
                response.getHeaders().getContentType());
    }
}
