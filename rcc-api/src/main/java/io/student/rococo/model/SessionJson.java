package io.student.rococo.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SessionJson(String username, Date issuedAt, Date expiresAt) {
  public static SessionJson empty() {
    return new SessionJson(null, null, null);
  }
}
