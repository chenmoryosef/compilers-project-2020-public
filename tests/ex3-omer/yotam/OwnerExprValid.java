class Main {
    public static void main(String[] a) { 
        System.out.println(3);
    }
}
    
    class Tree {
        Tree left;
        Tree right;

        public Tree getLeft() {
            return left;
        }

        public Tree getRight() {
            return right;
        }

        public int num() {
            return 2;
        }

        public int fun() {
            Tree t;
            t = new Tree().getLeft();
        	return t.fun();
    	}
}

