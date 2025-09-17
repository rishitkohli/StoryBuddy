package com.example.demo.controller;


import java.io.IOException;
import java.util.Map;

import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.ByteArrayOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.ChatRequest;
import com.example.demo.model.ChatResponse;
import com.example.demo.service.GeminiService;

@RestController
@RequestMapping("/api/chatbot-3")
@CrossOrigin(origins = "http://127.0.0.1:5500") 
public class ChatbotController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping
    public ChatResponse handleChat(@RequestBody ChatRequest request) {
        String result = geminiService.generateResponse(request.getStory(), request.getDomain());
        return new ChatResponse(result);
    }
    @PostMapping("/question")
    public ChatResponse handleQuestion(@RequestBody Map<String, String> payload) {
        String question = payload.get("question");
        String result = geminiService.generateResponseForQuestion(question);
        return new ChatResponse(result);
    }
    
    @PostMapping("/requirement-to-story")
    public ChatResponse convertRequirementToStory(@RequestBody Map<String, String> payload) {
        String requirement = payload.get("requirement");
        if (requirement == null || requirement.isBlank()) {
            return new ChatResponse("Requirement text is missing.");
        }

        String story = geminiService.convertRequirementToStory(requirement);
        return new ChatResponse(story);
    }
    
    @PostMapping("/estimate-effort")
    public ChatResponse estimateEffort(@RequestBody Map<String, String> payload) {
        String story = payload.get("story");
        String estimate = geminiService.estimateEffortFromStory(story);
        return new ChatResponse(estimate);
    }


    @PostMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcelFromAnalyzer(@RequestBody Map<String, String> payload) {
        String story = payload.get("story");
        String geminiResponse = geminiService.generateTestCasesForExcel(story);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("QA Scenarios");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("S.No.");
            header.createCell(1).setCellValue("Type");
            header.createCell(2).setCellValue("Title");
            header.createCell(3).setCellValue("Status");

            String[] lines = geminiResponse.split("\\n");
            int rowNum = 1;
            for (String line : lines) {
                String[] parts = line.split(" - ");
                if (parts.length >= 2) {
                    Row row = sheet.createRow(rowNum);
                    row.createCell(0).setCellValue(rowNum);
                    row.createCell(1).setCellValue(parts[0]);
                    row.createCell(2).setCellValue(parts[1]);
                    row.createCell(3).setCellValue("Yet to Execute"); // Default
                    rowNum++;
                }
            }

            // Add dropdown to Status column
            DataValidationHelper validationHelper = sheet.getDataValidationHelper();
            DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(
                    new String[]{"Yet to Execute", "Pass", "Fail"});
            CellRangeAddressList addressList = new CellRangeAddressList(1, 999, 3, 3); // D2:D1000
            DataValidation validation = validationHelper.createValidation(constraint, addressList);
            validation.setShowErrorBox(true);
            sheet.addValidationData(validation);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            byte[] excelBytes = out.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment().filename("QA_Scenarios.xlsx").build());

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Failed to generate Excel: " + e.getMessage()).getBytes());
        }
    }





}
