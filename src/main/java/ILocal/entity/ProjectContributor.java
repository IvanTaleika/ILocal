package ILocal.entity;



import javax.persistence.*;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "project_contributor")
public class ProjectContributor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User contributor;
    private Long project;

    @Transient
    private String projectName;

    @Enumerated(EnumType.STRING)
    private ContributorRole role;

    public ProjectContributor(){}

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getContributor() {
        return contributor;
    }

    public void setContributor(User contributor) {
        this.contributor = contributor;
    }

    public ContributorRole getRole() {
        return role;
    }

    public void setRole(ContributorRole role) {
        this.role = role;
    }

    public long getProject() {
        return project;
    }

    public void setProject(Long project) {
        this.project = project;
    }
}
