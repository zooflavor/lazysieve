#include "common.h"

#define BLOCK_SIZE (4096-sizeof(struct Block*))/(sizeof(struct PrimePosition))
#ifndef BUCKET_BITS
#define BUCKET_BITS 8
#endif
#define BUCKET_DIGITS (1<<BUCKET_BITS)
#define BUCKETS (1+(1+64/BUCKET_BITS)*(BUCKET_DIGITS-1))
#define TINY_PRIMES_COUNT 15
#define TINY_PRIMES_COUNT1 13
#define TINY_PRIMES_COUNT2 2

struct Block {
	struct Block *next;
	struct PrimePosition primes[BLOCK_SIZE];
};

struct Bucket {
	struct Block *blocks;
	size_t size;
};

char *databaseDirectory;
struct Bucket buckets[BUCKETS];
struct Block *freeBlocks=0;

uint64_t segment[SEGMENT_SIZE_UINT64_T];
uint64_t segmentCount=-1;
uint64_t segmentStart;

int64_t spaceToReserve=-1;

struct PrimePosition smallPrimes[SEGMENT_SMALL_SIZE_BITS>>1];
int smallPrimesCount=0;

uint8_t tinyPrimeCircumferences[TINY_PRIMES_COUNT];
uint64_t tinyPrimeMasks[TINY_PRIMES_COUNT];
uint8_t tinyPrimes1[]={13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61};
uint8_t tinyPrimes2[]={3, 11, 5, 7};

int8_t wheelClasses[]={
	-1,  0, -5, -4, -3, -2,
	-1,  1, -3, -2, -1,  2,
	-1,  3, -3, -2, -1,  4,
	-1,  5, -3, -2, -1,  6,
	-5, -4, -3, -2, -1,  7};
uint8_t wheelSteps[]={3, 2, 1, 2, 1, 2, 3, 1};

struct Block *allocateBlock();
int bitScanRight(uint64_t value);
int bucketIndex(uint64_t current, uint64_t position);
void checkDatabase();
void freeBlock(struct Block *block);
void init();
uint64_t initPosition(uint64_t prime, int square);
void initPrime(uint64_t prime);
void initTinyPrimes();
void printUsage();
void sieve();
void tinyPrimeMask(uint64_t *mask, uint8_t prime);

struct Block *allocateBlock(struct Block *next) {
	struct Block *result;
	if (0==freeBlocks) {
		result=malloc(sizeof(struct Block));
		if (0==result) {
			printStdError(__FILE__, __LINE__, "malloc");
		}
	}
	else {
		result=freeBlocks;
		freeBlocks=freeBlocks->next;
	}
	result->next=next;
	return result;
}

void addToBucket(int index, uint64_t position, uint64_t prime) {
	struct Bucket *bucket=buckets+index;
	int size=bucket->size;
	if ((BLOCK_SIZE<=size)
			|| (0==bucket->blocks)) {
		bucket->blocks=allocateBlock(bucket->blocks);
		size=0;
	}
	struct PrimePosition *primePosition=bucket->blocks->primes+size;
	primePosition->position=position;
	primePosition->prime=prime;
	bucket->size=size+1;
}

int bitScanRight(uint64_t value) {
	int result=0;
#if BUCKET_BITS == 1
	for (; 0l!=(value>>result); ++result) {
	}
#else
	for (int shift=0; 0l!=(value>>shift); ++result, shift+=BUCKET_BITS) {
	}
#endif
	return result;
}

int bucketIndex(uint64_t current, uint64_t position) {
#if BUCKET_BITS == 1
	return bitScanRight((current^position)>>SEGMENT_SMALL_SIZE_BITS_LOG2);
#else
#endif
	int result=bitScanRight((current^position)>>SEGMENT_SMALL_SIZE_BITS_LOG2);
	if (0==result) {
		return result;
	}
	--result;
	return result*(BUCKET_DIGITS-1)
			+((position>>(SEGMENT_SMALL_SIZE_BITS_LOG2+result*BUCKET_BITS))
					&(BUCKET_DIGITS-1));
}

void checkDatabase() {
	if (!fileExists(databaseDirectory)) {
		printf("a %s adatbázis könyvtár nem létezik\n", databaseDirectory);
		exit(1);
	}
	for (int ii=0; INIT_SEGMENTS>ii; ++ii) {
		char filename[FILE_NAME_SIZE];
		segmentFile(filename, databaseDirectory, ii*SEGMENT_SIZE_NUMBERS+1);
		if (!fileExists(filename)) {
			printf("a %s szegmens fájl nem létezik\n", filename);
			exit(1);
		}
	}
}

void freeBlock(struct Block *block) {
	block->next=freeBlocks;
	freeBlocks=block;
}

