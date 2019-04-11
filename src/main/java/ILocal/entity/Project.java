package ILocal.entity;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name="project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String projectName;
    private String description;
    private Date creationDate;
    private Date lastUpdate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="author_id")
    private User author;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<ProjectContributor> contributors =  new ArrayList<>();

    @OneToMany(mappedBy = "projectId", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<ProjectLang> projectLangs =  new ArrayList<>();

    @OneToMany(mappedBy = "projectId", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Term> terms =  new ArrayList<>();

  @Transient
  private long termsCount;
  @Transient
  private long pagesCount;

    public Project(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public List<ProjectContributor> getContributors() {
        return contributors;
    }

    public void setContributors(List<ProjectContributor> contributors) {
        this.contributors = contributors;
    }

    public List<ProjectLang> getProjectLangs() {
        return projectLangs;
    }

    public void setProjectLangs(List<ProjectLang> projectLangs) {
        this.projectLangs = projectLangs;
    }

    public List<Term> getTerms() {
        return terms;
    }

    public void setTerms(List<Term> terms) {
        this.terms = terms;
    }

  public long getTermsCount() {
    return termsCount;
  }

  public void setTermsCount(long termsCount) {
    this.termsCount = termsCount;
  }

  public long getPagesCount() {
    return pagesCount;
  }

  public void setPagesCount(long pagesCount) {
    this.pagesCount = pagesCount;
  }
}
