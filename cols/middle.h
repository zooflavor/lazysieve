#define LEFT_CHILD(ii) ((ii<<1)|1)

struct Middle {
	size_t subOffset;
	size_t offset;
	size_t prime;
};

struct Middle *middle;
size_t middleBranches;
size_t middleCount;
size_t middleLength;

int addMiddle(size_t prime, size_t offset);
void initMiddle();
void finishMiddle();
size_t moveMiddle(size_t index, size_t minOffset);
void printMiddle(char *indent, size_t index);
void sieveMiddle(size_t segmentEnd);

int addMiddle(size_t prime, size_t offset) {
	if (MIDDLE_PRIMES*prime<SEGMENT_BITS) {
		return 1;
	}
	if (middleCount>=middleLength) {
		middleLength*=2;
		middle=CREALLOC(middle, middleLength*sizeof(struct Middle));
	}
	middle[middleCount].offset=offset;
	middle[middleCount].prime=prime;
	++middleCount;
	return 0;
}

void initMiddle() {
	middleCount=0;
	middleLength=32*1024;
	middle=CMALLOC(middleLength*sizeof(struct Middle));
}

void finishMiddle() {
	if (0>=middleCount) {
		printf("no middle primes\n");
		exit(1);
	}
	size_t level=0;
	for (size_t ii=middleCount; 0<ii; ii/=2) {
		++level;
	}
	if (0!=(((1<<level)-1-middleCount)%2)) {
		if (middleCount>=middleLength) {
			middleLength*=2;
			middle=CREALLOC(middle, middleLength*sizeof(struct Middle));
		}
		middle[middleCount].offset=size;
		middle[middleCount].prime=size;
		++middleCount;
	}
	middleBranches=middleCount/2;
	for (ptrdiff_t ii=middleCount-1; (ptrdiff_t)middleBranches<=ii; --ii) {
		middle[ii].subOffset=middle[ii].offset;
	}
	for (ptrdiff_t ii=middleBranches-1; 0<=ii; --ii) {
		size_t min=middle[ii].offset;
		size_t left=middle[LEFT_CHILD(ii)].subOffset;
		if (left<min) {
			min=left;
		}
		size_t right=middle[LEFT_CHILD(ii)+1].subOffset;
		if (right<min) {
			min=right;
		}
		middle[ii].subOffset=min;
	}
}

size_t moveMiddle(size_t index, size_t minOffset) {
	size_t subOffset=middle[index].subOffset;
	if (subOffset<minOffset) {
		if (index<middleBranches) {
			subOffset=middle[index].offset;
			if (subOffset<minOffset) {
				size_t prime=middle[index].prime;
				do {
					subOffset+=prime;
				} while (subOffset<minOffset);
			}
			if (subOffset>minOffset) {
				int leftChild=LEFT_CHILD(index);
				size_t left=moveMiddle(leftChild, minOffset);
				if (left<subOffset) {
					subOffset=left;
				}
				if (subOffset>minOffset) {
					size_t right=moveMiddle(leftChild+1, minOffset);
					if (right<subOffset) {
						subOffset=right;
					}
				}
			}
		}
		else {
			size_t prime=middle[index].prime;
			do {
				subOffset+=prime;
			} while (subOffset<minOffset);
		}
		middle[index].subOffset=subOffset;
	}
	return subOffset;
}

void printMiddle(char *indent, size_t index) {
	if (index<middleBranches) {
		printf("%sbranch %ld\tprime %ld\toffset %ld\tsub %ld\n",
				indent,
				index,
				middle[index].prime,
				middle[index].offset,
				middle[index].subOffset);
		char indent2[256];
		sprintf(indent2, "%s\t", indent);
		printMiddle(indent2, LEFT_CHILD(index));
		printMiddle(indent2, LEFT_CHILD(index)+1);
	}
	else {
		printf("%sleaf %ld\tprime %ld\toffset %ld\n",
				indent,
				index,
				middle[index].prime,
				middle[index].subOffset);
	}
}

void sieveMiddle(size_t segmentEnd) {
	for (size_t offset=middle[0].subOffset; offset<segmentEnd; ) {
		size_t index=offset/64l;
		uint64_t bits=composites[index];
		for (size_t bitsEnd=(index+1l)*64l;
				offset<bitsEnd;
				offset=moveMiddle(0, offset+1l)) {
			bits|=1l<<(offset%64);
		}
		composites[index]=bits;
	}
}