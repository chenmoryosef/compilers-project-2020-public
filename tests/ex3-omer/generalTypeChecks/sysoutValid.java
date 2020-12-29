class Main {
    public static void main(String[] a) { 
        System.out.println(3);
    }
}
    
class Simple{
    public int num() {
        return 1;
    }

    public int fun() {
        System.out.println((new Simple()).num());
        return 0;
    }
}

