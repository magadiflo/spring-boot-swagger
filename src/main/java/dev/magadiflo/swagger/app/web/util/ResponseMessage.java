package dev.magadiflo.swagger.app.web.util;

import com.fasterxml.jackson.annotation.JsonInclude;

public record ResponseMessage<T>(String message,
                                 @JsonInclude(JsonInclude.Include.NON_NULL) T content) {
}
