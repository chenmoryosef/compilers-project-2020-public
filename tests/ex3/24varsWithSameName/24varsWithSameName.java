ERROR
class Main {
    public static void main(String[] args) {
        System.out.println(1);
    }
}

class Shared {
    int renamedThing;

    int theThing() {
        return renamedThing;
    }

}

class A extends Shared {
}

class B extends A {
    int theThing() {
        return renamedThing;
    }

}

class C extends A {
    int theThing() {
        return renamedThing;
    }

}

class D extends A {
    int theThing() {
        int theThing;
        boolean theThing;
        return theThing;
    }

}

class E extends A {
    int theThing(int theThing) {
        return theThing;
    }

}

