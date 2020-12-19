ERROR
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
    int theThing() {
        return theThing;
    }

}

class C extends A {
    int theThing() {
        return (theThing).theThing();
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

