package io.student.rococo.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.student.rococo.data.entity.UserEntity;
import io.student.rococo.util.BytesAsString;
import io.student.rococo.util.StringAsBytes;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserJson(UUID id, String username, String firstname, String lastname, String avatar) {
  public static UserJson fromEntity(UserEntity entity) {
    return new UserJson(
        entity.getId(),
        entity.getUsername(),
        entity.getFirstname(),
        entity.getLastname(),
        new BytesAsString(entity.getAvatar()).string()
    );
  }

  public UserEntity toEntity() {
    UserEntity ue = new UserEntity();
    ue.setUsername(username);
    ue.setFirstname(firstname);
    ue.setLastname(lastname);
    ue.setAvatar(new StringAsBytes(avatar).bytes());
    return ue;
  }
}
