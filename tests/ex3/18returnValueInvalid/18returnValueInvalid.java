class Main {
    public static void main(String[] a) {
        System.out.println(3);
    }
}

class Tree {
    Tree left;

    Tree right;

    Tree getLeft() {
        return left;
    }

    Tree getRight() {
        return new B();
    }

}

class A extends Tree {
}

class B {
}

