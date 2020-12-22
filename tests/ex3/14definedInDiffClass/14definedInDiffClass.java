class Main {
    public static void main(String[] args) {
        System.out.println(1);
    }
}

class A {
}

class B extends A {
    int bVar;

    int foo() {
        return theVar;
    }

}

class C extends A {
    int theVar;

    int foo() {
        return bVar;
    }

}

class D extends C {
    int bar(int anotherVar) {
        int lvTheVar;
        int rvTheVar;
        if ((anotherVar) < (1))
            rvTheVar = anotherVar;
        else
            lvTheVar = anotherVar;
        lvTheVar = rvTheVar;
        return anotherVar;
    }

}

