class Main {
    public static void main(String[] args) {
        System.out.println(new Example().run());
    }
}

class Example {
    int x;

    public int run() {
        x = 0;
        return x + x;
    }

    public int other(int y) {
        int x;
        x = 1;
        return x - 1;
    }

    public Example classRet(Example m, int k) {
        int x;
        x = 1;
        return m;
    }
}

class B extends Example{

    public Example classRet(Example m, int k) {
        int x;
        B s;
        Example k;
        x = 1;
        s = new B();
        k = s.classRet(s,x);
        return s;
    }

    public void test() {
        Example k;
        int h;
        h=4;
        k = new Example();
        k = classRet(k, h)
        x = 4;
        return x+x;
    }

    public int run() {
        x = 2;
        return x + x;
    }

    public int other(int ss) {
        int ss;
        ss = 1;
        return ss - 1;
    }
}

class Main extends B{

    public int run() {
        x = 2;
        return x + x;
    }

    public int other(int m) {
        int ss;
        ss = 1;
        return ss - 1;
    }

    public void C_Test() {
        x = 7;
        return x+x;
    }
}

class D extends C{
    public void D_Test() {
        x = 23;
        return x+x;
    }
}

class E {
    int x;
    public void other() {
        x = 7;
        return x+x;
    }
}
