package com.company.filehub.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class FileUploadRequest {

    @NotEmpty(message = "수신자를 1명 이상 지정해야 합니다")
    private List<Long> recipientIds;

    private String memo;
}