void init() {
	for (int ii=0; BUCKETS>ii; ++ii) {
		buckets[ii].blocks=0;
		buckets[ii].size=0;
	}
	initTinyPrimes();
	for (int ii=0; INIT_SEGMENTS>ii; ++ii) {
		printf("felkészülés %d/%d\n", ii+1, INIT_SEGMENTS);
		uint64_t start=ii*SEGMENT_SIZE_NUMBERS+1;
		readSegment(databaseDirectory, segment, start);
		for (size_t jj=(0==ii)?32:0; SEGMENT_SIZE_BITS>jj; ++jj) {
			if (0==((segment[jj>>6])&(1l<<(jj&0x3f)))) {
				initPrime(start+2l*jj);
			}
		}
	}
	printf("felkészülés vége\n");
}

uint64_t initPosition(uint64_t prime, int square) {
	uint64_t position=prime*((2*segmentStart+1)/prime);
	if (0==(position%2)) {
		position+=prime;
	}
	if (2*segmentStart+1>position) {
		position+=2*prime;
	}
	if (square) {
		uint64_t squared=prime*prime;
		if (squared>position) {
			position=squared;
		}
	}
	return position;
}

void initPrime(uint64_t prime) {
	if ((prime<=SEGMENT_SMALL_SIZE_BITS)
			/*&& (INIT_SEGMENTS*SEGMENT_SIZE_NUMBERS+1>=prime*prime)*/) {
		smallPrimes[smallPrimesCount].position
				=(initPosition(prime, 1)/2)-segmentStart;
		smallPrimes[smallPrimesCount].prime=prime;
		++smallPrimesCount;
	}
	else {
		uint64_t position=initPosition(prime, 1);
		uint64_t quotient=position/prime;
		uint64_t remainder=quotient%30;
		int class=wheelClasses[remainder];
		if (0>class) {
			position=(quotient-class)*prime;
			class=wheelClasses[remainder-class];
		}
		position>>=1;
		addToBucket(
				bucketIndex(segmentStart-SEGMENT_SMALL_SIZE_BITS, position),
				position, (((uint64_t)class)<<61)|prime);
	}
}

void initTinyPrimes() {
	int count=0;
	for (int ii=0; TINY_PRIMES_COUNT2>ii; ++ii) {
		uint8_t prime0=tinyPrimes2[2*ii];
		uint8_t prime1=tinyPrimes2[2*ii+1];
		uint64_t mask=0l;
		tinyPrimeMask(&mask, prime0);
		tinyPrimeMask(&mask, prime1);
		tinyPrimeCircumferences[count]=prime0*prime1;
		tinyPrimeMasks[count]=mask;
		++count;
	}
	for (int ii=0; TINY_PRIMES_COUNT1>ii; ++ii) {
		uint8_t prime=tinyPrimes1[ii];
		uint64_t mask=0l;
		tinyPrimeMask(&mask, prime);
		tinyPrimeCircumferences[count]=prime;
		tinyPrimeMasks[count]=mask;
		++count;
	}
	for (int ii=0; TINY_PRIMES_COUNT>ii; ++ii) {
		tinyPrimeCircumferences[ii]*=64/tinyPrimeCircumferences[ii];
	}
}

int main(int argv, char *argc[]) {
	if ((6!=argv)
			|| (0!=strcmp(argc[2], "start"))) {
		printUsage();
	}
	databaseDirectory=argc[1];
	checkDatabase();
	segmentStart=strtoull(argc[3], 0, 0);
	if (segmentStart<GENERATOR_START_NUMBER) {
		printf("A %lu szegmens kezdet kisebb, mint a minimum %lu\n",
				segmentStart, GENERATOR_START_NUMBER);
		exit(1);
	}
	if (1l!=(segmentStart&(SEGMENT_SIZE_NUMBERS-1))) {
		printf("a %lu szegmens kezdet nem 1-gyel, hanem %lu-val kongruens (mod %lu)\n",
				segmentStart,
				segmentStart&(SEGMENT_SIZE_NUMBERS-1),
				SEGMENT_SIZE_NUMBERS);
		exit(1);
	}
	if (END_NUMBER<=segmentStart) {
		printf("a %lu szegmens kezdet nagyobb, mint a maximum %lu\n",
				segmentStart, END_NUMBER-1l);
		exit(1);
	}
	printf("szegmens kezdet:    ");
	printUint64(20, segmentStart);
	printf("\n");
	if (0==strcmp(argc[4], "reserve-space")) {
		spaceToReserve=strtoll(argc[5], 0, 0);
		if ((1l<<27)>spaceToReserve) {
			printf("a %lu byte fenntartandó hely kisebb, mint a minimum %lu\n",
					spaceToReserve,
					(1l<<27));
			exit(1);
		}
		printf("fenntartandó hely: ");
		printUint64(20, spaceToReserve);
		printf(" byte \n");
	}
	else if (0==strcmp(argc[4], "segments")) {
		segmentCount=strtoll(argc[5], 0, 0);
		if (0>=segmentCount) {
			printf("a %lu szegmens kisebb, mint a minimum 1\n", segmentCount);
			exit(1);
		}
		uint64_t max=(END_NUMBER-segmentStart)/SEGMENT_SIZE_NUMBERS;
		if (max<segmentCount) {
			segmentCount=max;
		}
		printf("szegmensek száma: ");
		printUint64(20, segmentCount);
		printf("\n");
	}
	else {
		printUsage();
	}
	segmentStart>>=1;
	printf("kis szegmensek mérete: %d\n", SEGMENT_SMALL_SIZE_BITS_LOG2);
	printf("elágazások bitjei: %d\n", BUCKET_BITS);
	uint64_t initStart=nanoTime();
	init();
	uint64_t allSieveNanos=0l;
	for (int first=1;
			(0<segmentCount)
					|| ((0>segmentCount)
							&& (spaceToReserve+SEGMENT_SIZE_UINT8_T
									<freeSpace(databaseDirectory))); ) {
		if (first) {
			first=0;
		}
		else {
			initStart=nanoTime();
		}
		uint64_t initEnd=nanoTime();
		uint64_t sieveStart=nanoTime();
		sieve();
		uint64_t sieveEnd=nanoTime();
		uint64_t initNanos=initEnd-initStart;
		uint64_t sieveNanos=sieveEnd-sieveStart;
		writeSegment(databaseDirectory, segment, 2*segmentStart+1,
				initNanos, sieveNanos);
		allSieveNanos+=sieveNanos;
		printSegmentStats(2*segmentStart+1, initNanos, sieveNanos);
		segmentStart+=SEGMENT_SIZE_BITS;
		if (0<=segmentCount) {
			--segmentCount;
		}
	}
	printf("összes szitálás ");
	printUint64(0, allSieveNanos);
	printf(" ns\n");
	return 0;
}

