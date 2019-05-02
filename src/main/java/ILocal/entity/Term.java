package ILocal.entity;

import com.fasterxml.jackson.annotation.JsonView;
import ILocal.entity.UI.View;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "term")
@JsonView(View.ProjectItem.class)
public class Term {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;

    private String termValue;

    @Transient
    private List<TermLang> translations = new ArrayList<>();

    @Transient
    private List<TermComment> comments = new ArrayList<>();

    @Transient
    private boolean selected = false;

    @Transient
    private long commentsCount;

    public Term(){}

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

    public String getTermValue() {
        return termValue;
    }

    public void setTermValue(String termValue) {
        this.termValue = termValue;
    }

    public List<TermLang> getTranslations() {
        return translations;
    }

    public void setTranslations(List<TermLang> translations) {
        this.translations = translations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Term term = (Term) o;
        return Objects.equals(id, term.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public List<TermComment> getComments() {
        return comments;
    }

    public void setComments(List<TermComment> comments) {
        this.comments = comments;
    }

    public long getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(long commentsCount) {
        this.commentsCount = commentsCount;
    }
}
