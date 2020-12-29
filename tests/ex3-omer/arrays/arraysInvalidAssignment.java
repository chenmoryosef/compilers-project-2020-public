class Main {
    public static void main(String[] args) {
        System.out.println(1);
    }
}

class A { }

class B extends A {
    int[] arr;

    public int foo() {
        arr[0] = new A();
        return arr.length;
    }
}
