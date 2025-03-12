package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/assignment")
public class AssignmentController {

    @Autowired
    private OpenAIService openAIService;

    @PostMapping("/by-ai")
    public ResponseEntity<List<OracleUser>> assignByAi(@RequestBody Map<String, String> payload) {
        String description = payload.get("description");
        if (description == null || description.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            List<OracleUser> sortedUsers = openAIService.getAssignedUsers(description);
            return new ResponseEntity<>(sortedUsers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
