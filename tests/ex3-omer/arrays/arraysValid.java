class Main {
    public static void main(String[] args) {
        System.out.println(1);
    }
}

class A { }

class B extends A {
    int[] theVar;

    public int foo() {
        return theVar.length;
    }
}

class C extends A {
    int[] theVar;

    public int foo() {
        theVar = new int[3];
        theVar[0] = 5;
        theVar[1+0] = 2+2;
        return theVar[0];
    }
}

class D extends C {
    public int bar(int anotherVar) {
        System.out.println(theVar[theVar.length] - anotherVar);
        return theVar.length - anotherVar;
    }

}
