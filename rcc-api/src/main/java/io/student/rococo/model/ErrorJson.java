package io.student.rococo.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorJson(
    List<String> errors,
    String message
) {
  public static ErrorJson of(String... errors) {
    return new ErrorJson(List.of(errors), null);
  }

  public static ErrorJson of(List<String> errors) {
    return new ErrorJson(errors, null);
  }

  public static ErrorJson ofMessage(String message) {
    return new ErrorJson(null, message);
  }
}
