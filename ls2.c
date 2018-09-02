#include <inttypes.h>
#include <stdio.h>
#include <stdlib.h>

struct List {
	struct List *next;
	uint64_t position;
	uint64_t prime;
};

struct List *lists[65];

struct List *allocateList() {
	struct List *result=malloc(sizeof(struct List));
	if (0==result) {
		printf("malloc error\n");
		exit(1);
	}
	return result;
}

void add(int index, struct List *list) {
	list->next=lists[index];
	lists[index]=list;
}

int bsr(uint64_t value) {
	int result=0;
	while (0l!=(value>>result)) {
		++result;
	}
	return result;
}

int main(int argc, char** argv) {
	for (int ii=0; 65>ii; ++ii) {
		lists[ii]=0;
	}
	for (uint64_t currentPosition=2l;
			100l>currentPosition;
			++currentPosition) {
		int index=bsr(currentPosition^(currentPosition-1));
		struct List *list=lists[index];
		lists[index]=0;
		int sieved=0;
		while (0!=list) {
			struct List *next=list;
			list=list->next;
			int index2=bsr(currentPosition^next->position);
			if (0==index2) {
				sieved=1;
				next->position+=next->prime;
				add(bsr(currentPosition^next->position), next);
			}
			else {
				add(index2, next);
			}
		}
		if (!sieved) {
			printf("%lu\n", currentPosition);
			list=allocateList();
			list->position=currentPosition*currentPosition;
			list->prime=currentPosition;
			add(bsr(currentPosition^list->position), list);
		}
	}
	return 0;
}
