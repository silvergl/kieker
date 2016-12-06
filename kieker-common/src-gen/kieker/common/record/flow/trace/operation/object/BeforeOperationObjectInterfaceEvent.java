package kieker.common.record.flow.trace.operation.object;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import kieker.common.record.flow.trace.operation.object.BeforeOperationObjectEvent;
import kieker.common.util.registry.IRegistry;

import kieker.common.record.flow.IInterfaceRecord;

/**
 * @author Florian Fittkau
 * 
 * @since 1.10
 */
public class BeforeOperationObjectInterfaceEvent extends BeforeOperationObjectEvent implements IInterfaceRecord {
	private static final long serialVersionUID = -8438691367718487460L;

		/** Descriptive definition of the serialization size of the record. */
		public static final int SIZE = TYPE_SIZE_LONG // IEventRecord.timestamp
				 + TYPE_SIZE_LONG // ITraceRecord.traceId
				 + TYPE_SIZE_INT // ITraceRecord.orderIndex
				 + TYPE_SIZE_STRING // IOperationSignature.operationSignature
				 + TYPE_SIZE_STRING // IClassSignature.classSignature
				 + TYPE_SIZE_INT // IObjectRecord.objectId
				 + TYPE_SIZE_STRING // IInterfaceRecord.interface
		;
	
		public static final Class<?>[] TYPES = {
			long.class, // IEventRecord.timestamp
			long.class, // ITraceRecord.traceId
			int.class, // ITraceRecord.orderIndex
			String.class, // IOperationSignature.operationSignature
			String.class, // IClassSignature.classSignature
			int.class, // IObjectRecord.objectId
			String.class, // IInterfaceRecord.interface
		};
	
	/** user-defined constants */

	/** default constants */
	public static final String INTERFACE = "";

	/** property declarations */
	private final String _interface;

	/**
	 * Creates a new instance of this class using the given parameters.
	 * 
	 * @param timestamp
	 *            timestamp
	 * @param traceId
	 *            traceId
	 * @param orderIndex
	 *            orderIndex
	 * @param operationSignature
	 *            operationSignature
	 * @param classSignature
	 *            classSignature
	 * @param objectId
	 *            objectId
	 * @param _interface
	 *            _interface
	 */
	public BeforeOperationObjectInterfaceEvent(final long timestamp, final long traceId, final int orderIndex, final String operationSignature, final String classSignature, final int objectId, final String _interface) {
		super(timestamp, traceId, orderIndex, operationSignature, classSignature, objectId);
		this._interface = _interface == null?"":_interface;
	}

	/**
	 * This constructor converts the given array into a record.
	 * It is recommended to use the array which is the result of a call to {@link #toArray()}.
	 * 
	 * @param values
	 *            The values for the record.
	 */
	public BeforeOperationObjectInterfaceEvent(final Object[] values) { // NOPMD (direct store of values)
		super(values, TYPES);
		this._interface = (String) values[6];
	}

	/**
	 * This constructor uses the given array to initialize the fields of this record.
	 * 
	 * @param values
	 *            The values for the record.
	 * @param valueTypes
	 *            The types of the elements in the first array.
	 */
	protected BeforeOperationObjectInterfaceEvent(final Object[] values, final Class<?>[] valueTypes) { // NOPMD (values stored directly)
		super(values, valueTypes);
		this._interface = (String) values[6];
	}

	/**
	 * This constructor converts the given array into a record.
	 * 
	 * @param buffer
	 *            The bytes for the record.
	 * 
	 * @throws BufferUnderflowException
	 *             if buffer not sufficient
	 */
	public BeforeOperationObjectInterfaceEvent(final ByteBuffer buffer, final IRegistry<String> stringRegistry) throws BufferUnderflowException {
		super(buffer, stringRegistry);
		this._interface = stringRegistry.get(buffer.getInt());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] toArray() {
		return new Object[] {
			this.getTimestamp(),
			this.getTraceId(),
			this.getOrderIndex(),
			this.getOperationSignature(),
			this.getClassSignature(),
			this.getObjectId(),
			this.getInterface()
		};
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerStrings(final IRegistry<String> stringRegistry) {	// NOPMD (generated code)
		stringRegistry.get(this.getOperationSignature());
		stringRegistry.get(this.getClassSignature());
		stringRegistry.get(this.getInterface());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeBytes(final ByteBuffer buffer, final IRegistry<String> stringRegistry) throws BufferOverflowException {
		buffer.putLong(this.getTimestamp());
		buffer.putLong(this.getTraceId());
		buffer.putInt(this.getOrderIndex());
		buffer.putInt(stringRegistry.get(this.getOperationSignature()));
		buffer.putInt(stringRegistry.get(this.getClassSignature()));
		buffer.putInt(this.getObjectId());
		buffer.putInt(stringRegistry.get(this.getInterface()));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] getValueTypes() {
		return TYPES; // NOPMD
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSize() {
		return SIZE;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated This record uses the {@link kieker.common.record.IMonitoringRecord.Factory} mechanism. Hence, this method is not implemented.
	 */
	@Override
	@Deprecated
	public void initFromArray(final Object[] values) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated This record uses the {@link kieker.common.record.IMonitoringRecord.BinaryFactory} mechanism. Hence, this method is not implemented.
	 */
	@Override
	@Deprecated
	public void initFromBytes(final ByteBuffer buffer, final IRegistry<String> stringRegistry) throws BufferUnderflowException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (obj.getClass() != this.getClass()) return false;
		
		final BeforeOperationObjectInterfaceEvent castedRecord = (BeforeOperationObjectInterfaceEvent) obj;
		if (this.getLoggingTimestamp() != castedRecord.getLoggingTimestamp()) return false;
		if (this.getTimestamp() != castedRecord.getTimestamp()) return false;
		if (this.getTraceId() != castedRecord.getTraceId()) return false;
		if (this.getOrderIndex() != castedRecord.getOrderIndex()) return false;
		if (!this.getOperationSignature().equals(castedRecord.getOperationSignature())) return false;
		if (!this.getClassSignature().equals(castedRecord.getClassSignature())) return false;
		if (this.getObjectId() != castedRecord.getObjectId()) return false;
		if (!this.getInterface().equals(castedRecord.getInterface())) return false;
		return true;
	}
	
	public final String getInterface() {
		return this._interface;
	}	
}
