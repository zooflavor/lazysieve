import System.Environment
import System.IO

----------

data Linked t = Empty | Cons t (Linked t) deriving Eq

showLinkedTail :: Show t => Linked t -> String
showLinkedTail Empty = ""
showLinkedTail (Cons x y) = ", " ++ (show x) ++ (showLinkedTail y)

instance (Show t) => Show (Linked t) where
	show Empty = "[]"
	show (Cons x y) = "[" ++ (show x) ++ (showLinkedTail y) ++ "]"

linkedHead :: Linked t -> t -> t
linkedHead Empty defaultValue = defaultValue
linkedHead (Cons x _) _ = x

linkedTail :: Linked t -> Linked t
linkedTail Empty = Empty
linkedTail (Cons _ y) = y

linkedMap :: (t -> u) -> Linked t -> Linked u
linkedMap func Empty = Empty
linkedMap func (Cons head tail) = (Cons (func head) (linkedMap func tail))

---------

data Pair t u = Pair t u deriving Eq

instance (Show t, Show u) => Show (Pair t u) where
	show (Pair x y) = "(" ++ (show x) ++ ", " ++ (show y) ++ ")"

pairFirst :: Pair t u -> t
pairFirst (Pair x _) = x

pairSecond :: Pair t u -> u
pairSecond (Pair _ y) = y

---------

data Bit = Bit0 | Bit1 deriving Eq

instance Show Bit where
	show Bit0 = "0"
	show Bit1 = "1"

type Bits2 = Pair Bit Bit

type Bits = Linked Bit

-- láncolt listaként ábrázolt számot visszalakít haskell számmá a megjelenítéshez
bitsToNumber :: Bits -> Integer
bitsToNumber l = bitsToNumber2 l 0 1

bitsToNumber2 :: Bits -> Integer -> Integer -> Integer
bitsToNumber2 Empty sum _ = sum
bitsToNumber2 (Cons Bit0 x) sum power = bitsToNumber2 x sum (2 * power)
bitsToNumber2 (Cons Bit1 x) sum power =
	bitsToNumber2 x (sum + power) (2 * power)

----------

-- 3 bit összeadása, eredménye egy legfeljebb 2 bites szám
-- két szám összeadásánál minden helyiértéken ez alapján számolja ki az eredményt
-- a 3 argumentum az összeadás két argumentumának számjegye az aktuális helyiértéken, és az átvitel bit
addBits3 :: Bit -> Bit -> Bit -> Bits2
addBits3 Bit0 Bit0 Bit0 = Pair Bit0 Bit0
addBits3 Bit0 Bit0 Bit1 = Pair Bit0 Bit1
addBits3 Bit0 Bit1 Bit0 = Pair Bit0 Bit1
addBits3 Bit0 Bit1 Bit1 = Pair Bit1 Bit0
addBits3 Bit1 Bit0 Bit0 = Pair Bit0 Bit1
addBits3 Bit1 Bit0 Bit1 = Pair Bit1 Bit0
addBits3 Bit1 Bit1 Bit0 = Pair Bit1 Bit0
addBits3 Bit1 Bit1 Bit1 = Pair Bit1 Bit1

-- a position és az addend összeadása
-- a position és az addend listában a bitek helyiérték szerint növekvő sorrendben vannak
-- az eredmény csak azokat a biteket tartalmazza, amik eltérnek a position-től
-- az eredmény listában a bitek helyiérték szerint csökkenő sorrendben vannak
addPosition :: Bits -> Bits -> Bit -> Bits -> Bits
addPosition position addend carry result =
	let (Pair newCarry newPosition) =
			(addBits3
				(linkedHead position Bit0)
				(linkedHead addend Bit0)
				carry)
			in
		if ((Bit0==newCarry) && (Empty==(linkedTail addend))) then
			result
		else
			(addPosition
				(linkedTail position)
				(linkedTail addend)
				newCarry
				(Cons newPosition result))

-- eggyel megnöveli az argumentumot
-- az eredmény második tagja a módosított bitek száma, a tényleges bitek érdektelenek
incrementPosition :: Bits -> Pair Bits Bits
incrementPosition Empty = Pair (Cons Bit1 Empty) Empty
incrementPosition (Cons Bit0 tail) = Pair (Cons Bit1 tail) Empty
incrementPosition (Cons Bit1 tail) =
		let (Pair newPosition length) = (incrementPosition tail) in
			Pair (Cons Bit0 newPosition) (Cons Bit0 length)

--------

