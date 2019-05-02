package ILocal.entity;

import com.fasterxml.jackson.annotation.JsonView;
import ILocal.entity.UI.View;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project_lang")
@JsonView(View.ProjectItem.class)
public class ProjectLang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long projectId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lang_id")
    private Lang lang;

    @OneToMany(mappedBy = "projectLangId",  fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<TermLang> termLangs = new ArrayList<>();

    private boolean isDefault;

    @Transient
    private String projectName;
    @Transient
    private long termsCount;
    @Transient
    private long translatedCount;
    @Transient
    private long pagesCount;

    public ProjectLang() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Lang getLang() {
        return lang;
    }

    public void setLang(Lang lang) {
        this.lang = lang;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public List<TermLang> getTermLangs() {
        return termLangs;
    }

    public void setTermLangs(List<TermLang> termLangs) {
        this.termLangs = termLangs;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public long getTermsCount() {
        return termsCount;
    }

    public void setTermsCount(long termsCount) {
        this.termsCount = termsCount;
    }

    public long getTranslatedCount() {
        return translatedCount;
    }

    public void setTranslatedCount(long translatedCount) {
        this.translatedCount = translatedCount;
    }

    public long getPagesCount() {
        return pagesCount;
    }

    public void setPagesCount(long pagesCount) {
        this.pagesCount = pagesCount;
    }
}
