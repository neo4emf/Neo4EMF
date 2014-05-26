package fr.inria.atlanmod.neo4emf.resourceUtil;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.codegen.util.CodeGenUtil;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.Index;

import fr.inria.atlanmod.neo4emf.PersistentPackage;
import fr.inria.atlanmod.neo4emf.Point;
import fr.inria.atlanmod.neo4emf.drivers.IPersistenceService;
import fr.inria.atlanmod.neo4emf.drivers.IPersistenceServiceFactory;
import fr.inria.atlanmod.neo4emf.drivers.ISerializer;
import fr.inria.atlanmod.neo4emf.drivers.impl.NETransaction;
import fr.inria.atlanmod.neo4emf.persistence.PersistenceConfiguration;

public class Neo4emfResourceUtil {
	
	
	public static void importFromXMI(PersistentPackage ep, String xmiPath, String outputPath){
		importFromXMI(ep, URI.createFileURI(xmiPath), outputPath);		
	}
	
	
	
	public static void importFromXMI(PersistentPackage ep, URI xmiUri, String outputPath){
		// Init variables 
		URI uri = URI.createURI("neo4emf:///"+outputPath);
		PersistenceConfiguration conf = new PersistenceConfiguration(ep, uri, Collections.<String,Object>emptyMap());
		deleteFileOrDirectory(new File(outputPath));
		IPersistenceService graphDB = IPersistenceServiceFactory.eINSTANCE
				.createPersistenceService(conf);
		// Serialize the resource in Neo4j DB
		serializeResource(graphDB, xmiUri, null);
		
	}
	
	public static void importFromXMI(PersistentPackage ep, String xmiPath, String outputPath, String ecorePath){
		// Init variables 
		URI uri = URI.createURI("neo4emf:///"+outputPath);
		PersistenceConfiguration conf = new PersistenceConfiguration(ep, uri, Collections.<String,Object>emptyMap());
		deleteFileOrDirectory(new File(outputPath));
		IPersistenceService graphDB = IPersistenceServiceFactory.eINSTANCE
				.createPersistenceService(conf);
		// Serialize the resource in Neo4j DB
		Resource metaResource = initMetalmodel(ecorePath);
		serializeResource(graphDB,URI.createFileURI(xmiPath), metaResource);
		
	}
	
	
	private static Resource initMetalmodel(String ecorePath) {
		 URI modelFileURI = URI.createURI(ecorePath);
		 ResourceSet rSet = new ResourceSetImpl();
		 rSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
         rSet.setPackageRegistry(EPackage.Registry.INSTANCE);
         Resource metaResource = rSet.getResource(modelFileURI, true);
         registerResource(metaResource);
		return metaResource;
	}

	
	public static void registerResource(Resource resource){
		TreeIterator<EObject> iter = resource.getAllContents();
        while (iter.hasNext()){
       	 EObject object =  iter.next();
       	 if (object instanceof EPackage){
       		 EPackage p = (EPackage)object;
       		    EPackage.Registry.INSTANCE.put(p.getNsURI(), p);
       	 }	  
        }
	}

	
	private static void serializeResource(IPersistenceService graphDB, URI xmiUri, Resource metaResource){
		
		try {		
			Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
			Map<String, Object> map = reg.getExtensionToFactoryMap();
			map.put("xmi", new XMIResourceFactoryImpl());
			// Create a resource set to hold the resources.
			ResourceSet resourceSet = new ResourceSetImpl();
			// Create a new empty resource.
			Resource resource = resourceSet.getResource(xmiUri, true);

		EList<EObject> objectsList= resourceToObjectsList(resource);
		Map<EObject,Long> eObjectsToNodes =  persistNodes(graphDB, objectsList);

		persistReferences(graphDB, objectsList, eObjectsToNodes, metaResource);

		}finally{
			graphDB.shutdown();
		}
	}
	
	public static Map serialize (IPersistenceService graphDB, String xmiPath, Resource metaResource) {
		Map<EObject,Long> eObjectsToNodes = new HashMap<EObject, Long>();
		try {		
			Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
			Map<String, Object> map = reg.getExtensionToFactoryMap();
			map.put("xmi", new XMIResourceFactoryImpl());
			// Create a resource set to hold the resources.
			ResourceSet resourceSet = new ResourceSetImpl();
			// Create a new empty resource.
			URI uri = URI.createFileURI(xmiPath);
			Resource resource = resourceSet.getResource(uri, true);

		EList<EObject> objectsList= resourceToObjectsList(resource);
		 eObjectsToNodes =  persistNodes(graphDB, objectsList);

		persistReferences(graphDB, objectsList, eObjectsToNodes, metaResource);

		}finally{
			graphDB.shutdown();
		}
		return eObjectsToNodes;
	}

