main = do
    putStrLn $ show $ editDistance "hello" "hllo" []

editDistance :: [Char] -> [Char] -> [Int] -> Int
editDistance w1 w2 [] = editDistance w1 w2 [0..(length w2)]
editDistance [] w2 pRow = last pRow
editDistance w1 [] pRow = length w1
editDistance (c:w1) w2 pRow = editDistance w1 w2 (curRow 0 (length w2 + 1) c w2 pRow 0)

curRow :: Int -> Int -> Char -> [Char] -> [Int] -> Int -> [Int]
curRow 0 end c w pRow lst = [start] ++ curRow 1 end c w pRow start
                      where start = pRow !! 0 + 1
curRow index end c w pRow lst = if index < end
                        then [minim] ++ curRow (index + 1) end c w pRow minim
                        else []
                      where minim = min replace (min insert delete)
                                    where replace = pRow !! (index - 1) + (if c == (w !! (index - 1)) then 0 else 1)
                                          insert = lst + 1
                                          delete = pRow !! index + 1