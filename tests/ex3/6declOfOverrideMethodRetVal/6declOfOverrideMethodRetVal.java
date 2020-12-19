class Main {
    public static void main(String[] args) {
        System.out.println(1);
    }
}

class Shared {
    int theThing;

    int theThing() {
        return theThing;
    }

}

class A extends Shared {
}

class B extends A {
    boolean theThing() {
        return false;
    }

}

class C extends A {
    int theThing() {
        return theThing;
    }

}

class D extends A {
    int theThing() {
        int theThing;
        return theThing;
    }

}

class E extends A {
    int theThing(int theThing) {
        return theThing;
    }

}

