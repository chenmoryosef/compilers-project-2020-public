class Main {
    public static void main(String[] args) {
        System.out.println(3);
    }
}

class Example {
    int x;

    public int run() {
        x = 0;
        return x + x;
    }

    public int other() {
        int x;
        x = 1;
        return x - 1;
    }
}

class B extends Example{
    public void test() {
        x = 4;
        return x+x;
    }
}

class C extends Example{
    public void C_Test() {
        x = 7;
        return x+x;
    }
}

class D extends Example{
    public void D_Test() {
        x = 23;
        return x+x;
    }
}

class E {
    int x;
    public void E_Test() {
        x = 7;
        return x+x;
    }
}
