class Main {
    public static void main(String[] args) {
        System.out.println(1);
    }
}

class A {
    int what;

}

class B extends A {
    int foo() {
        return 0;
    }

}

class C {
    int what;

    int poo() {
        A a;
        B b;
        a = new A();
        b = new B();
        a = b;
        return 1;
    }

}

