package com.tencent.supersonic.chat.api.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SchemaElementMatch {

    SchemaElement element;
    double similarity;
    String detectWord;
    String word;
    Long frequency;
    MatchMode mode = MatchMode.CURRENT;

    public enum MatchMode {
        CURRENT,
        INHERIT
    }
}
