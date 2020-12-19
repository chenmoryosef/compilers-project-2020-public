class Factorial {
    public static void main(String[] a) {
        System.out.println((new Fac()).ComputeFac(10));
    }
}

class Fac {
    int ComputeFac(int num) {
        int num_aux;
        num_aux = (num) + (new Fac());
        return num_aux;
    }

}

