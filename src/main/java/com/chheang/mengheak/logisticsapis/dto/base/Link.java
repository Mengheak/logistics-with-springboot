package com.chheang.mengheak.logisticsapis.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@JsonPropertyOrder({"self", "first", "last", "previous", "next"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class Link {
    private String self;
    private String first;
    private String last;
    private String previous;
    private String next;
}
