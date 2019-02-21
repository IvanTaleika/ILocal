package ILocal.entity;

import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String project_name;
    private String description;
    private Date creation_date;
    private Date last_update;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="author_id")
    private User author;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<ProjectContributor> contributors =  new ArrayList<>();

    @OneToMany(mappedBy = "projectLangId", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<ProjectLang> projectLangs =  new ArrayList<>();

    @OneToMany(mappedBy = "projectId", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Term> terms =  new ArrayList<>();


    public Project(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(Date creation_date) {
        this.creation_date = creation_date;
    }

    public Date getLast_update() {
        return last_update;
    }

    public void setLast_update(Date last_update) {
        this.last_update = last_update;
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
}
