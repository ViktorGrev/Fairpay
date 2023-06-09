package no.ntnu.idatt1002.dao.sqlite;

import no.ntnu.idatt1002.dao.UserDAO;
import no.ntnu.idatt1002.dao.exception.AuthException;
import no.ntnu.idatt1002.dao.exception.DAOException;
import no.ntnu.idatt1002.model.User;

import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.function.Function;

/**
 * This class is an implementation of the {@link UserDAO} interface, using
 * SQLite as the underlying data source.
 *
 * @see SqlDAO
 * @see UserDAO
 */
public final class SqlUserDAO extends SqlDAO implements UserDAO {

  private static final String FIND_ONE_NAME = "SELECT * FROM users WHERE username = ?;";

  /**
   * {@inheritDoc}
   */
  @Override
  public User find(String username) {
    User.validateUsername(username);
    return executePreparedStatement(FIND_ONE_NAME, SqlUserDAO::build, username);
  }

  private static final String INSERT_PERSON = """
                INSERT INTO users (username, password, registerDate, lastLogin, phoneNumber)
                VALUES (? COLLATE NOCASE, ?, ?, ?, ?);
            """;

  /**
   * {@inheritDoc}
   */
  @Override
  public User create(String username, String password, long phoneNumber) {
    User.validateUsername(username);
    User.validatePhoneNumber(phoneNumber);
    if (password == null || password.isBlank())
      throw new IllegalArgumentException("password is null or blank");
    Date date = new Date(System.currentTimeMillis());
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(INSERT_PERSON)) {
      String encryptedPassword = encrypt(password);
      statement.setString(1, username);
      statement.setString(2, encryptedPassword);
      statement.setDate(3, date);
      statement.setDate(4, date);
      statement.setLong(5, phoneNumber);
      statement.execute();
      try(ResultSet resultSet = statement.getGeneratedKeys()) {
        if(resultSet.next()) {
          long userId = resultSet.getInt(1);
          return new User(userId, username, encryptedPassword, date, date, phoneNumber);
        }
      }
    } catch (SQLException e) {
      if(e.getErrorCode() == 19) {
        throw new DAOException("Username is taken");
      }
      e.printStackTrace();
      throw new DAOException(e);
    }
    return null;
  }

  private static final String FIND_ONE_ID = "SELECT * FROM users WHERE userId = ?;";

  /**
   * {@inheritDoc}
   */
  @Override
  public User find(Long filter) {
    Objects.requireNonNull(filter);
    return executePreparedStatement(FIND_ONE_ID, SqlUserDAO::build, filter);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<User> find(Collection<Long> filter) {
    Objects.requireNonNull(filter);
    if(filter.isEmpty()) throw new IllegalArgumentException("filter cannot be empty");
    String findStatement = buildFindStatement(filter.size());
    return executePreparedStatementList(findStatement, SqlUserDAO::build, filter.toArray());
  }

  private String buildFindStatement(int size) {
    return "SELECT * FROM users WHERE userId IN (?" + ", ?".repeat(Math.max(0, size)) + ");";
  }

  private static final String FIND_ONE_BY_NAME = "SELECT * FROM users WHERE username = ? COLLATE NOCASE;";

  /**
   * {@inheritDoc}
   */
  @Override
  public User authenticate(String username, String password) throws AuthException {
    User.validateUsername(username);
    if(password == null || password.isBlank())
      throw new IllegalArgumentException("password is null or blank");
    try(Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(FIND_ONE_BY_NAME)) {
      statement.setString(1, username);
      try(ResultSet resultSet = statement.executeQuery()) {
        if(resultSet.next()) {
          User user = build(resultSet);
          verifyPassword(user, password);
          updateLastLogin(connection, user.getId());
          return user;
        }
      }
    } catch (SQLException e) {
      throw new DAOException(e);
    }
    throw new AuthException("User does not exist");
  }

  private static final String SET_NAME = "UPDATE users SET username = ? WHERE userId = ?;";

  /**
   * {@inheritDoc}
   */
  @Override
  public void setName(long userId, String name) {
    User.validateUsername(name);
    executePreparedStatement(SET_NAME, name, userId);
  }

  private static final String SET_PHONE_NUMBER = "UPDATE users SET phoneNumber = ? WHERE userId = ?;";

  /**
   * {@inheritDoc}
   */
  @Override
  public void setPhoneNumber(long userId, long phoneNumber) {
    User.validatePhoneNumber(phoneNumber);
    executePreparedStatement(SET_PHONE_NUMBER, phoneNumber, userId);
  }

  private static final String UPDATE_LAST_LOGIN = "UPDATE users SET lastLogin = ? WHERE userId = ?;";

  /**
   * Updates the last login date for a user with the specified ID.
   *
   * @param   userId the user ID
   */
  private void updateLastLogin(Connection connection, long userId) {
    try(PreparedStatement statement = connection.prepareStatement(UPDATE_LAST_LOGIN)) {
      statement.setLong(1, System.currentTimeMillis());
      statement.setLong(2, userId);
      statement.execute();
    } catch (SQLException e) {
      throw new DAOException(e);
    }
  }

  /**
   * Verifies the password of a user.
   *
   * @param   user the user to verify the password for
   * @param   password the password
   * @throws  AuthException if the password is invalid
   */
  private void verifyPassword(User user, String password) throws AuthException {
    String string = encrypt(password);
    if(!user.getPassword().equals(string))
      throw new AuthException("Invalid password");
  }

  /**
   * Encrypts a string with MD5.
   *
   * @param   password the string to encrypt
   * @return  the encrypted string
   */
  public static String encrypt(String password) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] passBytes = password.getBytes();
      md.reset();
      byte[] digested = md.digest(passBytes);
      StringBuilder sb = new StringBuilder();
      for (byte b : digested) {
        sb.append(Integer.toHexString(0xff & b));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return null;

  }

  /**
   * Creates a user object from a ResultSet.
   *
   * @param   resultSet the ResultSet to retrieve data from
   * @return  a new User object
   * @throws  SQLException if a database access error occurs
   */
  static User build(ResultSet resultSet) {
    try {
      return new User(resultSet.getLong("userId"),
              resultSet.getString("username"),
              resultSet.getString("password"),
              resultSet.getDate("registerDate"),
              resultSet.getDate("lastLogin"),
              resultSet.getInt("phoneNumber"));
    } catch (SQLException e) {
      throw new DAOException(e);
    }
  }

  private static final String CREATE_USERS = """
                CREATE TABLE IF NOT EXISTS users (
            	userId integer PRIMARY KEY AUTOINCREMENT,
            	username text(16) UNIQUE COLLATE NOCASE NOT NULL,
            	password text(60) NOT NULL,
            	registerDate integer NOT NULL,
            	lastLogin integer NOT NULL,
            	phoneNumber integer NOT NULL
            );""";

  /**
   * {@inheritDoc}
   */
  @Override
  public void init() {
    execute(CREATE_USERS);
  }
}