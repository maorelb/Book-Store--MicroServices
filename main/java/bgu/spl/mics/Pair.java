package bgu.spl.mics;

public class Pair<T, E extends Comparable> implements Comparable<Pair> {
    private T left;
    private E right;

    public Pair(T left,E right){

        this.left=left;
        this.right=right;
    }

    public E getRight() {
        return right;
    }

    public T getLeft(){
        return left;
    }


    @Override
    public int compareTo(Pair o) {
        return right.compareTo(o.right);
    }

    public String toString(){
        return "("+left+","+right+")";
    }
}