void printUsage() {
	printf("használat:\n");
	printf("\tgenerator [adatbázis könyvtár] start [szegmens kezdete] reserve-space [fenntartandó hely]\n");
	printf("\tgenerator [adatbázis könyvtár] start [szegmens kezdete] segments [szegmensek száma]\n");
	exit(1);
}

void sieve() {
	for (size_t sc=0; SEGMENT_SIZE_BITS/SEGMENT_SMALL_SIZE_BITS>sc; ++sc) {
		for (size_t pp=sc*SEGMENT_SMALL_SIZE_UINT64_T,
					ee=pp+SEGMENT_SMALL_SIZE_UINT64_T;
				ee>pp; ++pp) {
			uint64_t segmentBits=0l;
			for (int tp=0; TINY_PRIMES_COUNT>tp; ++tp) {
				uint8_t circumference=tinyPrimeCircumferences[tp];
				uint64_t mask=tinyPrimeMasks[tp];
				segmentBits|=mask;
				mask>>=64-circumference;
				mask|=mask<<circumference;
				tinyPrimeMasks[tp]=mask;
			}
			segment[pp]=segmentBits;
		}
		int cacheStart=sc*SEGMENT_SMALL_SIZE_BITS;
		int cacheEnd=cacheStart+SEGMENT_SMALL_SIZE_BITS;
		for (size_t pp=0; smallPrimesCount>pp; ++pp) {
			uint64_t position=smallPrimes[pp].position;
			for (uint64_t prime=smallPrimes[pp].prime;
					cacheEnd>position;
					position+=prime) {
				segment[position>>6]|=1l<<(position&0x3fl);
			}
			smallPrimes[pp].position=position;
		}
		uint64_t positionStart=segmentStart+cacheStart;
		uint64_t positionEnd=positionStart+SEGMENT_SMALL_SIZE_BITS;
		struct Bucket *bucket=buckets+bucketIndex(
				positionStart-SEGMENT_SMALL_SIZE_BITS, positionStart);
		int size=bucket->size;
		bucket->size=0;
		struct Block *blocks=bucket->blocks;
		bucket->blocks=0;
		while (0!=blocks) {
			struct Block *next=blocks;
			blocks=blocks->next;
			for (int ii=0; size>ii; ++ii) {
				uint64_t position=next->primes[ii].position;
				uint64_t prime=next->primes[ii].prime;
				uint64_t class=(prime>>61)&7l;
				prime&=(~((uint64_t)0l))>>3;
				int index=bucketIndex(positionStart, position);
				if (0==index) {
					//this loop runs exactly once
					//	if all primes>=SEGMENT_SMALL_SIZE
					while (positionEnd>position) {
						segment[(position-segmentStart)>>6]
								|=1l<<((position-segmentStart)&0x3fl);
						position+=wheelSteps[class]*prime;
						class=(class+1)&7l;
					}
					index=bucketIndex(positionStart, position);
				}
				addToBucket(index, position, (class<<61)|prime);
			}
			freeBlock(next);
			size=BLOCK_SIZE;
		}
	}
	for (size_t pp=0; smallPrimesCount>pp; ++pp) {
		smallPrimes[pp].position-=SEGMENT_SIZE_BITS;
	}
}

void tinyPrimeMask(uint64_t *mask, uint8_t prime) {
	for (uint64_t pp=(initPosition(prime, 1)/2)-segmentStart;
			64>pp;
			pp+=prime) {
		(*mask)|=1l<<pp;
	}
}
