package com.hcc.controllers;

import com.hcc.services.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class AssignmentController {

    @Autowired
    private final AssignmentService assignmentService;


    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }


}
