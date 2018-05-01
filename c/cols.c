#include "main.h"

#define BUCKET_START(circle) ((circle)*((circle)+1)/2)
#define SEGMENT_LONGS (1l*SEGMENT_BYTES/8l)
#define SEGMENT_BITS (8l*SEGMENT_BYTES)
#define SEGMENT_NUMBERS (16l*SEGMENT_BYTES)

struct Offset {
	size_t offset;
	size_t prime;
};

size_t *buckets;
size_t *circles;
size_t circlesMax;
size_t *currentBuckets;
struct Offset *offsets;
size_t offsetsCount;
size_t offsetsLength;

void printBuckets();
void setBuckets(size_t circle, size_t lowerBucket, size_t lowerIndex,
		size_t upperBucket, size_t upperIndex);
void setCircles(size_t lower, size_t upper);

void initBuild() {
	if (0!=(SEGMENT_BYTES%8)) {
		printf("segment size must be a multiple of 8\n");
		exit(1);
	}
	if (0!=(length%SEGMENT_LONGS)) {
		printf("size must be a multiple of %ld\n", SEGMENT_NUMBERS);
		exit(1);
	}
	offsetsCount=0;
	offsetsLength=32*1024*1024;
	offsets=CMALLOC(offsetsLength*sizeof(struct Offset));
}

size_t findCircle(size_t lower, size_t upper, size_t min) {
	while (lower+1<upper) {
		int middle=(lower+upper)/2;
		if (min<=offsets[middle].prime) {
			upper=middle;
		}
		else {
			lower=middle;
		}
	}
	return upper;
}

void finishBuild() {
	if (0>=offsetsCount) {
		printf("no offsets\n");
		exit(1);
	}
	AZ(offsets[0].prime/SEGMENT_BITS);
	circlesMax=offsets[offsetsCount-1].prime/SEGMENT_BITS;
	buckets=CMALLOC((BUCKET_START(circlesMax+1)+1)*sizeof(size_t));
	circles=CMALLOC((circlesMax+2)*sizeof(size_t));
	currentBuckets=CMALLOC((circlesMax+1)*sizeof(size_t));
	circles[0]=0;
	circles[circlesMax+1]=offsetsCount;
	setCircles(0, circlesMax+1);
	buckets[BUCKET_START(circlesMax+1)]=circles[circlesMax+1];
	for (size_t cc=0; circlesMax>=cc; ++cc) {
		buckets[BUCKET_START(cc)]=circles[cc];
		currentBuckets[cc]=0;
		setBuckets(cc, 0, circles[cc], cc+1, circles[cc+1]);
	}
}

void nextPrime(size_t prime, size_t offset) {
	if (offsetsCount>=offsetsLength) {
		offsetsLength*=2;
		offsets=CREALLOC(offsets, offsetsLength*sizeof(struct Offset));
	}
	offsets[offsetsCount].offset=offset;
	offsets[offsetsCount].prime=prime;
	++offsetsCount;
}

void printBuckets() {
	printf("circlesMax %ld\n", circlesMax);
	for (size_t cc=0; circlesMax>=cc; ++cc) {
		printf("circle %4ld\t\tstart %6ld\tend %6ld\t%6ld primes\n",
				cc,
				circles[cc],
				circles[cc+1],
				circles[cc+1]-circles[cc]);
		for (size_t bb=0; cc>=bb; ++bb) {
			size_t indices[4];
			indices[0]=buckets[BUCKET_START(cc)+bb];
			indices[3]=(0==cc)?circles[1]:buckets[BUCKET_START(cc)+(bb+1)%(cc+1)];
			if (indices[0]<=indices[3]) {
				indices[1]=indices[3];
				indices[2]=indices[3];
			}
			else {
				indices[1]=circles[cc+1];
				indices[2]=circles[cc];
			}
			printf("\tbucket %4ld\tstart %6ld\tend %6ld\t%6ld primes\n",
					bb,
					indices[0],
					indices[3],
					indices[1]-indices[0]+indices[3]-indices[2]);
			for (int ii=0; 2>=ii; ii+=2) {
				for (size_t oo=indices[ii]; indices[ii+1]>oo; ++oo) {
					printf("\t\tprime %6ld - offset %6ld - circle %6ld - bucket %6ld\n",
							offsets[oo].prime,
							offsets[oo].offset,
							offsets[oo].prime/SEGMENT_BITS,
							offsets[oo].offset/SEGMENT_BITS);
				}
			}
		}
	}
	printf("circles\' end %6ld\n",
			circles[circlesMax+1]);
	printf("buckets\' end %6ld\n",
			buckets[BUCKET_START(circlesMax+1)]);
}

