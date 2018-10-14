package gui.check;

import gui.io.PrimesProducer;
import gui.io.Segment;
import gui.math.UnsignedLong;
import gui.ui.progress.Progress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestProcess extends CheckProcess {
	private static final List<Long> BASE=Collections.unmodifiableList(
			Arrays.asList(
					2l, 325l, 9375l, 28178l, 450775l, 9780504l, 1795265022l));
	private static final List<Long> PRIMES=Collections.unmodifiableList(
			Arrays.asList(3l, 5l, 13l, 19l, 73l, 193l, 407521l, 299210837l));
	
	public TestProcess(CheckSegments parent, List<Segment.Info> segmentInfos,
			int[] selectedRows) {
		super(parent, segmentInfos, selectedRows);
	}
	
	@Override
	protected void generate(PrimesProducer primesProducer, Progress progress,
			Segment segment) throws Throwable {
		bitIndices: for (int bitIndex=(1l==segment.segmentStart)?1:0;
				Segment.BITS>bitIndex;
				++bitIndex) {
			progress.progress(1.0*bitIndex/Segment.BITS);
			long number=segment.number(bitIndex);
			for (int ii=0; PRIMES.size()>ii; ++ii) {
				long prime=PRIMES.get(ii);
				if (0l==Long.remainderUnsigned(number, prime)) {
					if (number!=prime) {
						segment.setComposite(bitIndex);
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
				long xx=UnsignedLong.moduloExponentiation(
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
					xx=UnsignedLong.moduloMultiplication(number, xx, xx);
				}
				if (!prime) {
					segment.setComposite(bitIndex);
					continue bitIndices;
				}
			}
		}
		progress.finished();
	}
}
