#include "common.h"

char *databaseDirectory;
uint64_t initStart;
uint64_t initEnd;
struct PrimePosition primes[1<<15];
size_t primesCount=0;
uint64_t segment[SEGMENT_SIZE_UINT64_T];
uint64_t sieveStart;
uint64_t sieveEnd;

void initPrimes() {
	for (size_t ii=3; 1<<16>ii; ii+=2) {
		int prime=1;
		for (size_t jj=0; primesCount>jj; ++jj) {
			if (0==(ii%primes[jj].prime)) {
				prime=0;
				break;
			}
		}
		if (prime) {
			primes[primesCount].position=(ii*ii)>>1;
			primes[primesCount].prime=ii;
			++primesCount;
		}
	}
}

int main(int argv, char *argc[]) {
	if (2!=argv) {
		printf("usage:\n");
		printf("\tinit [database-directory]\n");
		exit(1);
	}
	databaseDirectory=argc[1];
	if (!fileExists(databaseDirectory)) {
		printf("database directory %s doesn't exist\n", databaseDirectory);
		exit(1);
	}
	initStart=nanoTime();
	initPrimes();
	for (size_t ss=0; INIT_SEGMENTS>ss; ++ss) {
		if (0!=ss) {
			initStart=nanoTime();
		}
		uint64_t segmentStart=ss*SEGMENT_SIZE_BITS;
		memset(segment, 0, SEGMENT_SIZE_UINT8_T);
		if (0==ss) {
			segment[0]|=1;
		}
		initEnd=nanoTime();
		sieveStart=nanoTime();
		uint64_t segmentEnd=segmentStart+SEGMENT_SMALL_SIZE_BITS;
		for (size_t sc=SEGMENT_SIZE_BITS/SEGMENT_SMALL_SIZE_BITS;
				0<sc;
				--sc, segmentEnd+=SEGMENT_SMALL_SIZE_BITS) {
			for (size_t pp=0; primesCount>pp; ++pp) {
				for (; segmentEnd>primes[pp].position;
						primes[pp].position+=primes[pp].prime) {
					segment[(primes[pp].position-segmentStart)>>6]
							|=1l<<(primes[pp].position&0x3f);
				}
			}
		}
		sieveEnd=nanoTime();
		fixEndianness(segment);
		uint64_t initNanos=initEnd-initStart;
		uint64_t sieveNanos=sieveEnd-sieveStart;
		uint64_t gzipNanos=writeSegment(databaseDirectory,
				segment, 2*segmentStart+1, initNanos, sieveNanos);
		printSegmentStats(2*segmentStart+1, initNanos, sieveNanos, gzipNanos);
	}
	return 0;
}
