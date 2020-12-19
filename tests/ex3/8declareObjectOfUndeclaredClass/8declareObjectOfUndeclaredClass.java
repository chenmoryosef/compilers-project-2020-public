class BinaryTree {
    public static void main(String[] a) {
        System.out.println((new BT()).Start());
    }
}

class BT {
    int Start() {
        Tree root;
        Dog mika;
        boolean ntb;
        int nti;
        root = new Tree();
        ntb = (root).Init(16);
        ntb = (root).Print();
        System.out.println(100000000);
        ntb = (root).Insert(8);
        ntb = (root).Print();
        ntb = (root).Insert(24);
        ntb = (root).Insert(4);
        ntb = (root).Insert(12);
        ntb = (root).Insert(20);
        ntb = (root).Insert(28);
        ntb = (root).Insert(14);
        ntb = (root).Print();
        System.out.println((root).Search(24));
        System.out.println((root).Search(12));
        System.out.println((root).Search(16));
        System.out.println((root).Search(50));
        System.out.println((root).Search(12));
        ntb = (root).Delete(12);
        ntb = (root).Print();
        System.out.println((root).Search(12));
        return 0;
    }

}

class Tree {
    Tree left;

    Tree right;

    int key;

    boolean has_left;

    boolean has_right;

    Tree my_null;

    boolean Init(int v_key) {
        key = v_key;
        has_left = false;
        has_right = false;
        return true;
    }

    boolean SetRight(Tree rn) {
        right = rn;
        return true;
    }

    boolean SetLeft(Tree ln) {
        left = ln;
        return true;
    }

    Tree GetRight() {
        return right;
    }

    Tree GetLeft() {
        return left;
    }

    int GetKey() {
        return key;
    }

    boolean SetKey(int v_key) {
        key = v_key;
        return true;
    }

    boolean GetHas_Right() {
        return has_right;
    }

    boolean GetHas_Left() {
        return has_left;
    }

    boolean SetHas_Left(boolean val) {
        has_left = val;
        return true;
    }

    boolean SetHas_Right(boolean val) {
        has_right = val;
        return true;
    }

    boolean Compare(int num1, int num2) {
        boolean ntb;
        int nti;
        ntb = false;
        nti = (num2) + (1);
        if ((num1) < (num2))
            ntb = false;
        else
        if (!((num1) < (nti)))
            ntb = false;
        else
            ntb = true;
        return ntb;
    }

    boolean Insert(int v_key) {
        Tree new_node;
        boolean ntb;
        boolean cont;
        int key_aux;
        Tree current_node;
        new_node = new Tree();
        ntb = (new_node).Init(v_key);
        current_node = this;
        cont = true;
        while (cont) {			{
            key_aux = (current_node).GetKey();

            if ((v_key) < (key_aux))
            {
                if ((current_node).GetHas_Left())
                    current_node = (current_node).GetLeft();
                else
                {
                    cont = false;

                    ntb = (current_node).SetHas_Left(true);

                    ntb = (current_node).SetLeft(new_node);

                }

            }
            else
            {
                if ((current_node).GetHas_Right())
                    current_node = (current_node).GetRight();
                else
                {
                    cont = false;

                    ntb = (current_node).SetHas_Right(true);

                    ntb = (current_node).SetRight(new_node);

                }

            }

        }

        }
        return true;
    }

    boolean Delete(int v_key) {
        Tree current_node;
        Tree parent_node;
        boolean cont;
        boolean found;
        boolean is_root;
        int key_aux;
        boolean ntb;
        current_node = this;
        parent_node = this;
        cont = true;
        found = false;
        is_root = true;
        while (cont) {			{
            key_aux = (current_node).GetKey();

            if ((v_key) < (key_aux))
                if ((current_node).GetHas_Left())
                {
                    parent_node = current_node;

                    current_node = (current_node).GetLeft();

                }
                else
                    cont = false;
            else
            if ((key_aux) < (v_key))
                if ((current_node).GetHas_Right())
                {
                    parent_node = current_node;

                    current_node = (current_node).GetRight();

                }
                else
                    cont = false;
            else
            {
                if (is_root)
                    if ((!((current_node).GetHas_Right())) && (!((current_node).GetHas_Left())))
                        ntb = true;
                    else
                        ntb = (this).Remove(parent_node, current_node);
                else
                    ntb = (this).Remove(parent_node, current_node);

                found = true;

                cont = false;

            }

            is_root = false;

        }

        }
        return found;
    }

    boolean Remove(Tree p_node, Tree c_node) {
        boolean ntb;
        int auxkey1;
        int auxkey2;
        Tree t;
        if ((c_node).GetHas_Left())
            ntb = (this).RemoveLeft(p_node, c_node);
        else
        if ((c_node).GetHas_Right())
            ntb = (this).RemoveRight(p_node, c_node);
        else
        {
            auxkey1 = (c_node).GetKey();

            t = (p_node).GetLeft();

            auxkey2 = (t).GetKey();

            if ((this).Compare(auxkey1, auxkey2))
            {
                ntb = (p_node).SetLeft(my_null);

                ntb = (p_node).SetHas_Left(false);

            }
            else
            {
                ntb = (p_node).SetRight(my_null);

                ntb = (p_node).SetHas_Right(false);

            }

        }
        return true;
    }

    boolean RemoveRight(Tree p_node, Tree c_node) {
        boolean ntb;
        Tree t;
        while ((c_node).GetHas_Right()) {			{
            t = (c_node).GetRight();

            ntb = (c_node).SetKey((t).GetKey());

            p_node = c_node;

            c_node = (c_node).GetRight();

        }

        }
        ntb = (p_node).SetRight(my_null);
        ntb = (p_node).SetHas_Right(false);
        return true;
    }

    boolean RemoveLeft(Tree p_node, Tree c_node) {
        boolean ntb;
        Tree t;
        while ((c_node).GetHas_Left()) {			{
            t = (c_node).GetLeft();

            ntb = (c_node).SetKey((t).GetKey());

            p_node = c_node;

            c_node = (c_node).GetLeft();

        }

        }
        ntb = (p_node).SetLeft(my_null);
        ntb = (p_node).SetHas_Left(false);
        return true;
    }

    int Search(int v_key) {
        boolean cont;
        int ifound;
        Tree current_node;
        int key_aux;
        current_node = this;
        cont = true;
        ifound = 0;
        while (cont) {			{
            key_aux = (current_node).GetKey();

            if ((v_key) < (key_aux))
                if ((current_node).GetHas_Left())
                    current_node = (current_node).GetLeft();
                else
                    cont = false;
            else
            if ((key_aux) < (v_key))
                if ((current_node).GetHas_Right())
                    current_node = (current_node).GetRight();
                else
                    cont = false;
            else
            {
                ifound = 1;

                cont = false;

            }

        }

        }
        return ifound;
    }

    boolean Print() {
        Tree current_node;
        boolean ntb;
        current_node = this;
        ntb = (this).RecPrint(current_node);
        return true;
    }

    boolean RecPrint(Tree node) {
        boolean ntb;
        if ((node).GetHas_Left())
        {
            ntb = (this).RecPrint((node).GetLeft());

        }
        else
            ntb = true;
        System.out.println((node).GetKey());
        if ((node).GetHas_Right())
        {
            ntb = (this).RecPrint((node).GetRight());

        }
        else
            ntb = true;
        return true;
    }

}