	public static Map<String, Map<Point, RelationshipType>>createRelationshipTypesMap(String key){
		Map<String,Map<Point, RelationshipType>> result = new HashMap<String,Map<Point, RelationshipType>>();
		EPackage pck = EPackage.Registry.INSTANCE.getEPackage(key);
		Assert.isNotNull(pck, "Unregistered ePackage with NS_URI: "+key );
		Resource resource = pck.eResource();
		for (EPackage ePck : getResourcePackages(resource)){
			result.put(ePck.getNsURI(), createRelationshipTypesMapForPackage(ePck));
		}
		return result;
		
	}
	
	@SuppressWarnings("unused")
	private static EPackage getFirstParentEPackage(EPackage pck) {
		return pck.getESuperPackage()== null ? pck : getFirstParentEPackage(pck.getESuperPackage());
		
	}

	public static Map<Point, RelationshipType> createRelationshipTypesMapForPackage(EPackage pck){
		Map<Point, RelationshipType> map = new HashMap<Point, RelationshipType>();
		for (EClass clsfier : getOrderedClassifiers(pck)){
			for (EStructuralFeature str : clsfier.getEAllReferences())
				if (! map.containsKey(new Point(clsfier.getClassifierID(), clsfier.getFeatureID(str)))){
					String stri = formatRelationshipName(clsfier,str);
					map.put(new Point(clsfier.getClassifierID(), str.getFeatureID()), DynamicRelationshipType.withName(stri));
				}
		}
		return map;
	}
	
	private static Map<Point, RelationshipType> createRelTypesMap(EList<EObject> objectsList) {
		Map<Point, RelationshipType> map = new HashMap<Point, RelationshipType>();
		for (EPackage pck : getEObjectsEPackages(objectsList)){
			map.putAll(createRelationshipTypesMapForPackage(pck));
		}
		return map;
	}
	
private static Map<Point, RelationshipType> createRelTypesMap(Resource resource) {
		Map<Point, RelationshipType> map = new HashMap<Point, RelationshipType>();
		for (EPackage pck : getResourcePackages(resource)){
			map.putAll(createRelationshipTypesMapForPackage(pck));
		}
		return map;
	}



public static String formatRelationshipName(EClass clsfier, EStructuralFeature str) {
	return FormatClassifierName(clsfier) + "__" + CodeGenUtil.format(str.getName(), '_', null, false, false).toUpperCase();
	
}
private static String FormatClassifierName(EClassifier cls)
{
  String name = cls.getName();
  String prefix = cls.getEPackage().getNsPrefix();
  return CodeGenUtil.format(name, '_', prefix, true, true).toUpperCase();
}

private static EList<EClass> getOrderedClassifiers(EPackage package_) {
	EList<EClass> result = new BasicEList<EClass>();
	Set<EClass> resultSet = new HashSet<EClass>();

    for (Iterator<EClass> iter = getEClasses(package_).iterator(); iter.hasNext(); )
    {
      List<EClass> extendChain = new LinkedList<EClass>();
      Set<EClass> visited = new HashSet<EClass>();
      for (EClass class_ = iter.next(); class_ != null && visited.add(class_); class_ = class_.getESuperTypes().isEmpty()? null : class_.getESuperTypes().get(0))
      {
        if (package_ == class_.getEPackage() && resultSet.add(class_))
        {
          extendChain.add(0, class_);
        }
      }
      result.addAll(extendChain);
    }
	return result;
}


private static EList<EClass> getEClasses(EPackage package_) {
	EList<EClass> listCls = new BasicEList<EClass>();
	for (EClassifier clsfier : package_.getEClassifiers()){
		if (clsfier instanceof EClass)
			listCls.add((EClass)clsfier);
	}
	return listCls;	
}


private static Set<EPackage> getEObjectsEPackages(EList<EObject> objectsList){
	Set<EPackage> pckList = new HashSet<EPackage>(); 
	Iterator<EObject> iterator = objectsList.iterator();
		while (iterator.hasNext()) {
			EObject obj = iterator.next();
			pckList.add(obj.eClass().getEPackage());
		}
	return pckList;
}


private static EList<EPackage> getResourcePackages(Resource resource){
	EList<EPackage> pckList = new BasicEList<EPackage>(); 
	Iterator<EObject> iterator = resource.getAllContents();
	while (iterator.hasNext()){
		EObject obj = iterator.next();
		if (obj instanceof EPackage)
			pckList.add((EPackage) obj);
	}
	return pckList;
}


/**
 * Constructor
 * {@code DB_PATH} the path of the data base & {@code xmiPath} path of the data base
 * @param DB_PATH 
 * @param xmiPath
 */

