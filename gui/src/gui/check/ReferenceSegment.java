package gui.check;

import gui.io.PrimeConsumer;
import gui.io.PrimeProducer;
import gui.io.Segment;
import gui.math.UnsignedLong;
import gui.ui.progress.Progress;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public abstract class ReferenceSegment {
	public static List<ReferenceSegment> REFERENCE_SEGMENTS;
	
	public static final ReferenceSegment SIEVE=new ReferenceSegment() {
		@Override
		public void generate(PrimeProducer primeProducer, Progress progress,
				Segment segment) throws Throwable {
			long max=UnsignedLong.squareRootFloor(segment.segmentEnd);
			PrimeConsumer sieve=(prime)->{
				long position=UnsignedLong.firstSievePosition(
						prime, segment.segmentStart);
				if (0>Long.compare(position, segment.segmentEnd)) {
					int bitIndex=segment.bitIndex(position);
					if (prime<=Segment.BITS) {
						for (; Segment.BITS>bitIndex; bitIndex+=prime) {
							segment.setComposite(bitIndex);
						}
					}
					else {
						segment.setComposite(bitIndex);
					}
				}
			};
			primeProducer.primes(sieve, max, progress);
		}
		
		@Override
		public String toString() {
			return "Eratoszthenész szitája";
		}
	};
	
	public static final ReferenceSegment TEST=new ReferenceSegment() {
		private final List<Long> BASE=Collections.unmodifiableList(
				Arrays.asList(
						2l,
						325l,
						9375l,
						28178l,
						450775l,
						9780504l,
						1795265022l));
		private final List<Long> PRIMES=Collections.unmodifiableList(
				Arrays.asList(
						3l,
						5l,
						13l,
						19l,
						73l,
						193l,
						407521l,
						299210837l));

		@Override
		public void generate(PrimeProducer primeProducer, Progress progress,
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
		
		@Override
		public String toString() {
			return "Erős pszeudo-prím teszt";
		}
	};
	
	static {
		try {
			Set<ReferenceSegment> referenceSegments=new TreeSet<>(
					(rs0, rs1)->rs0.toString().compareTo(rs1.toString()));
			int modifiers=Modifier.FINAL|Modifier.PUBLIC|Modifier.STATIC;
			for (Field field: ReferenceSegment.class.getDeclaredFields()) {
				if ((field.getModifiers()==modifiers)
						&& ReferenceSegment.class.equals(field.getType())) {
					ReferenceSegment referenceSegment
							=(ReferenceSegment)field.get(null);
					referenceSegments.add(referenceSegment);
				}
			}
			REFERENCE_SEGMENTS=Collections.unmodifiableList(
					new ArrayList<>(referenceSegments));
		}
		catch (IllegalAccessException
				|IllegalArgumentException
				|SecurityException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public abstract void generate(PrimeProducer primeProducer,
			Progress progress, Segment segment) throws Throwable;
	
	public static ReferenceSegment parse(String argument) {
		switch (argument.toLowerCase()) {
			case "sieve":
				return SIEVE;
			case "test":
				return TEST;
			default:
				throw new IllegalArgumentException(argument);
		}
	}
}
