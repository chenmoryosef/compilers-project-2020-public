package symbolTable;

public enum Type {
    METHOD("Method"),
    VARIABLE("Variable");

    private String name;

    Type(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