	public static void deleteFileOrDirectory( final File file )
    {
        if ( !file.exists() ) return;
        if ( file.isDirectory() )
            for ( File child : file.listFiles() )
            	deleteFileOrDirectory( child );
        else
            file.delete();
    }



	private static void persistReferences(IPersistenceService graphDB, 
			EList<EObject> objectsList,
			Map<EObject,Long> eObjectsToNodes,
			Resource resource) {
		
		Map<Point,RelationshipType> reltypesMap; 
		
		if (resource != null) {
			reltypesMap = createRelTypesMap(resource);			
		} else {
			reltypesMap = createRelTypesMap(objectsList);			
		}
		
		
		int i=0;
		NETransaction tx = graphDB.createTransaction();
		try {
			
		for (EObject eObject : objectsList) {
			for (EReference eRef : eObject.eClass().getEAllReferences()) {
				Object value = null;
				try {
					value = eObject.eGet(eRef);
				} catch (ClassCastException e) {};
				
				if (value != null) {
					if (eRef.isMany()){
						@SuppressWarnings({"unchecked"})
						EList<EObject> objList = (EList<EObject>) eObject.eGet(eRef);
						for (EObject eObjectEnd : objList){
							
							createPropertyForObjects(graphDB, eObjectsToNodes, eObject, eObjectEnd, eRef, reltypesMap);
							
								}
							}
					else {
							EObject singleEObject = (EObject) eObject.eGet(eRef);
							createPropertyForObjects(graphDB, eObjectsToNodes, eObject, singleEObject, eRef, reltypesMap);
						}
				}
				i++;
				if (i==ISerializer.DEFAULT_TRANSACTIONS_COUNT){
					tx.success();
					tx.commit();
					tx=graphDB.createTransaction();
					i=0;
						}				
			}
				
		}
		}finally{
			tx.success();
			tx.commit();
			
		}
	}

	private static void createPropertyForObjects(IPersistenceService graphDB,
						Map<EObject, Long> eObjectsToNodes,
					EObject eObject,
				EObject eObjectEnd, 
			EReference eRef,
			Map<Point,RelationshipType> reltypes) {
		
		Node node = graphDB.getNodeById(eObjectsToNodes.get(eObject));
		Node nodeEnd = graphDB.getNodeById(eObjectsToNodes.get(eObjectEnd));
		RelationshipType rel = DynamicRelationshipType.withName(formatRelationshipName(eObject.eClass(), eRef));
		//node.createRelationshipTo(nodeEnd, reltypes.get(new Point(eObject.eClass().getClassifierID(), eRef.getFeatureID())));		
		node.createRelationshipTo(nodeEnd, rel);		
		// get the reltype
	}

	private  static EList<EObject> resourceToObjectsList(Resource resource){
		EList<EObject> objectsList = new BasicEList<EObject>();	
		

		TreeIterator<EObject> iterator = resource.getAllContents();
		while (iterator.hasNext()){
			Object obj = iterator.next();
			if (obj instanceof EObject)
				objectsList.add((EObject)obj);

		}

		return objectsList;
	}

	private static Node createWithIndexIfNotExists(EClass c, 
			IPersistenceService graphDB ){		
		if (getMetaIndex(graphDB).get(IPersistenceService.ID_META, c.getEPackage().getName()+"_"+c.getClassifierID()).getSingle() != null)
			return getMetaIndex(graphDB).get(IPersistenceService.ID_META, c.getEPackage().getName()+"_"+c.getClassifierID()).getSingle();
		Node n = graphDB.createNode();
		n.setProperty(IPersistenceService.ECLASS_NAME,c.getName() );
		n.setProperty(IPersistenceService.NS_URI, c.getEPackage().getNsURI());
		getMetaIndex(graphDB).putIfAbsent(n, IPersistenceService.ID_META, c.getEPackage().getName()+"_"+c.getClassifierID());
		return n;					
	}

	
	public static Node createResourceNodeIfAbsent(IPersistenceService graphDB){
		if (getMetaIndex(graphDB).get(IPersistenceService.ID_META, IPersistenceService.RESOURCE_NODE).getSingle() != null)
			return getMetaIndex(graphDB).get(IPersistenceService.ID_META, IPersistenceService.RESOURCE_NODE).getSingle();
		Node n = graphDB.createNode();
		getMetaIndex(graphDB).putIfAbsent(n, IPersistenceService.ID_META, IPersistenceService.RESOURCE_NODE);
		return n;					
	}

	
	public static Index<Node> getMetaIndex(IPersistenceService graphDB){		
		return  graphDB.getMetaIndex();		
	}

	
	public static Node createNodeFromEObject(EObject eObject,IPersistenceService graphDB) {
		Node node = graphDB.createNode();
		Node typeNode = createWithIndexIfNotExists(eObject.eClass(), graphDB);
		node.createRelationshipTo(typeNode, IPersistenceService.INSTANCE_OF);
		if (isRoot(eObject))
			createResourceNodeIfAbsent(graphDB).createRelationshipTo(node, IPersistenceService.IS_ROOT);		
		return node;
	}
	
