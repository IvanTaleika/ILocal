package ILocal.entity;

import ILocal.entity.UI.View;
import com.fasterxml.jackson.annotation.JsonView;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Entity
@Table(name = "project")
@JsonView(View.ProjectItem.class)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectName;
    private String description;
    private Timestamp creationDate;
    private Timestamp lastUpdate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id")
    private User author;

    @OneToMany(mappedBy = "projectId", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<ProjectContributor> contributors = new ArrayList<>();

    @OneToMany(mappedBy = "projectId", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<ProjectLang> projectLangs = new ArrayList<>();

    @OneToMany(mappedBy = "projectId", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Term> terms = new ArrayList<>();

    @Transient
    private long termsCount;
    @Transient
    private long searchedTermsCount;
    @Transient
    private long translationsCount;
    @Transient
    private long pagesCount;
    @Transient
    private double progress;
    @Transient
    private List<Term> searchedTerms = new ArrayList<>();

    public Project() {
    }

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

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate() {
        this.creationDate = new Timestamp(Calendar.getInstance().getTimeInMillis());
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate() {
        this.lastUpdate = new Timestamp(Calendar.getInstance().getTimeInMillis());
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

    public List<Term> getSearchedTerms() {
        return searchedTerms;
    }

    public void setSearchedTerms(List<Term> searchedTerms) {
        this.searchedTerms = searchedTerms;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public long getTranslationsCount() {
        return translationsCount;
    }

    public void setTranslationsCount(long translationsCount) {
        this.translationsCount = translationsCount;
    }

    public long getSearchedTermsCount() {
        return searchedTermsCount;
    }

    public void setSearchedTermsCount(long searchedTermsCount) {
        this.searchedTermsCount = searchedTermsCount;
    }
}
