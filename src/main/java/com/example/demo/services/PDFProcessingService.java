package com.example.demo.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PDFProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(PDFProcessingService.class);
    
    /**
     * Extracts text content from a PDF file
     * 
     * @param pdfFile The uploaded PDF file
     * @return Extracted text content
     * @throws IOException If file cannot be read or processed
     */
    public String extractTextFromPDF(MultipartFile pdfFile) throws IOException {
        logger.info("Extracting text from PDF: {}", pdfFile.getOriginalFilename());
        
        try (PDDocument document = PDDocument.load(pdfFile.getInputStream())) {
            PDFTextStripper textStripper = new PDFTextStripper();
            String text = textStripper.getText(document);
            
            logger.info("Successfully extracted {} characters of text", text.length());
            return text;
        } catch (IOException e) {
            logger.error("Failed to extract text from PDF", e);
            throw e;
        }
    }
}