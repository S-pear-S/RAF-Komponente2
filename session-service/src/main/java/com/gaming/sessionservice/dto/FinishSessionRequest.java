package com.gaming.sessionservice.dto;
import lombok.Data;
import java.util.List;

@Data
public class FinishSessionRequest {
    private List<Long> absentPlayerIds;
}