package kieker.monitoring.core;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;

import javax.management.ObjectName;

import kieker.monitoring.core.configuration.Configuration;
import kieker.monitoring.timer.ITimeSource;
import kieker.monitoring.writer.IMonitoringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A factory class to create <code>{@link MonitoringController}</code> instances.
 * 
 * Also is resposible for reading the configuration and instantiating <code>{@link IMonitoringWriter}</code>
 * and <code>{@link ITimeSource}</code>.
 * 
 * @author Robert von Massow
 *
 */
public class ControllerFactory {

	private static Log log = LogFactory.getLog(ControllerFactory.class);

	private static IMonitoringWriter createWriter(final Configuration configuration) throws InstantiationException {
		IMonitoringWriter monitoringWriter = null;
		final String writerClassname = configuration.getStringProperty(Configuration.WRITER_CLASSNAME);
		try {
			// search for correct Constructor -> 2 correct parameters
			//TODO: dangerous! escaping controller in constructor! init at end of constructor
			final Class<?> clazz = Class.forName(writerClassname);
			if(IMonitoringWriter.class.isAssignableFrom(clazz)){
				@SuppressWarnings("unchecked")
				final
				Constructor<? extends IMonitoringWriter> constructor =
					(Constructor<IMonitoringWriter>) clazz.
					getConstructor(Configuration.class);
				monitoringWriter = ControllerFactory.instantiateClass(constructor, configuration.getPropertiesStartingWith(writerClassname));
			}
		} catch (final NoSuchMethodException ex) {
			ControllerFactory.log.error("Writer Class '" + writerClassname + "' has to implement a constructor that accepts a single Configuration", ex);
		} catch (final ClassNotFoundException ex) {
			ControllerFactory.log.error("Writer Class '" + writerClassname + "' not found", ex);
		} catch (final NoClassDefFoundError ex) {
			ControllerFactory.log.error("Writer Class '" + writerClassname + "' not found", ex);
		} catch(final InstantiationException ex) {
			ControllerFactory.log.error("Error invoking constructor " + writerClassname + "(IWriterController, Configuration)", ex);
			throw ex;
		}
		return monitoringWriter;
	}


	private static ITimeSource createTimeSource(final Configuration configuration) throws InstantiationException {
		final String timerClassname = configuration.getStringProperty(Configuration.TIMER_CLASSNAME);
		ITimeSource timesource = null;
		try {
			final Class<?> clazz = Class.forName(timerClassname);
			if(ITimeSource.class.isAssignableFrom(clazz)){
				@SuppressWarnings("unchecked")
				final
				Constructor<? extends ITimeSource> constructor =
					(Constructor<ITimeSource>) clazz.
					getConstructor(Configuration.class);
				timesource = ControllerFactory.instantiateClass(constructor, configuration.getPropertiesStartingWith(timerClassname));
				System.out.println();
			}
		} catch (final NoSuchMethodException ex) {
			ControllerFactory.log.error("Writer Class '" + timerClassname + "' has to implement a constructor that accepts a single Configuration", ex);
		} catch (final ClassNotFoundException ex) {
			ControllerFactory.log.error("Writer Class '" + timerClassname + "' not found", ex);
		} catch (final NoClassDefFoundError ex) {
			ControllerFactory.log.error("Writer Class '" + timerClassname + "' not found", ex);
		} catch(final InstantiationException ex) {
			ControllerFactory.log.error("Error invoking constructor " + timerClassname + "(IWriterController, Configuration)", ex);
			throw ex;
		}
		return timesource;
	}

	private static <A> A instantiateClass(final Constructor<A> constructor, final Object... objects) throws InstantiationException {
		try {
			return constructor.newInstance(objects);
		} catch (final Exception e) {
			throw new InstantiationException();
		}
	}

	/**
	 * Factory method for {@link IMonitoringController}.
	 * 
	 * @param configuration
	 * @return the requested instance of an <code>{@link IMonitoringController}</code>
	 */
	public static MonitoringController createInstance(final Configuration configuration) {
		final IController controller = new Controller(configuration);
		try {
			Runtime.getRuntime().addShutdownHook(new ShutdownHook(controller));
		} catch (final Exception e){
			ControllerFactory.log.warn("Failed to add shutdownHook");
		}
		IMonitoringWriter writer = null;
		final MonitoringController monitoringController;
		ITimeSource timeSource = null;
		try {
			timeSource = ControllerFactory.createTimeSource(configuration);
		}catch (final Exception e){
			ControllerFactory.log.error("TimeSource initalization failed", e);
			controller.terminateMonitoring();
			return null;
		}
		try{
			writer = ControllerFactory.createWriter(configuration);
		}catch (final Exception e){
			ControllerFactory.log.error("Writer creation failed", e);
			controller.terminateMonitoring();
			return null;
		}
		final WriterController writerController = new WriterController(timeSource, writer, controller,configuration.getBooleanProperty(Configuration.REPLAY_MODE));
		try {
			writer.setController(writerController);
		} catch (final Exception e) {
			writerController.terminateMonitoring();
			ControllerFactory.log.error("Failed to initialize the Writer", e);
			return null;
		}
		final SamplingController samplingController = new SamplingController(configuration.getIntProperty(Configuration.PERIODIC_SENSORS_EXECUTOR_POOL_SIZE));
		ObjectName objectname = null;
		try {
			objectname = new ObjectName(
					configuration.getStringProperty(Configuration.ACTIVATE_MBEAN_DOMAIN),
					"type",
					configuration.getStringProperty(Configuration.ACTIVATE_MBEAN_TYPE));
		} catch (final Exception e) {
			ControllerFactory.log.warn("Failed to initialize ObjectName", e);
		}
		monitoringController = new MonitoringController(controller, writerController, samplingController, objectname);
		try {
			ManagementFactory.getPlatformMBeanServer().registerMBean(monitoringController, objectname);
		} catch (final Exception e) {
			ControllerFactory.log.warn("Unable to register MBean Server", e);
		}
		samplingController.setMonController(monitoringController);
		return monitoringController;
	}

	public static MonitoringController getInstance() {
		return LazyHolder.INSTANCE;
	}

	/**
	 * SINGLETON
	 */
	private final static class LazyHolder {
		static {
			ControllerFactory.log.info("Initialization started");
			MonitoringController tmp = null;
			try {
				tmp = ControllerFactory.createInstance(Configuration.createSingletonConfiguration());
			} catch (final Exception e) {
				ControllerFactory.log.error("Something went wrong initializing the Controller", e);
			} finally {
				INSTANCE = tmp;
			}
		}
		private static final MonitoringController INSTANCE;
	}
}
