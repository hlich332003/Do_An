package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.service.ChatbotService;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*")
public class ChatbotResource {

    private static final Logger LOG = LoggerFactory.getLogger(ChatbotResource.class);

    private final ChatbotService chatbotService;

    public ChatbotResource(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> ask(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");
        LOG.debug("REST request to ask chatbot : {}", message);
        Map<String, String> result = new HashMap<>();
        result.put("reply", chatbotService.reply(message));
        return ResponseEntity.ok(result);
    }
}
