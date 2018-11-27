package gui.math;

import gui.util.DoubleMinQueue;
import java.util.Arrays;

public interface Sum {
	abstract class Abstract implements Sum {
		@Override
		public void add(double value) {
			if (Double.isInfinite(value)) {
				throw new ArithmeticException("infinity");
			}
			if (Double.isNaN(value)) {
				throw new ArithmeticException("NaN");
			}
			if (0.0==value) {
				return;
			}
			addImpl(value);
		}
		
		protected abstract void addImpl(double value);
	}
	
	class Array extends Abstract {
		private final double[] values=new double[2048];
		
		@Override
		protected void addImpl(double value) {
			while (0.0!=value) {
				long bits=Double.doubleToRawLongBits(value);
				int index=((int)(bits>>>52))&0x7ff;
				double value0=values[index];
				if (0.0==value0) {
					values[index]=value;
					break;
				}
				value+=value0;
				values[index]=0.0;
			}
		}
		
		@Override
		public void clear() {
			Arrays.fill(values, 0.0);
		}
		
		@Override
		public double sum() {
			double result=0.0;
			for (int ii=0; values.length>ii; ++ii) {
				result+=values[ii];
			}
			return result;
		}
	}
	
	class Priority extends Abstract {
		private final DoubleMinQueue queue=new DoubleMinQueue(16) {
			@Override
			protected int compare(int index0, int index1) {
				return Double.compare(
						Math.abs(heap[index0]),
						Math.abs(heap[index1]));
			}
		};
		
		@Override
		protected void addImpl(double value) {
			queue.add(value);
		}

		@Override
		public void clear() {
			queue.clear();
		}

		@Override
		public double sum() {
			while (1<queue.size()) {
				double sum=queue.remove()+queue.peek();
				if (0.0==sum) {
					queue.remove();
				}
				else {
					queue.replace(sum);
				}
			}
			if (queue.isEmpty()) {
				return 0.0;
			}
			return queue.peek();
		}
	}
	
	class Simple extends Abstract {
		private double sum;
		
		@Override
		protected void addImpl(double value) {
			sum+=value;
		}
		
		@Override
		public void clear() {
			sum=0.0;
		}
		
		@Override
		public double sum() {
			return sum;
		}
	}
	
	void add(double value);
	
	static Sum array() {
		return new Array();
	}
	
	void clear();
	
	static Sum preferred() {
		return priority();
	}
	
	static Sum priority() {
		return new Priority();
	}
	
	static Sum simple() {
		return new Simple();
	}
	
	//this is destructive
	double sum();
}
