package ILocal.entity;

public class SearchItem {
    private String project;
    private String term;
    private String lang;
    private String title;
    private String link;
    private String searchedValue;
    private String description;
    private String category;

    public SearchItem(String value, String category) {
        this.category = category;
        this.title = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getProject() {
        return project;
    }

    public String getSearchedValue() {
        return searchedValue;
    }

    public void setSearchedValue(String searchedValue) {
        this.searchedValue = searchedValue;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
