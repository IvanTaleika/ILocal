package ILocal.entity;

import java.util.Collection;
import java.util.List;
import javax.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "user")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String email;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String company;
    private String profilePhoto;
    private String activationCode;
    private boolean mailingAccess;

  @Transient
  private String token;
  @Transient
  private Collection<? extends GrantedAuthority> authorities;

  public User() {
  }

  public User(String userName, long id, String token, List<GrantedAuthority> grantedAuthorities) {
    this.username = userName;
    this.id = id;
    this.token = token;
    this.authorities = grantedAuthorities;
  }


    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

    public void setUsername(String username) {
        this.username = username;
    }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public boolean isMailingAccess() {
        return mailingAccess;
    }

    public void setMailingAccess(boolean mailingAccess) {
        this.mailingAccess = mailingAccess;
    }
}
