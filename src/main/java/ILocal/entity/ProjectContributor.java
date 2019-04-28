package ILocal.entity;


import javax.persistence.*;

@Entity
@Table(name = "project_contributor")
public class ProjectContributor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User contributor;
    private Long projectId;

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

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
