class Main {
    public static void main(String[] args) {
        System.out.println(new Example().run());
    }
}

class Example {
    public int run() {
        int x;
        x = 0;
        return x + x;
    }
}

class A extends Example {
    public Example run() {
        Example s;
        s = new Example();
        return s;
    }
}