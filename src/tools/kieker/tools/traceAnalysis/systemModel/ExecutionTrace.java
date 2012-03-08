/***************************************************************************
 * Copyright 2011 by
 *  + Christian-Albrechts-University of Kiel
 *    + Department of Computer Science
 *      + Software Engineering Group 
 *  and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package kieker.tools.traceAnalysis.systemModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

import kieker.tools.traceAnalysis.plugins.traceReconstruction.InvalidTraceException;
import kieker.tools.util.LoggingTimestampConverter;

/**
 * @author Andre van Hoorn
 */
public class ExecutionTrace extends AbstractTrace {

	// private static final Log LOG = LogFactory.getLog(ExecutionTrace.class);
	private final AtomicReference<MessageTrace> messageTrace = new AtomicReference<MessageTrace>();
	private int minEoi = -1;
	private int maxEoi = -1;
	private long minTin = -1;
	private long maxTout = -1;
	private int maxEss = -1;
	private final SortedSet<Execution> set = new TreeSet<Execution>(new Comparator<Execution>() {

		@Override
		public int compare(final Execution e1, final Execution e2) {
			if (e1.getTraceId() == e2.getTraceId()) {
				if (e1.getEoi() < e2.getEoi()) {
					return -1;
				}
				if (e1.getEoi() > e2.getEoi()) {
					return 1;
				}
				return 0;
			} else {
				// TODO: Should never happen, as #add makes sure that all trace ids equal
				if (e1.getTin() < e2.getTin()) {
					return -1;
				}
				if (e1.getTin() > e2.getTin()) {
					return 1;
				}
				return 0;
			}
		}
	});

	public ExecutionTrace(final long traceId) {
		super(traceId);
	}

	/**
	 * Adds an execution to the trace.
	 * 
	 * @param execution
	 * @throws InvalidTraceException
	 *             if the traceId of the passed Execution
	 *             object is not the same as the traceId of this ExecutionTrace object.
	 */
	public void add(final Execution execution) throws InvalidTraceException {
		synchronized (this) {
			if (this.getTraceId() != execution.getTraceId()) {
				throw new InvalidTraceException("TraceId of new record (" + execution.getTraceId() + ") differs from Id of this trace (" + this.getTraceId() + ")");
			}
			if ((this.minTin < 0) || (execution.getTin() < this.minTin)) {
				this.minTin = execution.getTin();
			}
			if ((this.maxTout < 0) || (execution.getTout() > this.maxTout)) {
				this.maxTout = execution.getTout();
			}
			if ((this.minEoi < 0) || (execution.getEoi() < this.minEoi)) {
				this.minEoi = execution.getEoi();
			}
			if ((this.maxEoi < 0) || (execution.getEoi() > this.maxEoi)) {
				this.maxEoi = execution.getEoi();
			}
			if (execution.getEss() > this.maxEss) {
				this.maxEss = execution.getEss();
			}
			this.set.add(execution);
			/* Invalidate the current message trace representation */
			this.messageTrace.set(null);
		}
	}

	/**
	 * Returns the message trace representation for this trace.
	 * 
	 * The transformation to a message trace is only computed during the
	 * first execution of this method. After this, the stored reference
	 * is returned --- unless executions are added to the trace afterwards.
	 */
	public MessageTrace toMessageTrace(final Execution rootExecution) throws InvalidTraceException {
		synchronized (this) {
			MessageTrace mt = this.messageTrace.get();
			if (mt != null) {
				return mt;
			}

			final List<AbstractMessage> mSeq = new ArrayList<AbstractMessage>();
			final Stack<AbstractMessage> curStack = new Stack<AbstractMessage>();
			final Iterator<Execution> eSeqIt = this.set.iterator();

			Execution prevE = rootExecution;
			int prevEoi = -1;
			boolean expectingEntryCall = true; // used to make that entry call found in first iteration
			while (eSeqIt.hasNext()) {
				final Execution curE = eSeqIt.next();
				if (expectingEntryCall && (curE.getEss() != 0)) {
					final InvalidTraceException ex = new InvalidTraceException("First execution must have ess " + "0 (found " + curE.getEss() // NOPMD (new in loop)
							+ ")\n Causing execution: " + curE);
					// don't log and throw
					// ExecutionTrace.LOG.error("Found invalid trace:" + ex.getMessage()); // don't need the stack trace here
					throw ex;
				}
				expectingEntryCall = false; // now we're happy
				if (prevEoi != (curE.getEoi() - 1)) {
					final InvalidTraceException ex = new InvalidTraceException("Eois must increment by 1 --" + "but found sequence <" + prevEoi // NOPMD (new in
																																				// loop)
							+ "," + curE.getEoi() + ">" + "(Execution: " + curE + ")");
					// don't log and throw
					// ExecutionTrace.LOG.error("Found invalid trace:" + ex.getMessage()); // don't need the stack trace here
					throw ex;
				}
				prevEoi = curE.getEoi();

				// First, we might need to clean up the stack for the next execution callMessage
				if ((!prevE.equals(rootExecution)) && (prevE.getEss() >= curE.getEss())) {
					Execution curReturnReceiver; // receiverComponentName of return message
					while (curStack.size() > curE.getEss()) {
						final AbstractMessage poppedCall = curStack.pop();
						prevE = poppedCall.getReceivingExecution();
						curReturnReceiver = poppedCall.getSendingExecution();
						final AbstractMessage m = new SynchronousReplyMessage(prevE.getTout(), prevE, curReturnReceiver); // NOPMD (new in loop)
						mSeq.add(m);
						prevE = curReturnReceiver;
					}
				}
				// Now, we handle the current execution callMessage
				if (prevE.equals(rootExecution)) { // initial execution callMessage
					final AbstractMessage m = new SynchronousCallMessage(curE.getTin(), rootExecution, curE); // NOPMD (new in loop)
					mSeq.add(m);
					curStack.push(m);
				} else if ((prevE.getEss() + 1) == curE.getEss()) { // usual callMessage with senderComponentName and receiverComponentName
					final AbstractMessage m = new SynchronousCallMessage(curE.getTin(), prevE, curE); // NOPMD (new in loop)
					mSeq.add(m);
					curStack.push(m);
				} else if (prevE.getEss() < curE.getEss()) { // detect ess incrementation by > 1
					final InvalidTraceException ex = new InvalidTraceException("Ess are only allowed to increment by 1 --" // NOPMD (new in loop)
							+ "but found sequence <" + prevE.getEss() + "," + curE.getEss() + ">" + "(Execution: " + curE + ")");
					// don't log and throw
					// ExecutionTrace.LOG.error("Found invalid trace:" + ex.getMessage()); // don't need the stack trace here
					throw ex;
				}
				if (!eSeqIt.hasNext()) { // empty stack completely, since no more executions
					Execution curReturnReceiver; // receiverComponentName of return message
					while (!curStack.empty()) {
						final AbstractMessage poppedCall = curStack.pop();
						prevE = poppedCall.getReceivingExecution();
						curReturnReceiver = poppedCall.getSendingExecution();
						final AbstractMessage m = new SynchronousReplyMessage(prevE.getTout(), prevE, curReturnReceiver); // NOPMD (new in loop)
						mSeq.add(m);
						prevE = curReturnReceiver;
					}
				}
				prevE = curE; // prepair next loop
			}
			mt = new MessageTrace(this.getTraceId(), mSeq);
			this.messageTrace.set(mt);
			return mt;
		}
	}

