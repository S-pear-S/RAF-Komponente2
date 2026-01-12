package com.gaming.sessionservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class SessionAttendanceReport {
    private Long organizerId;
    private List<Long> presentUserIds;
    private List<Long> absentUserIds;
}
