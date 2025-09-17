package com.example.demo.service;


import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class GeminiService {

    private final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=AIzaSyBmejvc1388rc7ry6YHoT6NiUg8K4Vjk38";
    
    String context = "We are a software development company that builds customized solutions for insurance providers, focusing on four key domains: "
    		+ "Premium Audit, Survey and Risk Control, Med Connection, and Subrosource. Our clients are insurance companies, and their clients are"
    		+ " the insured policyholders. We follow the Agile methodology with 2-weeks (10 working days) sprint cycles involving roles such as"
    		+ " Business Analysts (BA), Quality Assurance (QA), Developers, Support, and Scrum Masters who facilitate daily stand-ups, sprint planning, and"
    		+ " retrospectives. Tasks are selected from the sprint backlog based on business priorities and are collaboratively executed to deliver"
    		+ " functional software aligned with client needs. Our chatbot is designed to understand insurance domain workflows, assist"
    		+ " in generating and analyzing JIRA stories, estimate story points or hours per role, and provide role-specific insights for"
    		+ " BAs, QAs, and Developers, helping streamline communication and productivity throughout the sprint lifecycle.";
    
    public String generateResponse(String story, String domain) {
        String prompt = buildPrompt(story, domain);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> content = Map.of("parts", List.of(Map.of("text", prompt)));
        Map<String, Object> requestBody = Map.of("contents", List.of(content));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(GEMINI_API_URL, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> contentMap = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, String>> parts = (List<Map<String, String>>) contentMap.get("parts");
                    return parts.get(0).get("text");
                } else {
                    return "No candidates returned from Gemini.";
                }
            } else {
                return "Gemini API returned non-success status: " + response.getStatusCode();
            }
        } catch (Exception e) {
            return "Error communicating with Gemini API: " + e.getMessage();
        }
    }

    private String buildPrompt(String story, String domain) {
        return switch (domain.toUpperCase()) {
            case "BA" ->context + "You are acting as a Business Analyst. Analyze the following JIRA story and provide your response in the exact format below. Ensure the output is in plain text or HTML (not Markdown). Follow these formatting rules strictly:\r\n"
            		+ "\r\n"
            		+ "- Section headings must be in bold using <b> tags.\r\n"
            		+ "- Use bullet points (•) for lists with no extra spacing between bullets.\r\n"
            		+ "- Maintain a maximum line spacing of 1 between sections.\r\n"
            		+ "** VERY IMPORTANT ** : START ALL THE TEXT IMMEDIATLEY AFTER THE HEADINGS IN THE NEXT LINE  AND GIVE EXACTLY 1 LINE OF SPACE BEWEEN SECTIONS.THE TEXT SHOULD BE JUSTIFIED."
            		+ "##.START THE POINTS IN THE NEXT LINE. EVERY HEADING SHOULD BE CENETER ALIGNED HAVE THE FONT COLOR #fb4d0a"
            		+ "give only three short questions"
            		+ "\r\n"
            		+ "Use this structure:\r\n"
            		+ "\r\n"
            		+ "<b>SUMMARY</b>\r\n"
            		//+ "THERE SHOULD BE NO SPACE **START THE SUMMARY IMMEDIATELY**"
            		+ "[Provide a comprehensive summary of the JIRA story, clearly articulating the overall project or feature.Provide it in points]\r\n"
            		+ "\r\n"
            		+ "<b>LARGE INSIGHT AND DEEP DIVE</b>\r\n"
            		+ "• [Strategic or analytical insight 1]  \r\n"
            		+ "• [Insight 2]  \r\n"
            		+ "• [Insight 3]  \r\n"
            		+ "...\r\n"
            		+ "\r\n"
            		+ "<b>ACCEPTANCE CRITERIA</b>\r\n"
            		+ "• [Explicit or implied criterion 1]  \r\n"
            		+ "• [Criterion 2]  \r\n"
            		+ "• [Criterion 3]  \r\n"
            		+ "...\r\n"
            		+ "\r\n"
            		+ "<b>GAPS IN REQUIREMENT</b>\r\n"
            		+ "• [Gap 1]  \r\n"
            		+ "• [Gap 2]  \r\n"
            		+ "• [Gap 3]  \r\n"
            		+ "...\r\n"
            		+ "\r\n"
            		+ "<b>POINTS TO CLARIFY</b>\r\n"
            		+ "1. [First question related to the BA role]  \r\n"
            		+ "2. [Second question related to the BA role]  \r\n"
            		+ "3. [Third question related to the BA role]\r\n"
            		+ "\r\n"
            		+ "Now analyze the following story:\r\n" + story ;
            
	            case "QA" -> context + "You are acting as a QA Engineer. Analyze the following JIRA story and provide your response in the exact format below. Ensure the output is in plain text or HTML (not Markdown). Follow these formatting rules strictly:\r\n"
	            		+ "\r\n"
	            		+ "As a QA Engineer, your response should begin with a concise summary of the JIRA story from a testing perspective, highlighting the"
	            		+ " key functionalities and expected outcomes. Based on the story details and the implied or stated acceptance criteria, you are expected"
	            		+ " to generate a comprehensive set of test cases. The test cases should cover various categories, including"
	            		+ "Please provide:\r\n"
	            		+ "Positive test scenarios – covering valid flows that meet acceptance criteria.\r\n"
	            		+ "Negative test scenarios – to test invalid data, boundary conditions, and edge cases.\r\n"
	            		+ "Validation test scenarios – covering mandatory fields, error messages, and input validations.\r\n"
	            		+ "Integration scenarios – to verify dependencies with other modules, systems, or APIs.\r\n"
	            		+ "UI/UX scenarios – if relevant, covering usability, responsiveness, and accessibility aspects.\r\n"
	            		+ "Security scenarios – if relevant, including role-based access, permissions, and data privacy.\r\n"
	            		+ " Additionally, your response should address considerations for Performance Testing (e.g., load, stress, and responsiveness) and Accessibility Testing (e.g., compliance with WCAG standards)."
	            		+ "- Section headings must be in bold using <b> tags."
	            		+ "make sure all the test scenarios start with \"Verify\" and generate a maximum of 3 scenarios per type \r\n"
	            		+ "- Use bullet points (•) for lists with no extra spacing between bullets.\r\n"
	            		+ "- Maintain a maximum line spacing of 1 between sections.\r\n"
	            		+ "** VERY IMPORTANT ** : START ALL THE TEXT IMMEDIATLEY AFTER THE HEADINGS IN THE NEXT LINE  AND GIVE EXACTLY 1 LINE OF SPACE BEWEEN SECTIONS.THE TEXT SHOULD BE JUSTIFIED."
	            		+ "##.START THE POINTS IN THE NEXT LINE. EVERY HEADING SHOULD BE CENETER ALIGNED HAVE THE FONT COLOR #fb4d0a"
	            		+ "Give only three short questions"
	            		+ "\r\n"
	            		+ "Use this structure:\r\n"
	            		+ "\r\n"
	            		+ "<b>SUMMARY</b>\r\n"
	            		+ "[Provide a clear summary of the JIRA story from a testing perspective.give it in points]\r\n"
	            		+ "\r\n"
	            		+ "<b>TEST SCENARIOS</b>\r\n"
	            		+ "• [type1(bold)] "
	            		+ "	-test case 1]  \r\n"	            		
	            		+ "	-test case 2]  \r\n"
	            		+ "	-test case 3]  \r\n"
	            		+ "• [type2(bold)]  \r\n"
	            		+ "	-test case 1]  \r\n"	            		
	            		+ "	-test case 2]  \r\n"
	            		+ "	-test case 3]  \r\n"
	            		+ "• [type3(bold)]  \r\n"
	            		+ "	-test case 1]  \r\n"	            		
	            		+ "	-test case 2]  \r\n"
	            		+ "	-test case 3]  \r\n"
	            		//+ "• [Unit test case 1]  \r\n"
	            		+ "...\r\n"
	            		+ "\r\n"
	            		+ "<b>PERFORMANCE </b>\r\n"
	            		+ "• [Performance testing consideration]  \r\n"
	            		+ "...\r\n"
	            		+ "<b>ACCESSIBILITY</b>\r\n"
	            		+ "• [Accessibility testing consideration]  \r\n"
	            		+ "\r\n"
	            		+ "<b>POINTS TO CLARIFY</b>\r\n"
	            		+ "1. [First question related to the QA role]  \r\n"
	            		+ "2. [Second question related to the QA role]  \r\n"
	            		+ "3. [Third question related to the QA role]\r\n"
	            		+ "\r\n"
	            		+ "Now analyze the following story:\r\n" + story ;
            
            case "DEV" -> context  + "You are acting as a Developer. Analyze the following JIRA story and provide your response in the exact format below. Ensure the output is in plain text or HTML (not Markdown). Follow these formatting rules strictly:\r\n"
            		+ "\r\n"
            		+ "- Section headings must be in bold using <b> tags.\r\n"
            		+ "- Use bullet points (•) for lists with no extra spacing between bullets.\r\n"
            		+ "- Maintain a maximum line spacing of 1 between sections.\r\n"
            		+ "** VERY IMPORTANT ** : START ALL THE TEXT IMMEDIATLEY AFTER THE HEADINGS IN THE NEXT LINE  AND GIVE EXACTLY '1' LINE OF SPACE BEWEEN SECTIONS."
            		+ "##.START THE POINTS IN THE NEXT LINE. EVERY HEADING SHOULD BE CENETER ALIGNED HAVE THE FONT COLOR #fb4d0a"
            		+ "give only three short questions"
            		+ "\r\n"
            		+ "Use this structure:\r\n"
            		+ "\r\n"
            		+ "<b>SUMMARY</b>\r\n"
            		+ "[Provide a concise summary of the JIRA story.Give it in points]\r\n"
            		+ "\r\n"
            		+ "<b>WHAT NEEDS TO BE BUILT</b>\r\n"
            		+ "[Describe what needs to be built from a technical perspective, including any database or architectural implications in points.]\r\n"
            		+ "\r\n"
            		+ "<b>ACCEPTANCE CRITERIA</b>\r\n"
            		+ "• [First acceptance criterion]  \r\n"
            		+ "• [Second acceptance criterion]  \r\n"
            		+ "• [Third acceptance criterion]  \r\n"
            		+ "...\r\n"
            		+ "\r\n"
            		+ "<b>UNIT TEST CASES</b>\r\n"
            		+ "• [First test case idea]  \r\n"
            		+ "• [Second test case idea]  \r\n"
            		+ "• [Third test case idea]  \r\n"
            		+ "...\r\n"
            		+ "\r\n"
            		+ "<b>POINTS TO CLARIFY</b>\r\n"
            		+ "1. [First question related to the Developer's role]  \r\n"
            		+ "2. [Second question related to the Developer's role]  \r\n"
            		+ "3. [Third question related to the Developer's role]\r\n"
            		+ "\r\n"
            		+ "Now analyze the following story:" + story ;
            
            default -> "Invalid domain.";
        };
    }
    public String generateResponseForQuestion(String question) {
        String prompt = "Please answer the following question in brief:\n\n" + question;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> content = Map.of("parts", List.of(Map.of("text", prompt)));
        Map<String, Object> requestBody = Map.of("contents", List.of(content));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(GEMINI_API_URL, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> contentMap = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, String>> parts = (List<Map<String, String>>) contentMap.get("parts");
                    return parts.get(0).get("text");
                } else {
                    return "No candidates returned from Gemini.";
                }
            } else {
                return "Gemini API returned non-success status: " + response.getStatusCode();
            }
        } catch (Exception e) {
            return "Error communicating with Gemini API: " + e.getMessage();
        }
    }
    private String callGeminiAPI(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> content = Map.of("parts", List.of(Map.of("text", prompt)));
        Map<String, Object> requestBody = Map.of("contents", List.of(content));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(GEMINI_API_URL, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> contentMap = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, String>> parts = (List<Map<String, String>>) contentMap.get("parts");
                    return parts.get(0).get("text");
                } else {
                    return "No candidates returned from Gemini.";
                }
            } else {
                return "Gemini API returned non-success status: " + response.getStatusCode();
            }
        } catch (Exception e) {
            return "Error communicating with Gemini API: " + e.getMessage();
        }
    }
    
    public String estimateEffortFromStory(String story) {
        String prompt =context+ "You are acting as a Software Estimator."
        		+ " Based on the following JIRA story or requirement, estimate the number of working days required to complete the task."
        		+ "also give the split of dyas required by each role(ba,  dev and qa) in a sprint. and there are multiple people working under"
        		+ " a specific role simultaneously . not necesarry for every role to take up minimum 1 day in such cases show them as less than one day "
        		//+ "keep in mind that a sprint only lasts 10 working days and it contains multiple stories, bugs, tasks etc"
        		+ "make sure you give the number of days in integer format no floating point number "
        		+ "the estimated days should be in the range of 1 to 8 days"
        		+ "** VERY IMPORTANT ** : START ALL THE TEXT IMMEDIATLEY AFTER THE HEADINGS IN THE NEXT LINE  AND GIVE EXACTLY '1' LINE OF SPACE BEWEEN SECTIONS.  "   
        		+ "##.START THE POINTS IN THE NEXT LINE ONLY AFTER ONE POINT ENDS.MAKE SURE THE SUM OF NUMBER OR DAYS ALLOTED TO EACH ROLE IS EQUAL TO THE TOTAL NUMBER OF DAYS FOR THE SPRINT "
        		+ "Your response must follow this format and formatting rules:\r\n"
        		+ "\r\n"
        		+ "- Output must be in plain text or HTML (not Markdown).\r\n"
        		+ "- Use <b> tags for bold headings.\r\n"
        		+ "- Keep a maximum line spacing of 1 between sections.\r\n"
        		+ "\r\n"
        		+ "Use this structure:\r\n"
        		+ "\r\n"
        		+ "<b>ESTIMATED WORKING DAYS</b>(FONT COLOR #fb4d0a)\r\n"
        		+ "[Provide the estimated number of working days (e.g., 5 days)]\r\n"
        		+ "\r\n"
        		+ "<b>JUSTIFICATION</b>(FONT COLOR #fb4d0a)\r\n"
        		+ "[Briefly explain the reasoning behind the estimate, considering development, testing, and review cycles.and  succinct the text"
        		+ "give this role wise(BOLD) and in points. ]\r\n"
        		+ "• [Development(bold) : (Number of Days Font )] "
        		+ "	-points]  \r\n"	            		
        		+ "	-points]  \r\n"
        		+ "	-[points]  \r\n"
        		+ "\r\n"
        		+ "• [BA Support & Validation(bold) : (Number of Days)] "
        		+ "	-points]  \r\n"	            		
        		+ "	-points]  \r\n"
        		+ "	-[points]  \r\n"
        		+ "• [QA Validation(bold) : (Number of Days )] "
        		+ "	-points]  \r\n"	            		
        		+ "	-points]  \r\n"
        		+ "	-[points]  \r\n"
        		
        		+ "Now estimate based on the following input:" + story;

        return callGeminiAPI(prompt);
    }


    
    public String convertRequirementToStory(String requirementText) {
        String prompt = context + "You are acting as a Business Analyst. Convert the following requirement into a well-structured JIRA user story. "
        		+ "Your response must follow this format and formatting rules:\r\n"
        		+ "** VERY IMPORTANT ** : START ALL THE TEXT IMMEDIATLEY AFTER THE HEADINGS IN THE NEXT LINE  AND GIVE EXACTLY '1' LINE OF SPACE BEWEEN SECTIONS."
        		+ "##.START THE POINTS IN THE NEXT LINE. EVERY HEADING SHOULD BE CENETER ALIGNED HAVE THE FONT COLOR #fb4d0a"
        		+ "\r\n"
        		+ "- Output must be in plain text or HTML (not Markdown).\r\n"
        		+ "- Use <b> tags for bold headings.\r\n"
        		+ "- Use bullet points (•) for lists with no extra spacing between bullets.\r\n"
        		+ "- Keep a maximum line spacing of 1 between sections.\r\n"
        		+ "\r\n"
        		+ "Use this structure:\r\n"
        		+ "\r\n"
        		+ "<b>USER STORY</b>\r\n"
        		+ "As a [role], I want [feature] so that [benefit].\r\n"
        		+ "\r\n"
        		+ "<b>OVERVIEW</b>\r\n"
        		+ "[Provide a brief overview of the feature or functionality.]\r\n"
        		+ "\r\n"
        		+ "<b>ACCEPTANCE CRITERIA</b>\r\n"
        		+ "• [Criterion 1]  \r\n"
        		+ "• [Criterion 2]  \r\n"
        		+ "• [Criterion 3]  \r\n"
        		+ "...\r\n"
        		+ "\r\n"
        		+ "<b>QA NOTES</b>\r\n"
        		+ "• [Any QA-specific considerations, if applicable]  \r\n"
        		+ "• [Leave blank if none]\r\n"
        		+ "\r\n"
        		+ "Now convert the following requirement:" + requirementText;

        return callGeminiAPI(prompt);
    }
    public String generateTestCasesForExcel(String story) {
        String prompt = "You are a QA Engineer. Based on the following JIRA story, generate a list of test cases. " +
                        "Each test case should be in the format: Type - Description - Status. "
                        + "all the test cases should start with \"Verify\"" +
                        "donot provide any value for status column. Separate each test case with a new line.\n\n"
                        + "sort on the basis of type"
                        + "test types :"
                        + "Positive test scenarios – covering valid flows that meet acceptance criteria.\r\n"
                        + "Negative test scenarios – to test invalid data, boundary conditions, and edge cases.\r\n"
                        + "Validation test scenarios – covering mandatory fields, error messages, and input validations.\r\n"
                        + "Integration scenarios – to verify dependencies with other modules, systems, or APIs.\r\n"
                        + "UI/UX scenarios – if relevant, covering usability, responsiveness, and accessibility aspects.\r\n"
                        + "Security scenarios – if relevant, including role-based access, permissions, and data privacy.\r\n"
                        + "Performance scenarios – if applicable, to ensure the feature works well under expected loads." + story;
        

        return callGeminiAPI(prompt);
    }




}

