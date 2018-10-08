package gui.check;

import gui.io.Segment;
import gui.math.UnsignedLongMath;
import gui.ui.progress.Progress;
import gui.util.LongList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

public class TestProcess extends CheckProcess {
	private static final List<Long> BASE=Collections.unmodifiableList(
			Arrays.asList(
					2l, 325l, 9375l, 28178l, 450775l, 9780504l, 1795265022l));
	
	private List<Long> primes;
	
	public TestProcess(CheckSegments parent, List<Segment.Info> segmentInfos,
			int[] selectedRows) {
		super(parent, segmentInfos, selectedRows);
	}
	
	private void createPrimes(Progress progress) throws Throwable {
		if (null!=primes) {
			return;
		}
		List<Long> base=new ArrayList<>(BASE);
		long max=base.get(0);
		for (long base2: base) {
			max=Math.max(max, base2);
		}
		max=UnsignedLongMath.squareRootFloor(max);
		LongList primes2=new LongList();
		NavigableSet<Long> primes3=new TreeSet<>();
		for (long ii=2; max>=ii; ++ii) {
			progress.progress(1.0*ii/(max+1l));
			if (base.isEmpty()) {
				break;
			}
			boolean prime=true;
			for (int jj=0; primes2.size()>jj; ++jj) {
				if (0l==(ii%primes2.get(jj))) {
					prime=false;
					break;
				}
			}
			if (!prime) {
				continue;
			}
			primes2.add(ii);
			for (int jj=base.size()-1; 0<=jj; --jj) {
				while (0l==(base.get(jj)%ii)) {
					base.set(jj, base.get(jj)/ii);
					primes3.add(ii);
				}
				if (1l==base.get(jj)) {
					base.remove(jj);
				}
			}
		}
		primes3.addAll(base);
		primes3.remove(2l);
		primes=new ArrayList<>(primes3.descendingSet());
		progress.finished();
	}
	
	@Override
	protected void generate(CheckProcess.Primes primes, Progress progress,
			Segment segment) throws Throwable {
		createPrimes(progress.subProgress(0.0, "create primes", 0.05));
		testNumbers(progress.subProgress(0.05, "test numbers", 1.0), segment);
	}
	
	private void testNumbers(Progress progress, Segment segment)
			throws Throwable {
		bitIndices: for (int bitIndex=(1l==segment.segmentStart)?1:0;
				Segment.BITS>bitIndex;
				++bitIndex) {
			progress.progress(1.0*bitIndex/Segment.BITS);
			long number=segment.number(bitIndex);
			for (int ii=primes.size()-1; 0<=ii; --ii) {
				long prime=primes.get(ii);
				if (0l==Long.remainderUnsigned(number, prime)) {
					if (number!=prime) {
						segment.setNotPrime(bitIndex);
					}
					continue bitIndices;
				}
			}
			long dd=number-1;
			int ss=0;
			while (0l==(dd&1l)) {
				dd>>>=1;
				++ss;
			}
			for (int ii=BASE.size()-1; 0<=ii; --ii) {
				long base=BASE.get(ii);
				long xx=UnsignedLongMath.moduloExponentiation(
						base, dd, number);
				if (1l==xx) {
					continue;
				}
				boolean prime=false;
				for (int rr=0; ss>rr; ++rr) {
					if (number-1l==xx) {
						prime=true;
						break;
					}
					xx=UnsignedLongMath.moduloMultiplication(number, xx, xx);
				}
				if (!prime) {
					segment.setNotPrime(bitIndex);
					continue bitIndices;
				}
			}
		}
		progress.finished();
	}
}
