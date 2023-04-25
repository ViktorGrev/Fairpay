
package no.ntnu.idatt1002.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("User test")
class UserTest {

  private final User user = new User(1, "Username", "password", new Date(),
          new Date(), 12345678);

  @Test
  @DisplayName("Get user ID")
  void getUserId() {
    assertEquals(1, user.getId());
  }

  @Test
  @DisplayName("Get username")
  void getUsername() {
    assertEquals("Username", user.getUsername());
  }

  @Test
  @DisplayName("Set username")
  void setUsername() {
    user.setUsername("Username2");
    assertEquals("Username2", user.getUsername());
  }

  @Test
  @DisplayName("Set invalid username")
  void setInvalidUsername() {
    assertThrows(IllegalArgumentException.class, () -> user.setUsername(null));
  }

  @Test
  @DisplayName("Get phone number")
  void getPhoneNumber() {
    assertEquals(12345678, user.getPhoneNumber());
  }
}