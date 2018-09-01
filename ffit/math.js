function gaussianElimination(aa, bb, totalPivoting) {
	var sizeA=aa.length;
	if (sizeA!==aa[0].length) {
		throw "gaussianElimination: aa.length!==aa[0].length";
	}
	if (sizeA!==bb.length) {
		throw "gaussianElimination: aa.length!==bb.length";
	}
	var sizeB=bb[0].length;
	aa=matrixCopy(aa);
	bb=matrixCopy(bb);
	var columnSwaps=[];
	for (var ii=0; sizeA>ii; ++ii) {
		columnSwaps.push(ii);
	}
	for (var ii=0; sizeA-1>ii; ++ii) {
		var maxColumnsEnd=totalPivoting?sizeA:ii+1;
		var maxColumn=ii;
		var maxRow=ii;
		for (var rr=ii; sizeA>rr; ++rr) {
			for (var cc=ii; maxColumnsEnd>cc; ++cc) {
				if (Math.abs(aa[maxRow][maxColumn])<Math.abs(aa[rr][cc])) {
					maxColumn=cc;
					maxRow=rr;
				}
			}
		}
		if (maxColumn!==ii) {
			swap(columnSwaps, ii, maxColumn);
			matrixSwapColumns(aa, ii, maxColumn);
		}
		if (maxRow!==ii) {
			matrixSwapRows(aa, ii, maxRow);
			matrixSwapRows(bb, ii, maxRow);
		}
		if (0.0===aa[ii][ii]) {
			throw "gaussianElimination: 0.0===aa[ii][ii]";
		}
		for (var rr=ii+1; sizeA>rr; ++rr) {
			if (0.0!==aa[rr][ii]) {
				var factor=-aa[rr][ii]/aa[ii][ii];
				matrixAddMulRow(aa, ii, rr, factor);
				matrixAddMulRow(bb, ii, rr, factor);
				aa[rr][ii]=0.0;
			}
		}
	}
	for (var ii=sizeA-1; 0<=ii; --ii) {
		if (0.0===aa[ii][ii]) {
			throw "gaussianElimination: 0.0===aa[ii][ii]";
		}
		for (var rr=ii-1; 0<=rr; --rr) {
			if (0.0!==aa[rr][ii]) {
				var factor=-aa[rr][ii]/aa[ii][ii];
				matrixAddMulRow(aa, ii, rr, factor);
				matrixAddMulRow(bb, ii, rr, factor);
				aa[rr][ii]=0.0;
			}
		}
		var divisor=aa[ii][ii];
		for (var cc=sizeA-1; 0<=cc; --cc) {
			aa[ii][cc]/=divisor;
		}
		for (var cc=sizeB-1; 0<=cc; --cc) {
			bb[ii][cc]/=divisor;
		}
	}
	for (var ii=0; sizeA>ii; ++ii) {
		while (columnSwaps[ii]!==ii) {
			matrixSwapRows(bb, ii, columnSwaps[ii]);
			swap(columnSwaps, ii, columnSwaps[ii]);
		}
	}
	return bb;
}

function matrixAddMulRow(matrix, from, to, factor) {
	from=matrix[from];
	to=matrix[to];
	for (var cc=from.length-1; 0<=cc; --cc) {
		to[cc]+=factor*from[cc];
	}
}

function matrixCopy(matrix) {
	var result=[];
	for (var ii=0; matrix.length>ii; ++ii) {
		result.push(matrixCopyRow(matrix[ii]));
	}
	return result;
}

function matrixCopyRow(row) {
	var result=[];
	for (var ii=0; row.length>ii; ++ii) {
		result.push(row[ii]);
	}
	return result;
}

function matrixCreate(rows, columns) {
	var result=[];
	for (var rr=rows; 0<rr; --rr) {
		var row=[];
		for (var cc=columns; 0<cc; --cc) {
			row.push(0.0);
		}
		result.push(row);
	}
	return result;
}

function matrixIdentity(size) {
	var result=matrixCreate(size, size);
	for (var ii=size-1; 0<=ii; --ii) {
		result[ii][ii]=1.0;
	}
	return result;
}

function matrixInvert(matrix, totalPivoting) {
	return gaussianElimination(
			matrix,
			matrixIdentity(matrix.length),
			totalPivoting);
}

function matrixMake(rows, columns, ...values) {
	var result=matrixCreate(rows, columns);
	for (var rr=0; rows>rr; ++rr) {
		for (var cc=0; columns>cc; ++cc) {
			result[rr][cc]=values[rr*columns+cc];
		}
	}
	return result;
}