	/**
	 * TODO: It's not a good idea to return the internal data structure.
	 * See ticket http://samoa.informatik.uni-kiel.de:8000/kieker/ticket/152
	 * 
	 * @return the sorted set of {@link Execution}s in this trace
	 */
	public final SortedSet<Execution> getTraceAsSortedExecutionSet() {
		synchronized (this) {
			return this.set;
		}
	}

	/**
	 * Returns the length of this trace in terms of the number of contained
	 * executions.
	 * 
	 * @return the length of this trace.
	 */
	public final int getLength() {
		synchronized (this) {
			return this.set.size();
		}
	}

	@Override
	public String toString() {
		final StringBuilder strBuild = new StringBuilder();
		synchronized (this) {
			strBuild.append("TraceId ").append(this.getTraceId());
			strBuild.append(" (minTin=").append(this.minTin);
			strBuild.append(" (").append(LoggingTimestampConverter.convertLoggingTimestampToUTCString(this.minTin)).append(")");
			strBuild.append("; maxTout=").append(this.maxTout);
			strBuild.append(" (").append(LoggingTimestampConverter.convertLoggingTimestampToUTCString(this.maxTout)).append(")");
			strBuild.append("; maxEss=").append(this.maxEss).append("):\n");
			for (final Execution e : this.set) {
				strBuild.append("<");
				strBuild.append(e.toString()).append(">\n");
			}
		}
		return strBuild.toString();
	}

	/**
	 * Returns the maximum execution stack size (ess) value, i.e., the maximum
	 * stack depth, within the trace.
	 * 
	 * @return the maximum ess; -1 if the trace contains no executions.
	 */
	public int getMaxEss() {
		synchronized (this) {
			return this.maxEss;
		}
	}

	/**
	 * Returns the maximum execution order index (eoi) value within the trace.
	 * 
	 * @return the maximum eoi; -1 if the trace contains no executions.
	 */
	public int getMaxEoi() {
		synchronized (this) {
			return this.maxEoi;
		}
	}

	/**
	 * Returns the minimum execution order index (eoi) value within the trace.
	 * 
	 * @return the minimum eoi; -1 if the trace contains no executions.
	 */
	public int getMinEoi() {
		synchronized (this) {
			return this.minEoi;
		}
	}

	/**
	 * Returns the duration of this (possible incomplete) trace in nanoseconds.
	 * This value is the difference between the maximum tout and the minimum
	 * tin value.
	 * 
	 * @return the duration of this trace in nanoseconds.
	 */
	public long getDurationInNanos() {
		synchronized (this) {
			return this.getMaxTout() - this.minTin;
		}
	}

	/**
	 * Returns the maximum timestamp value of an execution return in this trace.
	 * 
	 * Notice that you should need use this value to reason about the
	 * control flow --- particularly in distributed scenarios.
	 * 
	 * @return the maxmum timestamp value; -1 if the trace contains no executions.
	 */
	public long getMaxTout() {
		synchronized (this) {
			return this.maxTout;
		}
	}

	/**
	 * Returns the minimum timestamp of an execution start in this trace.
	 * 
	 * Notice that you should need use this value to reason about the
	 * control flow --- particularly in distributed scenarios.
	 * 
	 * @return the minimum timestamp value; -1 if the trace contains no executions.
	 */
	public long getMinTin() {
		synchronized (this) {
			return this.minTin;
		}
	}

	// Explicit delegation to super method to make FindBugs happy
	@Override
	public int hashCode() { // NOPMD
		return super.hashCode();
	}

	/**
	 * Returns whether this Execution Trace and the passed Object are equal.
	 * Two execution traces are equal if the set of contained executions is
	 * equal.
	 * 
	 * @param obj
	 * @return true if the two objects are equal.
	 */
	@Override
	public boolean equals(final Object obj) {
		synchronized (this) {
			if (!(obj instanceof ExecutionTrace)) {
				return false;
			}
			if (this == obj) {
				return true;
			}
			final ExecutionTrace other = (ExecutionTrace) obj;
			return this.set.equals(other.set);
		}
	}
}
