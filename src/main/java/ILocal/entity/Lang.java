package ILocal.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "lang")
public class Lang {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String langName;
    private String langDef;
    private String langIcon;

    public Lang(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLangName() {
        return langName;
    }

    public void setLangName(String langName) {
        this.langName = langName;
    }

    public String getLangDef() {
        return langDef;
    }

    public void setLangDef(String langDef) {
        this.langDef = langDef;
    }

    public String getLangIcon() {
        return langIcon;
    }

    public void setLangIcon(String langIcon) {
        this.langIcon = langIcon;
    }
}
