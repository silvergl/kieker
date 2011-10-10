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

package kieker.tools.traceAnalysis.plugins.executionFilter;

import java.util.Set;

import kieker.analysis.plugin.IAnalysisPlugin;
import kieker.analysis.plugin.configuration.AbstractInputPort;
import kieker.analysis.plugin.configuration.IInputPort;
import kieker.analysis.plugin.configuration.IOutputPort;
import kieker.analysis.plugin.configuration.OutputPort;
import kieker.tools.traceAnalysis.systemModel.Execution;

/**
 * Allows to filter Execution objects based on their traceId.
 * 
 * @author Andre van Hoorn
 */
public class TraceIdFilter implements IAnalysisPlugin {

	private final Set<Long> selectedTraces;

	private final OutputPort<Execution> executionOutputPort = new OutputPort<Execution>("Execution output");

	/**
	 * Creates a filter instance that only passes Execution objects <i>e</i>
	 * whose traceId (<i>e.traceId</i>) is element of the set <i>selectedTraces</i>.
	 * 
	 * @param selectedTraces
	 */
	public TraceIdFilter(final Set<Long> selectedTraces) {
		this.selectedTraces = selectedTraces;
	}

	public IInputPort<Execution> getExecutionInputPort() {
		return this.executionInputPort;
	}

	private final IInputPort<Execution> executionInputPort = new AbstractInputPort<Execution>("Execution input") {

		@Override
		public void newEvent(final Execution event) {
			TraceIdFilter.this.newExecution(event);
		}
	};

	public IOutputPort<Execution> getExecutionOutputPort() {
		return this.executionOutputPort;
	}

	private void newExecution(final Execution execution) {
		if ((this.selectedTraces != null) && !this.selectedTraces.contains(execution.getTraceId())) {
			// not interested in this trace
			return;
		}
		this.executionOutputPort.deliver(execution);
	}

	@Override
	public boolean execute() {
		return true; // do nothing
	}

	@Override
	public void terminate(final boolean error) {
		// do nothing
	}

}
