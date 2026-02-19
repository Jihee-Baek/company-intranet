package com.company.filehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class NoticeRequest {

    @NotBlank(message = "제목을 입력해 주세요")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다")
    private String title;

    @NotBlank(message = "내용을 입력해 주세요")
    private String content;

    private boolean isPinned;
}
