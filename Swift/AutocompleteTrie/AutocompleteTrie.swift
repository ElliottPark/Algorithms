//
//  AutocompleteTrie.swift
//  AutocompleteTrie
//
//  Created by Elliott Park on 5/14/16.
//  This program is in the public domain.
//  It is based on Steve Hanov's blog post: http://stevehanov.ca/blog/index.php?id=114
//  The original code has been rewritten from Python to Swift (my very first Swift program)
//  and modified to accept characters one at a time instead of all at once, making it
//  ideal for use as an approximate autocompleter
//

import Foundation

class AutocompleteTrie {
    
    static var SORT_PRIORITY = 0
    static var SORT_DISTANCE = 1
    
    class Trie {
        var word: [Character]
        var children: [Character: Trie]
        var priority: Int
        init() {
            self.word = []
            self.children = [:]
            self.priority = Int.max
        }
        
        func insert(word: [Character]) {
            
            var node:Trie = self;
            for c in word {
                if node.children[c] == nil {
                    node.children[c] = Trie()
                }
                node = node.children[c]!
            }
            node.word = word
        }
        
        func getChildWords() -> Array<[Character]> {
            var words: Array<[Character]> = Array<[Character]>()
            getChildWordsRecursive(&words)
            return words
        }
        
        func getChildWordsRecursive(inout words: Array<[Character]>) {
            if !word.isEmpty {
                words.append(word)
            } else {
                for trie in children.values {
                    trie.getChildWordsRecursive(&words)
                }
            }
        }
    }
    
    var root: Trie
    var activeTries: Array<(Trie, [Int], Int)>
    var currentWord: Array<Character>
    
    init() {
        root = Trie()
        activeTries = Array<(Trie, [Int], Int)>()
        currentWord = Array<Character>()
    }
    
    func insert(word:[Character]) {
        root.insert(word)
    }
    
    func initialize() {
        activeTries.removeAll()
        currentWord.removeAll()
        activeTries.append((root, [0], 0))
    }
    
    func broadcaseInput(c: Character, maxCost: Int, final sortType: Int) -> Array<([Character], Int, Int)> {
        var resultsList = Array<([Character], Int, Int)>()
        currentWord.append(c)
        
        var activeCopies = Array<(Trie, [Int], Int)>()
        activeCopies.appendContentsOf(activeTries)
        activeTries.removeAll()
        for activeTrie in activeCopies {
            for (char, trie) in activeTrie.0.children {
                var previousRow = activeTrie.1
                previousRow.append(activeTrie.1.count)
                signal(trie, letter: char, previousRow: previousRow, word: currentWord, maxCost: maxCost)
            }
        }
        
        for (trie, _, cost) in activeTries {
            for word in trie.getChildWords() {
                resultsList.append((word, cost, trie.priority))
            }
        }
        
        resultsList.sortInPlace({
            if AutocompleteTrie.SORT_PRIORITY == sortType {
                if $0.2 != $1.2 {
                    return $0.2 > $1.2
                } else {
                    return $0.1 > $1.1
                }
            } else {
                if $0.1 != $1.1 {
                    return $0.1 > $1.1
                } else {
                    return $0.2 > $1.2
                }
            }
        })
        
        
        return resultsList
    }
    
    func signal(trie: Trie, letter: Character, previousRow: [Int], word: [Character], maxCost: Int) {
        let columns: Int = word.count + 1
        
        var currentRow = Array<Int>()
        currentRow.append(previousRow[0]+1)
        
        for column in 1...(columns-1) {
            
            let insertCost = currentRow[column-1]+1
            let deleteCost = previousRow[column]+1
            var replaceCost: Int
            if word[column-1] != letter {
                replaceCost = previousRow[column-1]+1
            } else {
                replaceCost = previousRow[column-1]
            }
            currentRow.append(min(insertCost, min(deleteCost, replaceCost)))
        }
        let size = currentRow.count
        let cost = currentRow[size-1]
        if cost <= maxCost {
            activeTries.append((trie, currentRow, cost))
        }
    }
}









