#include "main.h"

#define BUCKET_START(circle) ((circle)*((circle)+1)/2)
#define SEGMENT_LONGS (1l*SEGMENT_BYTES/8l)
#define SEGMENT_BITS (8l*SEGMENT_BYTES)
#define SEGMENT_NUMBERS (16l*SEGMENT_BYTES)

struct Offset {
	size_t offset;
	size_t prime;
};

size_t *brokenBuckets;
size_t *buckets;
size_t *circles;
size_t circlesMax;
size_t *currentBuckets;
struct Offset *offsets;
size_t offsetsCount;
size_t offsetsLength;

#ifdef MIDDLE_PRIMES
#include "middle.h"
#endif

void printBuckets(size_t segmentOffset);
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
#ifdef MIDDLE_PRIMES
	initMiddle();
#endif
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
	brokenBuckets=CMALLOC((circlesMax+1)*sizeof(size_t));
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
		brokenBuckets[cc]=cc;
		while (buckets[BUCKET_START(cc)+brokenBuckets[cc]]==circles[cc+1]) {
			buckets[BUCKET_START(cc)+brokenBuckets[cc]]=circles[cc];
			--brokenBuckets[cc];
		}
	}
#ifdef MIDDLE_PRIMES
	finishMiddle();
#endif
}

void nextPrime(size_t prime, size_t offset) {
#ifdef MIDDLE_PRIMES
	if (addMiddle(prime, offset))
#endif
	{
		if (offsetsCount>=offsetsLength) {
			offsetsLength*=2;
			offsets=CREALLOC(offsets, offsetsLength*sizeof(struct Offset));
		}
		offsets[offsetsCount].offset=offset;
		offsets[offsetsCount].prime=prime;
		++offsetsCount;
	}
}

void printBuckets(size_t segmentOffset) {
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
			indices[3]=buckets[BUCKET_START(cc)+(bb+1)%(cc+1)];
			if (bb==brokenBuckets[cc]) {
				indices[1]=circles[cc+1];
				indices[2]=circles[cc];
			}
			else {
				indices[1]=indices[3];
				indices[2]=indices[3];
			}
			printf("\tbucket%s %4ld\tstart %6ld\tend %6ld\t%6ld primes\n",
					(bb==brokenBuckets[cc])?"*":" ",
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
							(offsets[oo].offset-segmentOffset)/SEGMENT_BITS);
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
		for (size_t oo=circles[0], ee=circles[1]; ee>oo; ++oo) {
			size_t prime=offsets[oo].prime;
			size_t offset=offsets[oo].offset;
			while (segmentEnd>offset) {
				composites[offset/64]|=1l<<(offset%64);
				offset+=prime;
			}
			offsets[oo].offset=offset;
		}
#ifdef MIDDLE_PRIMES
		sieveMiddle(segmentEnd);
#endif
		for (size_t cc=1; circlesMax>=cc; ++cc) {
			size_t brokenBucket=brokenBuckets[cc];
			size_t bucket0=currentBuckets[cc];
			size_t bucket1=(bucket0+1)%(cc+1);
			currentBuckets[cc]=bucket1;
			size_t circleBucket=BUCKET_START(cc);
			size_t circleOffset=segmentStart+(cc+1)*SEGMENT_BITS;
			size_t indices[4];
			indices[0]=buckets[circleBucket+bucket0];
			indices[3]=buckets[circleBucket+bucket1];
			if (bucket0==brokenBucket) {
				indices[1]=circles[cc+1];
				indices[2]=circles[cc];
				for (int jj=0; 4>jj; jj+=2) {
					for (size_t ii=indices[jj], end=indices[jj+1];
							end>ii; ++ii) {
						size_t prime=offsets[ii].prime;
						size_t offset=offsets[ii].offset;
						composites[offset/64]|=1l<<(offset%64);
						offset+=prime;
						if (offset>=circleOffset) {
							offsets[ii].offset=offset;
						}
						else {
							offsets[ii]=offsets[indices[0]];
							offsets[indices[0]].prime=prime;
							offsets[indices[0]].offset=offset;
							++indices[0];
							if (indices[0]==indices[1]) {
								indices[0]=indices[2];
								brokenBuckets[cc]
										=(brokenBuckets[cc]+cc)%(cc+1);
							}
						}
					}
				}
			}
			else {
				for (size_t ii=indices[0], end=indices[3]; end>ii; ++ii) {
					size_t prime=offsets[ii].prime;
					size_t offset=offsets[ii].offset;
					composites[offset/64]|=1l<<(offset%64);
					offset+=prime;
					if (offset>=circleOffset) {
						offsets[ii].offset=offset;
					}
					else {
						offsets[ii]=offsets[indices[0]];
						offsets[indices[0]].prime=prime;
						offsets[indices[0]].offset=offset;
						++indices[0];
					}
				}
			}
			buckets[circleBucket+bucket0]=indices[0];
		}
		segmentStart=segmentEnd;
		segmentEnd+=SEGMENT_BITS;
	}
}
