class Main {
    public static void main(String[] args) {
        System.out.println(1);
    }
}

class A { }

class B extends A {
    int notAnArray;

    public int foo() {
        return notAnArray.length;
    }
}
