package fr.inria.atlanmod.neo4emf.persistence;


public interface IPersistedEObject {

	/**
	 * Add the given attribute to {@link IPersistedEObject} in the back-end
	 * @param name the name of the attribute
	 * @param value the value of the attribute
	 */
	public void addAttribute(String name, Object value);
	/**
	 * Add the given reference to {@link IPersistedEObject} in the back-end
	 * @param name the name of the reference
	 * @param value the {@link IPersistedEObject} linked with the reference
	 */
	public void addReference(String name, IPersistedEObject value);
	/**
	 * Remove the given reference from {@link IPersistedEObject} in the back-end
	 * @param name the name of the reference
	 * @param value the {@link IPersistedEObject} linked with the reference
	 */
	public void removeReference(String name, IPersistedEObject value);
}
