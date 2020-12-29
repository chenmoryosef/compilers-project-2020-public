class Main {
    public static void main(String[] a) { 
        System.out.println(3);
    }
}
    
class Simple{
    public int fun() {
        Banana b;
        b = new Apple();
        System.out.println(b.bar());
        return 0;
    }
}

class Banana{
    public int bar() {
        return 0;
    }
}

