class Main {
    public static void main(String[] args) {
        System.out.println(1);
    }
}

class Shared {
    int theThing;

    Shared theThing() {
        return new Shared();
    }

}

class A extends Shared {
}

class B extends A {
    A theThing() {
        return new A();
    }

}

