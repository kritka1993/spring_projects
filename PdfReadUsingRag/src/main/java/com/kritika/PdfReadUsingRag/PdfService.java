package com.kritika.PdfReadUsingRag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public class PdfService
{
    @Autowired
    public VectorStore vectorStore;

    @Autowired
    public ChatClient chatClient;


    public void uploadPdf(MultipartFile file) throws IOException {

        Path tempFile = Files.createTempFile(
                "uploaded-",
                ".pdf"
        );

        Files.write(
                tempFile,
                file.getBytes()
        );

        PagePdfDocumentReader reader =
                new PagePdfDocumentReader(
                        new ByteArrayResource(
                                file.getBytes()
                        )
                );

        List<Document> documents =
                reader.get();

        vectorStore.add(documents);

        Files.deleteIfExists(tempFile);
    }

    public String askQuestion(String question) {

        List<Document> documents =
                vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(question)
                                .topK(5)
                                .build()
                );

        String context = documents.stream()
                .map(Document::getText)
                .reduce(
                        "",
                        (a, b) -> a + "\n\n" + b
                );

        String prompt = """
                You are a PDF question-answering assistant.

                Answer the user's question using ONLY
                the supplied PDF context.

                If the answer is not available in the context,
                say: "I could not find the answer in the PDF."

                PDF CONTEXT:
                %s

                QUESTION:
                %s
                """.formatted(context, question);

        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
    }
}
