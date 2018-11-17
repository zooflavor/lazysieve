package gui.math;

import gui.util.DoubleMinQueue;

public interface Sum {
	abstract class AbstractSum implements Sum {
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
	
	class PrioritySum extends AbstractSum {
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
	
	class SimpleSum extends AbstractSum {
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
	
	void clear();
	
	static Sum priority() {
		return new PrioritySum();
	}
	
	static Sum simple() {
		return new SimpleSum();
	}
	
	//this is destructive
	double sum();
}
