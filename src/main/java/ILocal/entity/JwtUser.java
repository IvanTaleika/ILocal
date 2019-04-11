package ILocal.entity;

public class JwtUser {

  private String userName;
  private long id;
  private String role;

  public JwtUser(long id, String userName) {
    this.id = id;
    this.userName = userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public void setId(long id) {
    this.id = id;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getUserName() {
    return userName;
  }

  public long getId() {
    return id;
  }

  public String getRole() {
    return role;
  }
}