type PrimePosition = Pair Bits Bits
type Bucket = Linked PrimePosition
type Buckets = Linked Bucket

-- a (prime, position) párt beszúrja a megfelelő edénybe
-- a position és a remainder az addPosition fordított eredménye
-- a remainder hossza szabályozza a beszúrás helyét
insertPrime :: Buckets -> Bits -> Bits -> Bits -> Buckets
insertPrime Empty prime position remainder =
	insertPrime (Cons Empty Empty) prime position remainder
insertPrime (Cons bucketsHead bucketsTail) prime position Empty =
	(Cons (Cons (Pair prime position) bucketsHead) bucketsTail)
insertPrime (Cons bucketsHead bucketsTail) prime position (Cons _ remainderTail) =
	(Cons bucketsHead (insertPrime bucketsTail prime position remainderTail))

-- a (prime, position+prime) párt beszúrja a megfelelő edénybe
-- position+prime a következő szám, ahol prime szitálni fog
insert1Prime :: Buckets -> Bits -> Bits -> Buckets
insert1Prime buckets position prime =
	let newPosition = (addPosition position prime Bit0 Empty) in
		insertPrime buckets prime newPosition newPosition

-- a bucket összes prímet beszúrja a következő szitált pozíciójuk alapján az edényekbe
-- a bucket elemeinek pozíció tagját figyelmen kívül hagyja, az mindig üres lista
insertAllPrime :: Buckets -> Bits -> Bucket -> Buckets
insertAllPrime buckets position Empty = buckets
insertAllPrime buckets position (Cons (Pair prime _) tail) =
	insertAllPrime (insert1Prime buckets position prime) position tail

-- egy edényt ketté választ a benne lévő (prím, pozíció) párok pozíciójának legelső bitje alapján
-- az első edénybe a 0-val kezdődő pozíciójú párok kerülnek
-- a második edénybe az 1-gyel kezdődő pozíciójú párok kerülnek
-- a pozíciókból az első, feldolgozott bitet elhagyja
splitBucket :: Bucket -> Bucket -> Bucket -> Pair Bucket Bucket
splitBucket Empty zeros ones = Pair zeros ones
splitBucket (Cons (Pair prime (Cons Bit0 position)) tail) zeros ones =
	splitBucket tail (Cons (Pair prime position) zeros) ones
splitBucket (Cons (Pair prime (Cons Bit1 position)) tail) zeros ones =
	splitBucket tail zeros (Cons (Pair prime position) ones)

-- a sor pozíciójának növelése után a sor invariánsát visszaállítja
-- a második argumentum hossza határozza meg a feldolgozandó edény sorszámát
fixBuckets :: Buckets -> Bits -> Pair Bucket Buckets
fixBuckets Empty _ = Pair Empty Empty
fixBuckets (Cons bucket buckets) Empty =
	Pair bucket (Cons Empty buckets)
fixBuckets (Cons Empty buckets) (Cons _ length) =
	let (Pair bucket newBuckets) = (fixBuckets buckets length) in
		let (Pair zeros ones) = (splitBucket bucket Empty Empty) in
			Pair zeros (Cons ones newBuckets)

--------

type State = Pair Bits Buckets

-- a kezdő állapotban a sor pozíciója 1, és minden edénye üres
startState = Pair (Cons Bit1 Empty) Empty :: State

nextState :: State -> Pair (Linked Bits) State
nextState (Pair position buckets) =
	let (Pair newPosition length) = (incrementPosition position) in
		let (Pair bucket newBuckets) = (fixBuckets buckets length) in
			if (Empty==bucket) then
				Pair
					(Cons newPosition Empty)
					(Pair
						newPosition
						(insert1Prime newBuckets newPosition newPosition))
			else
				Pair
					Empty
					(Pair
						newPosition
						(insertAllPrime newBuckets newPosition bucket))

printPrimes :: Linked Bits -> IO()
printPrimes Empty = return ()
printPrimes (Cons head tail) = do {print (bitsToNumber head); printPrimes tail}

sieve :: State -> Integer -> IO()
sieve state remaining
	| remaining <= 0 = return ()
	| otherwise =
		let (Pair primes newState) = (nextState state) in do
			printPrimes primes
			sieve newState (remaining-1)

printUsage :: IO()
printUsage = do {
	putStrLn "usage:";
	putStrLn "\tbucket-sieve.bin <end>"}

main :: IO()
main = do
	args <- getArgs
	case args of
		[end] -> sieve startState ((read end :: Integer)-1-(bitsToNumber (pairFirst startState)))
		_ -> printUsage
