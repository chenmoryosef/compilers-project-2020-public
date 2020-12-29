ERROR
class Main {
    public static void main(String[] args) {
        System.out.println(1);
    }
}

class A {
}

class B extends A {
    int theMethod() {
        return 1;
    }

    int theMethod() {
        return (this).theMethod();
    }

}

class C extends A {
    int theMethod() {
        return 1;
    }

}

class D extends C {
    int anotherMethod() {
        return (this).theMethod();
    }

}

