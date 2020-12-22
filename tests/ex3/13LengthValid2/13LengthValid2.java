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

}

class C extends B {
    int anotherMethod(B b, int i) {
        int[] max;
	int len;
        len = new int[((this).theMethod()) * ((b).theMethod())].lenght;
        return len;
    }

}

