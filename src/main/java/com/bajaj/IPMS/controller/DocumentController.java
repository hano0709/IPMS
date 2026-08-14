package com.bajaj.IPMS.controller;

import com.bajaj.IPMS.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("documents")
public class DocumentController {

    @Autowired
    DocumentService documentService;

    @GetMapping("/{docId}/download")
    public ResponseEntity<?> downloadFile(@PathVariable("docId") Long id){
        return documentService.downloadFile(id);
    }
}
