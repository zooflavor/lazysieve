#ifndef __COMMON_H__
#define __COMMON_H__ 1

#define _POSIX_C_SOURCE 199309L

#define GZIP 0

#include <errno.h>
#include <fcntl.h>
#include <inttypes.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <unistd.h>
#include <zlib.h>
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
void gzReadFully(gzFile file, void *buffer, size_t length);
void gzWriteFully(gzFile file, void *buffer, size_t length);
void makeDirectoryIfDoesntExist(char *file, int line,
		char *directory, mode_t mode);
uint64_t nanoTime();
void printErrorPrefix(char *file, int line, char *message);
void printSegmentStats(uint64_t segmentStart, uint64_t initNanos,
		uint64_t sieveNanos, uint64_t gzipNanos);
void printStdError(char *file, int line, char *message);
void printUint64(int length, uint64_t value);
void readFully(int file, void *buffer, size_t length);
void readSegment(char *databaseDirectory, void *segment, uint64_t start);
void segmentFile(char *result, char *databaseDirectory, uint64_t segmentStart);
uint64_t sqrt64(int64_t value);
void writeFully(int file, void *buffer, size_t length);
uint64_t writeSegment(char *databaseDirectory, void *segment, uint64_t start,
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

void gzReadFully(gzFile file, void *buffer, size_t length) {
	uint8_t *buffer2=buffer;
	while (0<length) {
		int rr=gzread(file, buffer2, length);
		if (0==rr) {
			printErrorPrefix(__FILE__, __LINE__, "gzread");
			printf("eof\n");
			exit(1);
		}
		else if (0>rr) {
			printErrorPrefix(__FILE__, __LINE__, "gzread");
			printf("failed\n");
			exit(1);
		}
		buffer2+=rr;
		length-=rr;
	}
}

void gzWriteFully(gzFile file, void *buffer, size_t length) {
	uint8_t *buffer2=buffer;
	while (0<length) {
		int rr=gzwrite(file, buffer2, length);
		if (0>=rr) {
			printErrorPrefix(__FILE__, __LINE__, "gzwrite");
			printf("failed\n");
			exit(1);
		}
		buffer2+=rr;
		length-=rr;
	}
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
		uint64_t sieveNanos, uint64_t gzipNanos) {
	printf("segment ");
	printUint64(10, segmentStart/SEGMENT_SIZE_NUMBERS);
	printf(" - start ");
	printUint64(20, segmentStart);
	printf(" - init ");
	printUint64(20, initNanos);
	printf(" ns - sieve ");
	printUint64(20, sieveNanos);
	printf(" ns - gzip ");
	printUint64(20, gzipNanos);
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
	gzFile gzf=gzopen(filename, "rb");
	if (0==gzf) {
		printErrorPrefix(__FILE__, __LINE__, "gzopen");
		printf("failed\n");
		exit(1);
	}
	if (0!=gzbuffer(gzf, 256*1024)) {
		printErrorPrefix(__FILE__, __LINE__, "gzbuffer");
		printf("failed\n");
		exit(1);
	}
	gzReadFully(gzf, segment, SEGMENT_SIZE_UINT8_T);
	uint64_t temp;
	gzReadFully(gzf, &temp, sizeof(uint64_t));
	if (start!=temp) {
		printf("segment file %s: invalid segment start %lu\n",
				filename, temp);
		exit(1);
	}
	gzReadFully(gzf, &temp, sizeof(uint64_t));
	gzReadFully(gzf, &temp, sizeof(uint64_t));
	gzReadFully(gzf, &temp, sizeof(uint64_t));
	if (Z_OK!=gzclose(gzf)) {
		printErrorPrefix(__FILE__, __LINE__, "gzclose");
		printf("failed\n");
		exit(1);
	}
}

void segmentFile(char *result, char *databaseDirectory,
		uint64_t segmentStart) {
	sprintf(result, "%s/%02lx/%02lx/%02lx/%016lx.gz",
			databaseDirectory,
			(segmentStart>>56)&0xffl,
			(segmentStart>>48)&0xffl,
			(segmentStart>>40)&0xffl,
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

uint64_t writeSegment(char *databaseDirectory, void *segment, uint64_t start,
		uint64_t initNanos, uint64_t sieveNanos) {
	char tempFileName[FILE_NAME_SIZE];
	char directory0Name[FILE_NAME_SIZE];
	char directory1Name[FILE_NAME_SIZE];
	char directory2Name[FILE_NAME_SIZE];
	char outputFileName[FILE_NAME_SIZE];
	sprintf(tempFileName, "%s/tmp.tmp", databaseDirectory);
	sprintf(directory0Name, "%s/%02lx", databaseDirectory, (start>>56)&0xff);
	sprintf(directory1Name, "%s/%02lx", directory0Name, (start>>48)&0xff);
	sprintf(directory2Name, "%s/%02lx", directory1Name, (start>>40)&0xff);
	sprintf(outputFileName, "%s/%016lx.gz", directory2Name, start);
	deleteIfExists(tempFileName);
	makeDirectoryIfDoesntExist(__FILE__, __LINE__, directory0Name, S_IRWXU|S_IRWXG|S_IRWXO);
	makeDirectoryIfDoesntExist(__FILE__, __LINE__, directory1Name, S_IRWXU|S_IRWXG|S_IRWXO);
	makeDirectoryIfDoesntExist(__FILE__, __LINE__, directory2Name, S_IRWXU|S_IRWXG|S_IRWXO);
	deleteIfExists(outputFileName);
#if GZIP == 1
	gzFile gzf=gzopen(tempFileName, "wb1");
	if (0==gzf) {
		printErrorPrefix(__FILE__, __LINE__, "gzopen");
		printf("failed\n");
		exit(1);
	}
	if (0!=gzbuffer(gzf, 256*1024)) {
		printErrorPrefix(__FILE__, __LINE__, "gzbuffer");
		printf("failed\n");
		exit(1);
	}
	uint64_t gzipStart=nanoTime();
	gzWriteFully(gzf, segment, SEGMENT_SIZE_UINT8_T);
	if (Z_OK!=gzflush(gzf, Z_FULL_FLUSH)) {
		printErrorPrefix(__FILE__, __LINE__, "gzflush");
		printf("failed\n");
		exit(1);
	}
	uint64_t gzipNanos=nanoTime()-gzipStart;
	gzWriteFully(gzf, &start, sizeof(uint64_t));
	gzWriteFully(gzf, &initNanos, sizeof(uint64_t));
	gzWriteFully(gzf, &sieveNanos, sizeof(uint64_t));
	gzWriteFully(gzf, &gzipNanos, sizeof(uint64_t));
	if (Z_OK!=gzclose(gzf)) {
		printErrorPrefix(__FILE__, __LINE__, "gzclose");
		printf("failed\n");
		exit(1);
	}
#else
	int gzf=open(tempFileName, O_CREAT|O_RDWR|O_TRUNC,
			S_IRUSR|S_IWUSR|S_IRGRP|S_IWGRP|S_IROTH|S_IWOTH);
	if (-1==gzf) {
		printStdError(__FILE__, __LINE__, "open");
	}
	uint64_t gzipStart=nanoTime();
	writeFully(gzf, segment, SEGMENT_SIZE_UINT8_T);
	if (-1==fdatasync(gzf)) {
		printStdError(__FILE__, __LINE__, "fdatasync");
	}
	uint64_t gzipNanos=nanoTime()-gzipStart;
	writeFully(gzf, &start, sizeof(uint64_t));
	writeFully(gzf, &initNanos, sizeof(uint64_t));
	writeFully(gzf, &sieveNanos, sizeof(uint64_t));
	writeFully(gzf, &gzipNanos, sizeof(uint64_t));
	if (-1==close(gzf)) {
		printStdError(__FILE__, __LINE__, "close");
	}
#endif
	if (0!=rename(tempFileName, outputFileName)) {
		printStdError(__FILE__, __LINE__, "rename");
	}
	return gzipNanos;
}

#endif
