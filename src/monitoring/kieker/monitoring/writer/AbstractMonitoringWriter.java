package kieker.monitoring.writer;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kieker.common.record.IMonitoringRecord;
import kieker.monitoring.core.IMonitoringController;
import kieker.monitoring.core.configuration.Configuration;

/*
 * ==================LICENCE=========================
 * Copyright 2006-2011 Kieker Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ==================================================
 */
/**
 * @author Jan Waller
 */
public abstract class AbstractMonitoringWriter implements IMonitoringWriter {
	private static final Log log = LogFactory.getLog(Configuration.class);
	
	protected final Configuration configuration;
	protected final IMonitoringController ctrl;
	
	/**
	 * 
	 * @param IMonitoringController
	 * @param configuration
	 */
	protected AbstractMonitoringWriter(final IMonitoringController ctrl, final Configuration configuration) {
		this.ctrl = ctrl; 
		try {
			// somewhat dirty hack...
			Properties defaultProps = getDefaultProperties();
			if (defaultProps != null) {
				configuration.setDefaultProperties(defaultProps);
			}
		} catch (final IllegalAccessException ex) {
			AbstractMonitoringWriter.log.error("Unable to set writer custom default properties");
		}
		this.configuration = configuration;
	}
	
	/**
	 * This method should be overwritten, iff the writer is external to Kieker and
	 * thus its default configuration is not included in the default config file.
	 * 
	 * @return
	 */
	protected Properties getDefaultProperties() {
		return null;
	}
	
	@Override
	public String getInfoString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Writer: '");
		sb.append(this.getClass().getName());
		sb.append("'\n\tConfiguration");
		if (configuration.isEmpty()) {
			sb.append("\n\t\tNo Parameters)");
		} else {
			for (String property : configuration.stringPropertyNames()) {
				sb.append("\n\t\t");
				sb.append(property);
				sb.append("='");
				sb.append(configuration.getProperty(property));
				sb.append("'");
			}
		}
		return sb.toString();
	}
	
	@Override
	public abstract boolean newMonitoringRecord(IMonitoringRecord record);
	
	@Override
	public abstract void terminate();
}
