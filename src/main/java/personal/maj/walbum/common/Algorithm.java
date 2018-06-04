package personal.maj.walbum.common;

/**
 * Created by MAJ on 2018/5/29.
 */
public class Algorithm {

    private static Algorithm algorithm = new Algorithm();

    private Algorithm() {
    }

    public static Algorithm get() {return  algorithm;}

    public int fnvHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++)
            hash = (hash ^ str.charAt(i)) * p;
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        return hash;
    }
}
