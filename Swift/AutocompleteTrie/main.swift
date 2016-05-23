//
//  main.swift
//  LevTrie
//
//  Created by Elliott Park on 5/14/16.
//  This program is in the public domain.
//  It is based on Steve Hanov's blog post: http://stevehanov.ca/blog/index.php?id=114
//

import Foundation

var autocompleteTrie = AutocompleteTrie()
var words = ["hello", "halo", "hallelujah", "haberdasher", "world", "worlds", "would",
             "cricket", "cracker", "", "blast", "burst", "cat", "two", "to", "too", "two",
             "tooth", "tall", "fat", "far", "woo", "won", "winner", "wonderer", "wanderer"]

for word in words {
    autocompleteTrie.insert(Array(word.characters))
}

autocompleteTrie.initialize()

var word: [Character] = Array("helo".characters)
var count = 0
for char in word {
    print("\n\tEpoch: ", count)
    var results: Array<([Character], Int, Int)> = autocompleteTrie.broadcaseInput(char, maxCost: 1, final: AutocompleteTrie.SORT_DISTANCE)
    print("\tNumber of results: ", results.count)
    for (chars, cost, priority) in results {
        var ch = String(chars)
        print(ch, " ", cost)
    }
    count += 1
}

print("Program Finished")



