/*
 * Created by Elliott Park, 2016
 *
 * This program is in the public domain.
 * This program is adapted from Steve Hanov's blog: http://stevehanov.ca/blog/index.php?id=114
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * A trie data sctructure that incrementally calculates the Damerau-Levenstein
 * distance between a stream of input characters and words saved in the trie.
 * After each letter is entered into the AutocompleteTrie, it returns all of
 * the words that could both complete and be within a maximum distance
 * threshold of what has been entered so far.
 */
public class AutocompleteTrie {

    public static int SORT_PRIORITY = 0;
    public static int SORT_DISTANCE = 1;

    class Tuple<T,U> {
        public T first;
        public U second;

        public Tuple(T first, U second) {
            this.first = first;
            this.second = second;
        }
    }

    class Triple<T,U,V> {
        public T first;
        public U second;
        public V third;

        public Triple(T first, U second, V third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }
    }

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

    Trie root = new Trie();
    private List<Triple<Trie, List<Integer>, Integer>> activeTries;
    List<Character> currentWord = new ArrayList<Character>();

    public AutocompleteTrie() {
        this.activeTries = new ArrayList<Triple<Trie, List<Integer>, Integer>>();
    }

    public void insert(char[] word) {
        root.insert(word);
    }

    public void init() {
        activeTries.clear();
        currentWord.clear();
        Set<Map.Entry<Character, Trie>> set = root.children.entrySet();
        List<Integer> currentRow = new ArrayList<Integer>();
        currentRow.add(0);
        activeTries.add(new Triple<Trie, List<Integer>, Integer>(root, currentRow, 0));
    }

    public List<Triple<char[], Integer, Integer>> broadcastInput(char c, int maxCost, final int sortType) {
        currentWord.add(c);

        List<Triple<Trie, List<Integer>, Integer>> activeTriesCopy = new ArrayList<Triple<Trie, List<Integer>, Integer>>();
        activeTriesCopy.addAll(activeTries);
        activeTries.clear();
        for (Triple<Trie, List<Integer>, Integer> activeTrie : activeTriesCopy) {
            Set<Map.Entry<Character, Trie>> set = activeTrie.first.children.entrySet();
            for (Map.Entry<Character, Trie> child : set) {
                activeTrie.second.add(activeTrie.second.size());
                signal(child.getValue(), child.getKey(), activeTrie.second, currentWord, maxCost);
            }
        }

        List<Triple<char[], Integer, Integer>> resultsList = new ArrayList<Triple<char[], Integer, Integer>>();
        for (Triple<Trie, List<Integer>, Integer> triple : activeTries) {
            List<char[]> words = triple.first.getChildWords();
            for (char[] word : words) {
                resultsList.add(new Triple<char[], Integer, Integer>(word, triple.third, triple.first.priority));
            }
        }

        Collections.sort(resultsList, new Comparator<Triple<char[], Integer, Integer>>() {
            @Override
            public int compare(Triple<char[], Integer, Integer> o1, Triple<char[], Integer, Integer> o2) {
                if (SORT_PRIORITY == sortType) {
                    if (o1.third != o2.third) {
                        return o1.third - o2.third;
                    } else {
                        return o1.second - o2.second;
                    }
                } else {
                    if (o1.second != o2.second) {
                        return o1.second - o2.second;
                    } else {
                        return o1.third - o2.third;
                    }
                }
            }
        });
        return resultsList;
    }

    public void signal(Trie trie, char letter, List<Integer> previousRow, List<Character> word, int maxCost) {
        int columns = word.size()+1;
        List<Integer> currentRow = new ArrayList<Integer>();
        currentRow.add(previousRow.get(0)+1);

        for (int column = 1; column < columns; column++) {
            int insertCost = currentRow.get(column-1)+1;
            int deleteCost = previousRow.get(column)+1;
            int replaceCost;
            if (word.get(column-1) != letter) {
                replaceCost = previousRow.get(column-1)+1;
            } else {
                replaceCost = previousRow.get(column-1);
            }
            currentRow.add(Math.min(insertCost, Math.min(deleteCost, replaceCost)));
        }
        int size = currentRow.size();
        int cost = currentRow.get(size-1);
        if (cost <= maxCost) {
            activeTries.add(new Triple<Trie, List<Integer>, Integer>(trie, currentRow, cost));
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

    public static void main(String[] args) {
        AutocompleteTrie autocompleteTrie = new AutocompleteTrie();
        String[] words = {"hello", "halo", "hallelujah", "haberdasher", "world", "worlds", "would",
                "cricket", "cracker", "", "blast", "burst", "cat", "two", "to", "too", "two",
                "tooth", "tall", "fat", "far", "woo", "won", "winner", "wonderer", "wanderer"};
        for (String s : words) {
            autocompleteTrie.insert(s.toCharArray());
        }
        autocompleteTrie.init();
        char[] word = "hello".toCharArray();
        int epoch = 0;
        for (char c : word) {
            System.out.println("\n\tepoch: "+epoch);
            List<Triple<char[], Integer, Integer>> results = autocompleteTrie.broadcastInput(c, 1, SORT_DISTANCE);
            System.out.println(c + ": " + results.size());
            for (Triple<char[], Integer, Integer> triple : results) {
                System.out.println(new String(triple.first) + ", " + triple.second);
            }
            epoch++;
        }

        System.out.println("\nProgram Finished");
    }
}
