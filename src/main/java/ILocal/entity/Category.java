package ILocal.entity;

public enum Category {
    PROJECT("Project"), TERM("Term"), TERMVALUE("Translation");

    String value;

    Category(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
