package ILocal.entity;

import ILocal.entity.UI.View;
import com.fasterxml.jackson.annotation.JsonView;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "user")
public class User implements UserDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonView(View.ProjectItem.class)
	private Long id;
	private String email;

	@JsonView(View.ProjectItem.class)
	private String username;

	private String password;

	@JsonView(View.ProjectItem.class)
	private String firstName;

	@JsonView(View.ProjectItem.class)
	private String lastName;

    @JsonView(View.ProjectItem.class)
	private String profilePhoto;

	private String refreshToken;
	private String activationCode;
	private boolean mailingAccess;

    @OneToMany(mappedBy = "userId", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Contact> contacts = new ArrayList<>();

    @OneToMany(mappedBy = "userId", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<UserLang> langs = new ArrayList<>();

    @OneToMany(mappedBy = "userId", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<JobExperience> jobs = new ArrayList<>();

    @Transient
    private String token;
    @Transient
    private Collection<? extends GrantedAuthority> authorities;
    @Transient
    private String repeatPassword;
    @Transient
    private String oldPassword;
    @Transient
    private ResultStat resultStat;
    @Transient
    private String avatar;

    public User(){}

    public User(String userName, long id, String token, List<GrantedAuthority> grantedAuthorities) {
        this.username = userName;
        this.id = id;
        this.token = token;
        this.authorities = grantedAuthorities;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
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

    public String getRepeatPassword() {
        return repeatPassword;
    }

    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public List<UserLang> getLangs() {
        return langs;
    }

    public void setLangs(List<UserLang> langs) {
        this.langs = langs;
    }

    public List<JobExperience> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobExperience> jobs) {
        this.jobs = jobs;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public ResultStat getResultStat() {
        return resultStat;
    }

    public void setResultStat(ResultStat resultStat) {
        this.resultStat = resultStat;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
}
