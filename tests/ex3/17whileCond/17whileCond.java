class Main {
    public static void main(String[] args) {
        System.out.println(1);
    }
}

class A {
}

class B extends A {
    int theVar;

    int foo() {
        return theVar;
    }

}

class C extends A {
    int theVar;

    int foo() {
        return theVar;
    }

}

class D extends C {
    int bar(int theVar, int anotherVar) {
        while (theVar) {
            System.out.println(1);
        }
        return 1;
    }

}

