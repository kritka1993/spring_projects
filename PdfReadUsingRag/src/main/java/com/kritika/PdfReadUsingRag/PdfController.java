package com.kritika.PdfReadUsingRag;


import com.kritika.PdfReadUsingRag.PdfService.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfService pdfRagService;

    public PdfController(PdfService pdfRagService) {
        this.pdfRagService = pdfRagService;
    }


    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "Please upload a PDF file"
                    ));
        }

        pdfRagService.uploadPdf(file);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "PDF uploaded and indexed successfully"
                )
        );
    }

    @GetMapping("/ask")
    public ResponseEntity<?> ask(
            @RequestParam String question) {

        String answer =
                pdfRagService.askQuestion(question);

        return ResponseEntity.ok(
                Map.of(
                        "question", question,
                        "answer", answer
                )
        );
    }
}