#define _POSIX_C_SOURCE 199309L

#include <fcntl.h>
#include <gmp.h>
#include <inttypes.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/types.h>

#include "util.h"

#define WHEEL_CIRCUMFERENCE 2

void initBuild();
void finishBuild();
void nextPrime(size_t prime, size_t offset);
void sieve();

void readPrimes(char *inputFile);
void writeResult(char *outputFile);

uint64_t *composites;
mpz_t end;
mpz_t endSqrt;
size_t length;
uint64_t *primes;
size_t size;
size_t size2;
mpz_t start;

int main(int argc, char *argv[]) {
	printf("binary: %s\n", OUTPUT);
	printf("source: %s %s\n", MAIN, ARGS);
	if (5!=argc) {
		printf("usage: %s <input-file> <start> <size> <output-file>\n",
				OUTPUT);
		exit(1);
	}
	
	mpz_init(end);
	mpz_init(endSqrt);
	mpz_init(start);
	AZ(mpz_set_str(start, argv[2], 10));
	
	size=atol(argv[3]);
	
	mpz_add_ui(end, start, size);
	mpz_sqrt(endSqrt, end);
	
	gmp_printf("start:     %'20Zd\n", start);
	gmp_printf("size:      %'20d\n", size);
	gmp_printf("end:       %'20Zd\n", end);
	gmp_printf("sqrt(end): %'20Zd\n", endSqrt);
	if (0==mpz_tstbit(start, 0)) {
		printf("start must be odd\n");
		exit(1);
	}
	if (0>=mpz_cmp_ui(start, WHEEL_CIRCUMFERENCE)) {
		printf("start must be greater than %d\n", WHEEL_CIRCUMFERENCE);
		exit(1);
	}
	if (0!=(size%128)) {
		printf("size must be a multiple of 128\n");
		exit(1);
	}
	if (0>=mpz_cmp(start, endSqrt)) {
		printf("start must be greater than sqrt(end)\n");
		exit(1);
	}
	
	uint64_t time0=nanoTime();
	size2=size/2;
	length=size/128;
	composites=CMALLOC(length*sizeof(uint64_t));
	primes=CMALLOC(length*sizeof(uint64_t));
	memset(composites, 0l, length*sizeof(uint64_t));
	memset(primes, ~0l, length*sizeof(uint64_t));
	initBuild();
	readPrimes(argv[1]);
	finishBuild();
	uint64_t time1=nanoTime();
	printf("build:     %20ld ns\n", time1-time0);
	time0=nanoTime();
	sieve();
	time1=nanoTime();
	printf("sieve:     %20ld ns\n", time1-time0);
	writeResult(argv[4]);
	
	
	mpz_clear(start);
	mpz_clear(endSqrt);
	mpz_clear(end);
	
	return 0;
}

void readPrimes(char *inputFile) {
	mpz_t prime, prime2, temp;
	mpz_init(prime);
	mpz_init(prime2);
	mpz_init(temp);
	
	int file=CP(open(inputFile, 0));
	uint8_t buf[1024];
	
	for (int ii=sizeof(uint8_t); 0<ii; ) {
		size_t rr=CNN(read(file, buf, ii));
		if (0==rr) {
			printf("unexpected eof\n");
			exit(1);
		}
		ii-=rr;
		if (1!=buf[0]) {
			printf("%s must start at 3\n", inputFile);
			exit(1);
		}
	}
	
	mpz_set_ui(prime, 3);
	while (1) {
		size_t rr=CNN(read(file, &buf, sizeof(buf)));
		if (0==rr) {
			printf("not enough primes\n");
			exit(1);
		}
		for (int ii=0; rr>ii; ++ii) {
			uint8_t next=buf[ii];
			for (int jj=8; 0<jj; --jj) {
				if (0==(next&1)) {
					if (0<mpz_cmp(prime, endSqrt)) {
						goto end;
					}
					mpz_mul_ui(prime2, prime, 2);
					mpz_tdiv_r(temp, start, prime2);
					int cc=mpz_cmp(temp, prime);
					if (0<cc) {
						mpz_sub(temp, prime2, temp);
						mpz_add(temp, temp, prime);
					}
					else {
						mpz_sub(temp, prime, temp);
					}
					if (0>=mpz_cmp_ui(temp, size)) {
						size_t offset=mpz_get_ui(temp)/2;
						if (0<=mpz_cmp_ui(prime2, size)) {
							composites[offset/64]|=(1l<<(offset%64));
						}
						else {
							nextPrime(mpz_get_ui(prime), offset);
						}
					}
				}
				next>>=1;
				mpz_add_ui(prime, prime, 2);
			}
		}
	}
	end:
	CNN(close(file));
	
	mpz_clear(temp);
	mpz_clear(prime2);
	mpz_clear(prime);
}

void writeResult(char *outputFile) {
	for (int ii=0; length>ii; ++ii) {
		composites[ii]|=~primes[ii];
	}
	uint8_t *buf=(uint8_t*)composites;
	size_t bufLength=length*(sizeof(uint64_t)/sizeof(uint8_t));
	
	mpz_t temp;
	mpz_init(temp);
	mpz_tdiv_q_ui(temp, start, 2);
	
	int file=CP(open(outputFile, O_CREAT|O_RDWR|O_TRUNC,
			S_IRUSR|S_IWUSR|S_IRGRP|S_IWGRP|S_IROTH));
	
	size_t count;
	uint8_t *digits=mpz_export(0, &count, 1, 1, 0, 1, temp);
	if (0==digits) {
		printf("mpz_export failed\n");
		exit(1);
	}
	for (int ii=count-2; 0<=ii; --ii) {
		digits[ii]|=0x80;
	}
	for (int offset=0; 0<count; ) {
		int ww=CNN(write(file, digits+offset, count));
		offset+=ww;
		count-=ww;
	}
	free(digits);
	mpz_clear(temp);
	
	for (; 0<bufLength ; ) {
		ssize_t ww=CNN(write(file, buf, bufLength));
		buf+=ww;
		bufLength-=ww;
	}
	
	CNN(close(file));
}
