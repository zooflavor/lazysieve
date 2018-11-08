#ifndef __COMMON_H__
#define __COMMON_H__ 1

#define _POSIX_C_SOURCE 199309L

#include <errno.h>
#include <fcntl.h>
#include <inttypes.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/statvfs.h>
#include <sys/types.h>

#define FILE_NAME_SIZE 256
#define INIT_SEGMENTS 4
#define SEGMENT_SIZE_BITS (1l<<29)
#define SEGMENT_SIZE_NUMBERS (SEGMENT_SIZE_BITS<<1)
#define SEGMENT_SIZE_UINT8_T (SEGMENT_SIZE_BITS>>3)
#define SEGMENT_SIZE_UINT64_T (SEGMENT_SIZE_BITS>>6)
#ifndef SEGMENT_SMALL_SIZE_BITS_LOG2
#define SEGMENT_SMALL_SIZE_BITS_LOG2 22
#endif
#define SEGMENT_SMALL_SIZE_BITS (1l<<SEGMENT_SMALL_SIZE_BITS_LOG2)
#define SEGMENT_SMALL_SIZE_UINT8_T (SEGMENT_SMALL_SIZE_BITS>>3)
#define SEGMENT_SMALL_SIZE_UINT64_T (SEGMENT_SMALL_SIZE_BITS>>6)
#define END_NUMBER ((uint64_t)(1l-(1l<<34)))
#define GENERATOR_START_NUMBER ((1l<<32)+1l)

#define CMALLOC(size) cmalloc(__FILE__, __LINE__, size)

struct PrimePosition {
	uint64_t position;
	uint64_t prime;
};

char digits[]="0123456789abcdef";

void *cmalloc(char *file, int line, size_t size);
void deleteIfExists(char *file);
int fileExists(char *file);
void fixEndianness(uint64_t *segment);
uint64_t freeSpace(char *databaseDirectory);
void makeDirectoryIfDoesntExist(char *file, int line,
		char *directory, mode_t mode);
uint64_t nanoTime();
void printErrorPrefix(char *file, int line, char *message);
void printSegmentStats(uint64_t segmentStart, uint64_t initNanos,
		uint64_t sieveNanos);
void printStdError(char *file, int line, char *message);
void printUint64(int length, uint64_t value);
void readFully(int file, void *buffer, size_t length);
void readSegment(char *databaseDirectory, void *segment, uint64_t start);
void segmentFile(char *result, char *databaseDirectory, uint64_t segmentStart);
uint64_t sqrt64(int64_t value);
void writeFully(int file, void *buffer, size_t length);
void writeSegment(char *databaseDirectory, void *segment, uint64_t start,
		uint64_t initNanos, uint64_t sieveNanos);

void *cmalloc(char *file, int line, size_t size) {
	void *result=malloc(size);
	if (0==result) {
		printStdError(file, line, "malloc");
	}
	return result;
}

void deleteIfExists(char *file) {
	if (0!=unlink(file)) {
		int rr=errno;
		if (ENOENT==rr) {
			return;
		}
		errno=rr;
		printStdError(__FILE__, __LINE__, "unlink");
	}
}

void fixEndianness(uint64_t *segment) {
}

int fileExists(char *file) {
	int rr=access(file, F_OK);
	if (0==rr) {
		return 1;
	}
	rr=errno;
	if (ENOENT==rr) {
		return 0;
	}
	errno=rr;
	printStdError(__FILE__, __LINE__, "access");
	return 0;
}

uint64_t freeSpace(char *databaseDirectory) {
	struct statvfs stat;
	if (0!=statvfs(databaseDirectory, &stat)) {
		printStdError(__FILE__, __LINE__, "statvfs");
	}
	return stat.f_bsize*stat.f_bavail;
}

void makeDirectoryIfDoesntExist(char *file, int line,
		char *directory, mode_t mode) {
	if (0!=mkdir(directory, mode)) {
		int rr=errno;
		if (EEXIST==rr) {
			return;
		}
		errno=rr;
		printStdError(file, line, "mkdir");
	}
}

uint64_t nanoTime() {
	struct timespec ts;
	if (0!=clock_gettime(CLOCK_MONOTONIC_RAW, &ts)) {
		printStdError(__FILE__, __LINE__, "clock_gettime");
	}
	return ts.tv_sec*1000000000+ts.tv_nsec;
}

void printErrorPrefix(char *file, int line, char *message) {
	printf("%s: %d: %s: ", file, line, message);
	fflush(stdout);
}

