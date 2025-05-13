package com.hcc.enums;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.hcc.exceptions.assignmentexceptions.InvalidAssignmentStatusException;

import java.util.Arrays;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AssignmentStatusEnum {
    PENDING_SUBMISSION("Pending Submission", 1),
    SUBMITTED("Submitted", 2),
    IN_REVIEW("In Review", 3),
    NEEDS_UPDATE("Needs Update", 4),
    COMPLETED("Completed", 5),
    RESUBMITTED("Resubmitted", 6);

    private String status;
    private Integer step;
    AssignmentStatusEnum(String status, Integer step) {
        this.status = status;
        this.step = step;
    }

    public String getStatus() {
        return status;
    }

    public Integer getStep() {
        return step;
    }

    public static AssignmentStatusEnum fromStatusStep(Integer step) {
        return Arrays.stream(values())
                .filter((e)-> e.getStep().equals(step))
                .findFirst()
                .orElseThrow(() -> new InvalidAssignmentStatusException("This Status Step does not Exist"));
    }
}