	private static boolean isRoot(EObject eObject) {		
		if (eObject.eContainer() instanceof EPackage 
				|| eObject.eContainer() == null ) 
			return true;
		return false;
	}
	
	

	private static Map<EObject, Long> persistNodes(IPersistenceService graphDB,		
							EList<EObject> objectsList){
		
		Map<EObject, Long>  eObjectsToNodes= new HashMap<EObject,Long>();
		NETransaction tx = graphDB.createTransaction();
		try{
			int i = 0;
			for (EObject eObject : objectsList){
				 Node	n = createNodeFromEObject(eObject, graphDB);
				 eObjectsToNodes.put(eObject, n.getId());
				 setupAttributes(eObject,n);
				i++; 
				if (i> ISerializer.DEFAULT_TRANSACTIONS_COUNT){			
					i=0;
					tx.success();
					tx.commit();
					tx= graphDB.createTransaction();
				}
			}
		}finally{
			tx.success();
			tx.commit();
		}
		return eObjectsToNodes;
	}
	
	@SuppressWarnings("unchecked")
	private static void setupAttributes(EObject eObject, Node n) {
		// TODO Auto-generated method stub
		EList<EAttribute> atrList= eObject.eClass().getEAllAttributes();
		java.util.Iterator<EAttribute> it = atrList.iterator();
		while(it.hasNext()){			
			EAttribute at =it.next();			
			if (eObject.eGet(at)!= null && !at.isMany()){
				if (at.getEType() instanceof EEnum)
					n.setProperty(at.getName(), eObject.eGet(at).toString());
				else if (isPrimitive(at.getName()))
					n.setProperty(at.getName(), eObject.eGet(at));
				else 
					n.setProperty(at.getName(), eObject.eGet(at).toString());
					}			
			else if (eObject.eGet(at) != null && at.isMany()){
				n.setProperty(at.getName(), ((EList<EDataType>) eObject.eGet(at)).toArray());}
			else if (!at.isMany()){ 
				if (at.getEType().getInstanceClass().isAssignableFrom(Boolean.class))
					n.setProperty(at.getName(), false );
				else if (at.getEType().getInstanceClass().isAssignableFrom(String.class))
					n.setProperty(at.getName(), "");
				else if (at.getEType().getInstanceClass().isAssignableFrom(Integer.class))
					n.setProperty(at.getName(), 0);
				else
					n.setProperty(at.getName(), 0);
				}
			else {n.setProperty(at.getName(), new Object[1]);}
		}
	}
	
	private static boolean isPrimitive(String str){
		if (str.equals("Boolean") 
				|| str.equals("Integer") 
				|| str.equals("Short") 
				|| str.equals("Long") 
				|| str.equals("Float")
				|| str.equals("String") 
				|| str.equals("Double"))
			return false;
		return true;
	}
	
	@SuppressWarnings("unused")
	private static String formatUniquePackageName(EPackage pck){
		return formatUniquePackageNameLevel_i(pck,"",0);
	}

	private static String formatUniquePackageNameLevel_i(EPackage pck,
			String string, int i) {
		String underScores = "";
		for (int j=0;j>i;j++)
			underScores+="_";
		String localName= underScores+pck.getNsURI();
		string +=localName;
		return pck.getESuperPackage()!=null ? formatUniquePackageNameLevel_i(pck,string,i+1): string;
	}
	
	public static URI createNeo4emfURI(File file) {
		URI fileUri = URI.createFileURI(file.getAbsolutePath());
		URI uri = URI.createHierarchicalURI(
				"neo4emf", 
				fileUri.authority(),
				fileUri.device(),
				fileUri.segments(),
				fileUri.query(),
				fileUri.fragment());
		return uri;
	}
}
