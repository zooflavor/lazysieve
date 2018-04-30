#include "main.h"

size_t *primesList0;
size_t *primesList1;
size_t primesListCount;
size_t primesListLength;

void initBuild() {
	memset(primes, ~0l, length*sizeof(uint64_t));
	primesListCount=0;
	primesListLength=32*1024*1024;
	primesList0=CMALLOC(primesListLength*sizeof(size_t));
	primesList1=CMALLOC(primesListLength*sizeof(size_t));
}

void finishBuild() {
}

void nextPrime(size_t prime, size_t offset) {
	if (primesListCount>=primesListLength) {
		primesListLength*=2;
		primesList0=CREALLOC(primesList0, primesListLength*sizeof(size_t));
		primesList1=CREALLOC(primesList1, primesListLength*sizeof(size_t));
	}
	primesList0[primesListCount]=prime;
	primesList1[primesListCount]=offset;
	++primesListCount;
}

void sieve() {
	for (int ii=0; primesListCount>ii; ++ii) {
		for (size_t prime=primesList0[ii], offset=primesList1[ii];
				size2>offset;
				offset+=prime) {
			composites[offset/64]|=1l<<(offset%64);
		}
	}
}