function matrixMultiply(matrix0, matrix1) {
	if (matrix0[0].length!==matrix1.length) {
		throw "matrixMultiply: matrix[0].length!==matrix1.length";
	}
	var rows=matrix0.length;
	var columns=matrix1[0].length;
	var common=matrix1.length;
	var result=matrixCreate(rows, columns);
	for (var rr=rows-1; 0<=rr; --rr) {
		for (var cc=columns-1; 0<=cc; --cc) {
			var sum2=[];
			for (var dd=common-1; 0<=dd; --dd) {
				sum2.push(matrix0[rr][dd]*matrix1[dd][cc]);
			}
			result[rr][cc]=sum(sum2);
		}
	}
	return result;
}

function matrixSwapColumns(matrix, column0, column1) {
	if (column0!==column1) {
		for (var rr=matrix.length-1; 0<=rr; --rr) {
			swap(matrix[rr], column0, column1);
		}
	}
}

function matrixSwapRows(matrix, row0, row1) {
	swap(matrix, row0, row1);
}

function matrixTranspose(matrix) {
	var rows=matrix.length;
	var columns=matrix[0].length;
	var result=matrixCreate(columns, rows);
	for (var rr=rows-1; 0<=rr; --rr) {
		for (var cc=columns-1; 0<=cc; --cc) {
			result[cc][rr]=matrix[rr][cc];
		}
	}
	return result;
}

function regression(functions, sample) {
	var xx=matrixCreate(sample.length, functions.length);
	var yy=matrixCreate(sample.length, 1);
	for (var rr=sample.length-1; 0<=rr; --rr) {
		yy[rr][0]=sample[rr].yy;
		for (var cc=functions.length-1; 0<=cc; --cc) {
			xx[rr][cc]=functions[cc](sample[rr].xx);
		}
	}
	var xxt=matrixTranspose(xx);
	return gaussianElimination(
			matrixMultiply(xxt, xx),
			matrixMultiply(xxt, yy),
			true);
}

function sum(array) {
	if (0>=array.length) {
		return 0.0;
	}
	if (1===array.length) {
		return array[0];
	}
    if (2===array.length) {
		return array[0]+array[1];
    }
    var heap=[];
	var length=heap.length;
    for (var ii=array.length-1; 0<=ii; --ii) {
        if (0.0!==array[ii]) {
			heap.push(0.0);
			sumHeapAdd(heap, length, array[ii]);
			++length;
        }
    }
	if (0>=length) {
		return 0.0;
	}
	if (1===length) {
		return heap[0];
	}
	while (1<length) {
		var min0=sumHeapPoll(heap, length);
		--length;
		var min1=sumHeapPoll(heap, length);
		--length;
		sumHeapAdd(heap, length, min0+min1);
		++length;
	}
	return heap[0];
}

function sumHeapAdd(heap, length, value) {
	var index=length;
	heap[index]=value;
	while (0<index) {
		var parent=sumHeapParent(index);
		if (Math.abs(heap[parent])<=Math.abs(heap[index])) {
			break;
		}
		swap(heap, parent, index);
		index=parent;
	}
}

function sumHeapParent(index) {
	return (index-1)>>1;
}

function sumHeapPoll(heap, length) {
	var result=heap[0];
	--length;
	heap[0]=heap[length];
	var index=0;
	while (true) {
		var leftChild=sumHeapLeftChild(index);
		if (length<=leftChild) {
			break;
		}
		var rightChild=sumHeapRightChild(index);
		if (length<=rightChild) {
			if (Math.abs(heap[index])<=Math.abs(heap[leftChild])) {
				break;
			}
			swap(heap, index, leftChild);
			index=leftChild;
		}
		else {
			if (Math.abs(heap[index])<=Math.abs(heap[leftChild])) {
				if (Math.abs(heap[index])<=Math.abs(heap[rightChild])) {
					break;
				}
				else {
					swap(heap, index, rightChild);
					index=rightChild;
				}
			}
			else {
				if (Math.abs(heap[index])<=Math.abs(heap[rightChild])) {
					//l<p<=r
					swap(heap, index, leftChild);
					index=leftChild;
				}
				else {
					if (Math.abs(heap[leftChild])
							<=Math.abs(heap[rightChild])) {
						swap(heap, index, leftChild);
						index=leftChild;
					}
					else {
						swap(heap, index, rightChild);
						index=rightChild;
					}
				}
			}
		}
	}
	return result;
}

function sumHeapLeftChild(index) {
	return (index<<1)+1;
}

function sumHeapRightChild(index) {
	return (index+1)<<1;
}

function sumSelect(array, length) {
	var result=length-1;
    var magnitude=Math.abs(array[result]);
	for (var ii=result-1; 0<=ii; --ii) {
        var im=Math.abs(array[ii]);
		if (magnitude>im) {
			result=ii;
            magnitude=im;
		}
	}
	return result;
}

function swap(array, index0, index1) {
	if (index0!==index1) {
		var tt=array[index0];
		array[index0]=array[index1];
		array[index1]=tt;
	}
}
