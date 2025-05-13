package com.hcc.dtos.request.assignmentdto;


import javax.validation.constraints.NotNull;

public class CreateAssignmentRequestDto {

    @NotNull(message = "assignment Number should be provided")
    private final Integer assignmentNumber;
    @NotNull(message = "userId should be provided")
    private final Long userId;

    public Integer getAssignmentNumber() {
        return assignmentNumber;
    }

    public Long getUserId() {
        return userId;
    }

    public CreateAssignmentRequestDto( Integer assignmentNumber, Long userId) {
        this.assignmentNumber = assignmentNumber;
        this.userId = userId;
    }
}
