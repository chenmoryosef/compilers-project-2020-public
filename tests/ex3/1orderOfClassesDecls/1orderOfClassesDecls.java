ERROR
class Main {
	public static void main(String[] args) {
		System.out.println(1);
	}
}

class B extends A {
	int theMethod() {
		return 1;
	}

}

class A {
}

class C extends A {
	int renamedMethod() {
		return 1;
	}

}

class D extends C {
	int anotherMethod(B b) {
		System.out.println(((this).renamedMethod()) - ((b).theMethod()));
		return ((this).renamedMethod()) - ((b).theMethod());
	}

}

