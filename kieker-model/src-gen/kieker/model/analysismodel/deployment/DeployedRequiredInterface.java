/**
 */
package kieker.model.analysismodel.deployment;

import kieker.model.analysismodel.assembly.AssemblyRequiredInterface;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Deployed Required Interface</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link kieker.model.analysismodel.deployment.DeployedRequiredInterface#getAssemblyRequiredInterface <em>Assembly Required Interface</em>}</li>
 *   <li>{@link kieker.model.analysismodel.deployment.DeployedRequiredInterface#getRequires <em>Requires</em>}</li>
 * </ul>
 *
 * @see kieker.model.analysismodel.deployment.DeploymentPackage#getDeployedRequiredInterface()
 * @model
 * @generated
 */
public interface DeployedRequiredInterface extends EObject {
	/**
	 * Returns the value of the '<em><b>Assembly Required Interface</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Assembly Required Interface</em>' reference.
	 * @see #setAssemblyRequiredInterface(AssemblyRequiredInterface)
	 * @see kieker.model.analysismodel.deployment.DeploymentPackage#getDeployedRequiredInterface_AssemblyRequiredInterface()
	 * @model
	 * @generated
	 */
	AssemblyRequiredInterface getAssemblyRequiredInterface();

	/**
	 * Sets the value of the '{@link kieker.model.analysismodel.deployment.DeployedRequiredInterface#getAssemblyRequiredInterface <em>Assembly Required Interface</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Assembly Required Interface</em>' reference.
	 * @see #getAssemblyRequiredInterface()
	 * @generated
	 */
	void setAssemblyRequiredInterface(AssemblyRequiredInterface value);

	/**
	 * Returns the value of the '<em><b>Requires</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Requires</em>' reference.
	 * @see #setRequires(DeployedProvidedInterface)
	 * @see kieker.model.analysismodel.deployment.DeploymentPackage#getDeployedRequiredInterface_Requires()
	 * @model
	 * @generated
	 */
	DeployedProvidedInterface getRequires();

	/**
	 * Sets the value of the '{@link kieker.model.analysismodel.deployment.DeployedRequiredInterface#getRequires <em>Requires</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Requires</em>' reference.
	 * @see #getRequires()
	 * @generated
	 */
	void setRequires(DeployedProvidedInterface value);

} // DeployedRequiredInterface
