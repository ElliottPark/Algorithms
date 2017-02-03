/*
    Author: Elliott Park
    This code is adapted from Steve Hanov's http://stevehanov.ca/blog/index.php?id=114
    This code is in the public domain.
*/
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CharLevTrie {

    class Trie {
        char[] word;
        Map<Character, Trie> children;
        int priority;
        public Trie() {
            this.word = null;
            this.children = new HashMap<Character, Trie>();
            this.priority = Integer.MAX_VALUE;
        }

        public void insert(char[] word) {
            Trie node = this;
            for (char letter : word) {
                if (!node.children.containsKey(letter)) {
                    node.children.put(letter, new Trie());
                }
                node = node.children.get(letter);
            }
            node.word = word;
        }

        public List<char[]> getChildWords() {
            List<char[]> words = new ArrayList<char[]>();
            getChildWordsRecursive(words);
            return words;
        }

        public void getChildWordsRecursive(List<char[]> words) {
            if (word != null) {
                words.add(word);
            } else {
                Set<Map.Entry<Character, Trie>> set = children.entrySet();
                for (Map.Entry<Character, Trie> e : set) {
                    e.getValue().getChildWordsRecursive(words);
                }
            }
        }

    }

    class Tuple<U,V> {
        public U first;
        public V second;
        public Tuple(U first, V second) {
            this.first = first;
            this.second = second;
        }
    }

    Trie root = new Trie();

    Comparator<Tuple<char[], Integer>> resultsComparator = new Comparator<Tuple<char[], Integer>>() {
        @Override
        public int compare(Tuple<char[], Integer> lhs, Tuple<char[], Integer> rhs) {
            return lhs.second - lhs.second;
        }
    };

    public CharLevTrie() {
    }

    public void insert(char[] word) {
        root.insert(word);
    }

    public List<Tuple<char[], Integer>> search(char[] word, int maxCost) {
        List<Integer> currentRow = new ArrayList<Integer>();
        for (int i = 0; i <= word.length; i++) {
            currentRow.add(i);
        }
        List<Tuple<char[], Integer>> results = new ArrayList<Tuple<char[], Integer>>();

        Set<Map.Entry<Character, Trie>> set = root.children.entrySet();
        for (Map.Entry<Character, Trie> e : set) {
            searchRecursive(e.getValue(), e.getKey(), word, currentRow, results, maxCost);
        }

        Collections.sort(results, resultsComparator);
        return results;
    }

    public void searchRecursive(Trie node, char letter, char[] word, List<Integer> previousRow,
                                List<Tuple<char[], Integer>> results, int maxCost) {

        int columns = word.length+1;
        List<Integer> currentRow = new ArrayList<Integer>();
        currentRow.add(previousRow.get(0)+1);


        for (int column = 1; column < columns; column++) {
            int insertCost = currentRow.get(column-1)+1;
            int deleteCost = previousRow.get(column)+1;
            int replaceCost;
            if (word[column-1] != letter) {
                replaceCost = previousRow.get(column-1)+1;
            } else {
                replaceCost = previousRow.get(column-1);
            }
            currentRow.add(Math.min(insertCost, Math.min(deleteCost, replaceCost)));
        }
        int size = currentRow.size();
        int cost = currentRow.get(size-1);
        if (cost <= maxCost && node.word != null) {
            results.add(new Tuple<char[], Integer>(node.word, cost));
        }

        if (min(currentRow) <= maxCost) {
            Set<Map.Entry<Character, Trie>> set = node.children.entrySet();
            for (Map.Entry<Character, Trie> e : set) {
                searchRecursive(e.getValue(), e.getKey(), word, currentRow, results, maxCost);
            }
        }
    }

    public int min(List<Integer> list) {
        int min = Integer.MAX_VALUE;
        for (Integer i : list) {
            if (i < min) {
                min = i;
            }
        }
        return min;
    }


    public static int editDistance(char[] word1, char[] word2) {
        int len1 = word1.length;
        int len2 = word2.length;

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        char c1;
        char c2;
        //iterate though, and check last char
        for (int i = 0; i < len1; i++) {
            c1 = word1[i];
            for (int j = 0; j < len2; j++) {
                c2 = word2[j];
                //if last two chars equal
                if (c1 == c2) {
                    dp[i + 1][j + 1] = dp[i][j];
                } else {
                    int replace = dp[i][j] + 1;
                    int insert = dp[i][j + 1] + 1;
                    int delete = dp[i + 1][j] + 1;
                    int min = replace > insert ? insert : replace;
                    min = delete > min ? min : delete;
                    dp[i + 1][j + 1] = min;
                }
            }
        }
        return dp[len1][len2];
    }

    public static void testOne(CharLevTrie charLevTrie, char[] word) {

        System.out.println("Testing: " + new String(word));
        List<Tuple<char[], Integer>> list = charLevTrie.search(word, 2);
        int length = list.size();
        for (int i = 0; i < length; i++) {
            System.out.println('\t' + new String(list.get(i).first) + ", " + list.get(i).second);
        }
    }

    public static void main(String[] args) {
        CharLevTrie levTrie = new CharLevTrie();
        String[] words = {"hell", "hello", "halo", "hallelujah", "haberdasher", "world", "worlds", "would", "to",
                "cricket", "blast", "damn", "cat", "two",
                "tooth", "too", "tall", "fat", "woo", "car"};

        for (String s : words) {
            levTrie.insert(s.toCharArray());
        }

        testOne(levTrie, "hel".toCharArray());
        testOne(levTrie, "hellus".toCharArray());
        testOne(levTrie, "rata".toCharArray());
        testOne(levTrie, "vacat".toCharArray());

        System.out.println("Program Finished");
    }
}
