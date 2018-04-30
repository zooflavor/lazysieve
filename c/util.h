#define CMALLOC(ss) checkMalloc(__FILE__, __LINE__, "malloc", ss, malloc(ss));
#define CREALLOC(vv, ss) checkMalloc(__FILE__, __LINE__, "realloc", ss, realloc(vv, ss));
#define AE(ee, vv) assertEquals(__FILE__, __LINE__, ee, vv)
#define ANZ(vv) assertNonZero(__FILE__, __LINE__, vv)
#define AZ(vv) assertZero(__FILE__, __LINE__, vv)
#define CNN(vv) checkNonNegative(__FILE__, __LINE__, vv)
#define CP(vv) checkPositive(__FILE__, __LINE__, vv)
#define LEFT_CHILD(ii) ((ii<<1)+1)

int assertEquals(char*, int, int, int);
int assertNonZero(char*, int, int);
int assertZero(char*, int, int);
void *checkMalloc(char *file, int line, char *func, int size, void *value);
int checkNonNegative(char*, int, int);
int checkPositive(char*, int, int);
int16_t min16(int16_t value0, int16_t value1);
int32_t min32(int32_t value0, int32_t value1);
uint64_t nanoTime();

int assertEquals(char *file, int line, int expected, int value) {
	if (expected!=value) {
		printf("%s:%d: assertEquals(%d, %d) failed\n",
				file, line, expected, value);
		exit(1);
	}
	return value;
}

int assertNonZero(char *file, int line, int value) {
	if (0==value) {
		printf("%s:%d: assertNonZero(%d) failed\n", file, line, value);
		exit(1);
	}
	return value;
}

int assertZero(char *file, int line, int value) {
	if (0!=value) {
		printf("%s:%d: assertZero(%d) failed\n", file, line, value);
		exit(1);
	}
	return value;
}

void *checkMalloc(char *file, int line, char *func, int size, void *value) {
	if (0==value) {
		printf("%s:%d: checkMalloc(%s, %d) failed\n", file, line, func, size);
		perror(0);
		exit(1);
	}
	return value;
}

int checkNonNegative(char *file, int line, int value) {
	if (0>value) {
		printf("%s:%d: checkNonNegative(%d) failed\n", file, line, value);
		perror(0);
		exit(1);
	}
	return value;
}

int checkPositive(char *file, int line, int value) {
	if (0>=value) {
		printf("%s:%d: checkPositive(%d) failed\n", file, line, value);
		perror(0);
		exit(1);
	}
	return value;
}

int16_t min16(int16_t value0, int16_t value1) {
	return (value0<=value1)?value0:value1;
}

int32_t min32(int32_t value0, int32_t value1) {
	return (value0<=value1)?value0:value1;
}

uint64_t nanoTime() {
	struct timespec ts;
	CNN(clock_gettime(CLOCK_MONOTONIC_RAW, &ts));
	return ts.tv_sec*1000000000+ts.tv_nsec;
}
