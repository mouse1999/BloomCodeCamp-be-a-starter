package com.hcc.dtos.request.assignmentdto;



import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min; // Example, if assignmentNumber must be positive

public class CreateAssignmentRequest {

    @NotNull(message = "Assignment number is required")
    @Min(value = 1, message = "Assignment number must be positive") // Example validation
    private Integer assignmentNumber;



    public CreateAssignmentRequest() {}

    public Integer getAssignmentNumber() {
        return assignmentNumber;
    }

    public void setAssignmentNumber(Integer assignmentNumber) {
        this.assignmentNumber = assignmentNumber;
    }


}