void setBuckets(size_t circle, size_t lowerBucket, size_t lowerIndex,
		size_t upperBucket, size_t upperIndex) {
	if (lowerBucket+1>=upperBucket) {
		return;
	}
	size_t mb=(lowerBucket+upperBucket)/2;
	size_t mo=mb*SEGMENT_BITS;
	int li=lowerIndex;
	int ui=upperIndex;
	while (li<ui) {
		if (mo>offsets[li].offset) {
			++li;
		}
		else if (mo<=offsets[ui-1].offset) {
			--ui;
		}
		else {
			--ui;
			struct Offset temp=offsets[li];
			offsets[li]=offsets[ui];
			offsets[ui]=temp;
			++li;
		}
	}
	buckets[BUCKET_START(circle)+mb]=ui;
	setBuckets(circle, lowerBucket, lowerIndex, mb, ui);
	setBuckets(circle, mb, ui, upperBucket, upperIndex);
}

void setCircles(size_t lower, size_t upper) {
	if (lower+1>=upper) {
		return;
	}
	size_t middle=(lower+upper)/2;
	circles[middle]=findCircle(
			circles[lower], circles[upper], middle*SEGMENT_BITS);
	setCircles(lower, middle);
	setCircles(middle, upper);
}

void sieve() {
	for (size_t segmentStart=0, segmentEnd=SEGMENT_BITS;
			size2>segmentStart; ) {
		printf("*** segment start %ld\n", segmentStart);
		printBuckets();
		for (size_t oo=circles[0], ee=circles[1]; ee>oo; ++oo) {
			size_t prime=offsets[oo].prime;
			size_t offset=offsets[oo].offset;
			while (segmentEnd>offset) {
				composites[offset/64]|=1l<<(offset%64);
				offset+=prime;
			}
			offsets[oo].offset=offset;
		}
		for (size_t cc=1; circlesMax>=cc; ++cc) {
			/*for (size_t oo=circles[cc], ee=circles[cc+1]; ee>oo; ++oo) {
				size_t prime=offsets[oo].prime;
				size_t offset=offsets[oo].offset;
				if (segmentEnd>offset) {
					composites[offset/64]|=1l<<(offset%64);
					offset+=prime;
					offsets[oo].offset=offset;
				}
			}*/
			size_t circleOffset=segmentStart+(cc+1)*SEGMENT_BITS;
			size_t circleBucket=BUCKET_START(cc);
			size_t bucket0=currentBuckets[cc];
			size_t bucket1=(bucket0+1)%(cc+1);
			currentBuckets[cc]=bucket1;
			size_t start=buckets[circleBucket+bucket0];
			size_t end=buckets[circleBucket+bucket1];
			if (start<=end) {
				for (size_t ii=start; end>ii; ++ii) {
					size_t prime=offsets[ii].prime;
					size_t offset=offsets[ii].offset;
					composites[offset/64]|=1l<<(offset%64);
					offset+=prime;
					if (offset>=circleOffset) {
						offsets[ii].offset=offset;
					}
					else {
						offsets[ii]=offsets[start];
						offsets[start].prime=prime;
						offsets[start].offset=offset;
						++start;
					}
				}
			}
			else {
				size_t limit0=circles[cc];
				size_t limit1=circles[cc+1];
				for (size_t ii=start; limit1>ii; ++ii) {
					size_t prime=offsets[ii].prime;
					size_t offset=offsets[ii].offset;
					composites[offset/64]|=1l<<(offset%64);
					offset+=prime;
					if (offset>=circleOffset) {
						offsets[ii].offset=offset;
					}
					else {
						offsets[ii]=offsets[start];
						offsets[start].prime=prime;
						offsets[start].offset=offset;
						++start;
					}
				}
				for (size_t ii=limit0; end>ii; ++ii) {
					size_t prime=offsets[ii].prime;
					size_t offset=offsets[ii].offset;
					composites[offset/64]|=1l<<(offset%64);
					offset+=prime;
					if (offset>=circleOffset) {
						offsets[ii].offset=offset;
					}
					else {
						offsets[ii]=offsets[start];
						offsets[start].prime=prime;
						offsets[start].offset=offset;
						++start;
						if (start>=limit1) {
							start=limit0;
						}
					}
				}
			}
			buckets[circleBucket+bucket0]=start;
		}
		segmentStart=segmentEnd;
		segmentEnd+=SEGMENT_BITS;
	}
	printBuckets();
}