void printSegmentStats(uint64_t segmentStart, uint64_t initNanos,
		uint64_t sieveNanos) {
	printf("segment ");
	printUint64(10, segmentStart/SEGMENT_SIZE_NUMBERS);
	printf(" - start ");
	printUint64(20, segmentStart);
	printf(" - init ");
	printUint64(20, initNanos);
	printf(" ns - sieve ");
	printUint64(20, sieveNanos);
	printf(" ns\n");
}

void printStdError(char *file, int line, char *message) {
	printErrorPrefix(file, line, message);
	perror(0);
	exit(1);
}

void printUint64(int length, uint64_t value) {
	char buf[128];
	int bufLength=0;
	for (; (0==bufLength) || (0l<value); value/=10) {
		buf[bufLength]=digits[value%10];
		++bufLength;
		if (3==(bufLength%4)) {
			buf[bufLength]=' ';
			++bufLength;
		}
	}
	for (int padding=length-bufLength; 0<padding; --padding) {
		buf[bufLength]=' ';
		++bufLength;
	}
	buf[bufLength]=0;
	for (int ii=0; bufLength>2*ii+1; ++ii) {
		char tt=buf[ii];
		buf[ii]=buf[bufLength-ii-1];
		buf[bufLength-ii-1]=tt;
	}
	printf("%s", buf);
}

void readFully(int file, void *buffer, size_t length) {
	char *buffer2=buffer;
	ssize_t result;
	while (0<length) {
		result=read(file, buffer2, length);
		if (-1==result) {
			printStdError(__FILE__, __LINE__, "read");
		}
		if (0==result) {
			printErrorPrefix(__FILE__, __LINE__, "read");
			printf("unexpected eof\n");
			exit(1);
		}
		buffer2+=result;
		length-=result;
	}
}

void readSegment(char *databaseDirectory, void *segment, uint64_t start) {
	char filename[FILE_NAME_SIZE];
	segmentFile(filename, databaseDirectory, start);
	int file=open(filename, O_RDONLY);
	if (-1==file) {
		printStdError(__FILE__, __LINE__, "open");
	}
	readFully(file, segment, SEGMENT_SIZE_UINT8_T);
	uint64_t temp;
	readFully(file, &temp, sizeof(uint64_t));
	if (start!=temp) {
		printf("segment file %s: invalid segment start %lu\n",
				filename, temp);
		exit(1);
	}
	readFully(file, &temp, sizeof(uint64_t));
	readFully(file, &temp, sizeof(uint64_t));
	if (-1==close(file)) {
		printStdError(__FILE__, __LINE__, "close");
	}
}

void segmentFile(char *result, char *databaseDirectory,
		uint64_t segmentStart) {
	sprintf(result,
			"%s/primes.%016lx",
			databaseDirectory,
			segmentStart);
}

uint64_t sqrt64(int64_t value) {
	if (1>=value) {
		return value;
	}
	uint64_t ll=1;
	uint64_t uu=(INT64_MAX>>32)+1;
	while (ll+1<uu) {
		uint64_t mm=(ll+uu)>>1;
		if (value>=mm*mm) {
			ll=mm;
		}
		else {
			uu=mm;
		}
	}
	return ll;
}

void writeFully(int file, void *buffer, size_t length) {
	char *buffer2=buffer;
	ssize_t result;
	while (0<length) {
		result=write(file, buffer2, length);
		if (-1==result) {
			printStdError(__FILE__, __LINE__, "write");
		}
		buffer2+=result;
		length-=result;
	}
}

void writeSegment(char *databaseDirectory, void *segment, uint64_t start,
		uint64_t initNanos, uint64_t sieveNanos) {
	char tempFileName[FILE_NAME_SIZE];
	char outputFileName[FILE_NAME_SIZE];
	sprintf(tempFileName, "%s/tmp.tmp", databaseDirectory);
	segmentFile(outputFileName, databaseDirectory, start);
	deleteIfExists(tempFileName);
	int file=open(tempFileName, O_CREAT|O_RDWR|O_TRUNC,
			S_IRUSR|S_IWUSR|S_IRGRP|S_IWGRP|S_IROTH|S_IWOTH);
	if (-1==file) {
		printStdError(__FILE__, __LINE__, "open");
	}
	writeFully(file, segment, SEGMENT_SIZE_UINT8_T);
	writeFully(file, &start, sizeof(uint64_t));
	writeFully(file, &initNanos, sizeof(uint64_t));
	writeFully(file, &sieveNanos, sizeof(uint64_t));
	if (-1==close(file)) {
		printStdError(__FILE__, __LINE__, "close");
	}
	deleteIfExists(outputFileName);
	if (0!=rename(tempFileName, outputFileName)) {
		printStdError(__FILE__, __LINE__, "rename");
	}
}

#endif
