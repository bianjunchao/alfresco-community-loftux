package com.activiti.repo.domain.hibernate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.activiti.repo.domain.ChildAssoc;
import com.activiti.repo.domain.ContainerNode;
import com.activiti.repo.domain.ContentNode;
import com.activiti.repo.domain.Node;
import com.activiti.repo.domain.NodeAssoc;
import com.activiti.repo.domain.NodeKey;
import com.activiti.repo.domain.RealNode;
import com.activiti.repo.domain.ReferenceNode;
import com.activiti.repo.domain.Store;
import com.activiti.repo.domain.StoreKey;
import com.activiti.repo.ref.QName;
import com.activiti.repo.ref.StoreRef;
import com.activiti.util.BaseHibernateTest;
import com.activiti.util.GUID;

/**
 * Test persistence and retrieval of Hibernate-specific implementations of the
 * {@link com.activiti.repo.domain.Node} interface
 * 
 * @author Derek Hulley
 */
public class HibernateNodeTest extends BaseHibernateTest
{
    private Store store;
    
    public HibernateNodeTest()
    {
    }
    
    protected void onSetUpInTransaction() throws Exception
    {
        store = new StoreImpl();
		StoreKey storeKey = new StoreKey(StoreRef.PROTOCOL_WORKSPACE,
                "TestWorkspace@" + System.currentTimeMillis());
		store.setKey(storeKey);
        // persist so that it is present in the hibernate cache
        getSession().save(store);
    }
    
    protected void onTearDownInTransaction()
    {
        // force a flush to ensure that the database updates succeed
        getSession().flush();
        getSession().clear();
    }

    public void testSetUp() throws Exception
    {
        assertNotNull("Workspace not initialised", store);
    }

	public void testGetStore() throws Exception
	{
        // create a new Node
        Node node = new NodeImpl();
		NodeKey key = new NodeKey("Random Protocol", "Random Identifier", "AAA");
		node.setKey(key);
        node.setStore(store);   // not meaningful as it contradicts the key
        node.setType(Node.TYPE_CONTAINER);
        // persist it
		try
		{
			Serializable id = getSession().save(node);
			fail("No store exists");
		}
		catch (Throwable e)
		{
			// expected
		}
		// this should not solve the problem
        node.setStore(store);
        // persist it
		try
		{
			Serializable id = getSession().save(node);
			fail("Setting store does not persist protocol and identifier attributes");
		}
		catch (Throwable e)
		{
			// expected
		}
		
		// fix the key
		key = new NodeKey(store.getKey().getProtocol(), store.getKey().getIdentifier(), "AAA");
		node.setKey(key);
		// now it should work
		Serializable id = getSession().save(node);

        // throw the reference away and get the a new one for the id
        node = (Node) getSession().load(NodeImpl.class, id);
        assertNotNull("Node not found", node);
		// check that the store has been loaded
		Store loadedStore = node.getStore();
		assertNotNull("Store not present on node", loadedStore);
		assertEquals("Incorrect store key", store, loadedStore);
	}
	
    public void testMap() throws Exception
    {
        // create a new Node
        Node node = new NodeImpl();
		NodeKey key = new NodeKey(store.getKey(), "AAA");
		node.setKey(key);
        node.setType(Node.TYPE_CONTAINER);
        // give it a property map
        Map<String, Serializable> propertyMap = new HashMap<String, Serializable>(5);
        propertyMap.put("{}A", "AAA");
        node.getProperties().putAll(propertyMap);
        // persist it
        Serializable id = getSession().save(node);

        // throw the reference away and get the a new one for the id
        node = (Node) getSession().load(NodeImpl.class, id);
        assertNotNull("Node not found", node);
        // extract the Map
        propertyMap = node.getProperties();
        assertNotNull("Map not persisted", propertyMap);
        // ensure that the value is present
        assertNotNull("Property value not present in map", propertyMap.get("{}A"));
    }

    public void testSubclassing() throws Exception
    {
        // persist a subclass of Node
        Node node = new ContentNodeImpl();
		NodeKey key = new NodeKey(store.getKey(), "AAA");
		node.setKey(key);
        node.setType(Node.TYPE_CONTENT);
        Serializable id = getSession().save(node);
        // get the node back
        node = (Node) getSession().get(NodeImpl.class, id);
        // check
        assertNotNull("Persisted node not found", id);
        assertTrue("Subtype not retrieved", node instanceof ContentNode);
    }

    public void testReferenceNode() throws Exception
    {
        // make a reference node
        ReferenceNode node = new ReferenceNodeImpl();
		NodeKey key = new NodeKey(store.getKey(), "AAA");
		node.setKey(key);
        node.setStore(store);
        node.setType(Node.TYPE_REFERENCE);
        node.setReferencedPath("/somepath/to/some/node[1]");
        Serializable refNodeId = getSession().save(node);

        // get the ref node back by ID
        node = (ReferenceNode) getSession().get(NodeImpl.class, refNodeId);
    }
    
    public void testNodeAssoc() throws Exception
    {
        // make a real node
        RealNode sourceNode = new RealNodeImpl();
        NodeKey sourceKey = new NodeKey(store.getKey(), GUID.generate());
        sourceNode.setKey(sourceKey);
        sourceNode.setStore(store);
        sourceNode.setType(Node.TYPE_REAL);
        Serializable realNodeKey = getSession().save(sourceNode);
        
        // make a container node
        ContainerNode targetNode = new ContainerNodeImpl();
        NodeKey targetKey = new NodeKey(store.getKey(), GUID.generate());
        targetNode.setKey(targetKey);
        targetNode.setStore(store);
        targetNode.setType(Node.TYPE_CONTAINER);
        Serializable containerNodeKey = getSession().save(targetNode);
        
        // create an association between them
        NodeAssoc assoc = new NodeAssocImpl();
        assoc.setName("next");
        assoc.buildAssociation(sourceNode, targetNode);
        getSession().save(assoc);
        
        // make another association between the same two nodes
        assoc = new NodeAssocImpl();
        assoc.setName("helper");
        assoc.buildAssociation(sourceNode, targetNode);
        getSession().save(assoc);
        
        // flush and clear the session
        getSession().flush();
        getSession().clear();
        
        // reload the source
        sourceNode = (RealNode) getSession().get(RealNodeImpl.class, sourceKey);
        assertNotNull("Source node not found", sourceNode);
        // check that the associations are present
        assertEquals("Expected exactly 2 target assocs", 2, sourceNode.getTargetNodeAssocs().size());
        
        // reload the target
        targetNode = (ContainerNode) getSession().get(NodeImpl.class, targetKey);
        assertNotNull("Target node not found", targetNode);
        // check that the associations are present
        assertEquals("Expected exactly 2 source assocs", 2, targetNode.getSourceNodeAssocs().size());
    }

    public void testChildAssoc() throws Exception
    {
        // make a content node
        ContentNode contentNode = new ContentNodeImpl();
		NodeKey key = new NodeKey(store.getKey(), GUID.generate());
		contentNode.setKey(key);
        contentNode.setStore(store);
        contentNode.setType(Node.TYPE_CONTENT);
        Serializable contentNodeKey = getSession().save(contentNode);

        // make a container node
        ContainerNode containerNode = new ContainerNodeImpl();
		key = new NodeKey(store.getKey(), GUID.generate());
		containerNode.setKey(key);
        containerNode.setStore(store);
        containerNode.setType(Node.TYPE_CONTAINER);
        Serializable containerNodeKey = getSession().save(containerNode);
        // create an association to the content
        ChildAssoc assoc1 = new ChildAssocImpl();
        assoc1.setIsPrimary(true);
        assoc1.setQName(QName.createQName(null, "number1"));
        assoc1.buildAssociation(containerNode, contentNode);
        getSession().save(assoc1);

        // make another association between the same two parent and child nodes
        ChildAssoc assoc2 = new ChildAssocImpl();
        assoc2.setIsPrimary(true);
        assoc2.setQName(QName.createQName(null, "number2"));
        assoc2.buildAssociation(containerNode, contentNode);
        getSession().save(assoc2);

//        flushAndClear();

        // reload the container
        containerNode = (ContainerNode) getSession().get(ContainerNodeImpl.class, containerNodeKey);
        assertNotNull("Node not found", containerNode);
        // check
        assertEquals("Expected exactly 2 children", 2, containerNode.getChildAssocs().size());
        for (Iterator iterator = containerNode.getChildAssocs().iterator(); iterator.hasNext(); /**/)
        {
            ChildAssoc assoc = (ChildAssoc) iterator.next();
            // the node id must be known
            assertNotNull("Node not populated on assoc", assoc.getChild());
            assertEquals("Node key on child assoc is incorrect", contentNodeKey,
                    assoc.getChild().getKey());
        }

        // check that we can traverse the association from the child
        Set<ChildAssoc> parentAssocs = contentNode.getParentAssocs();
        assertEquals("Expected exactly 2 parent assocs", 2, parentAssocs.size());
        parentAssocs = new HashSet<ChildAssoc>(parentAssocs);
        for (ChildAssoc assoc : parentAssocs)
        {
            // maintain inverse assoc sets
            assoc.removeAssociation();
            // remove the assoc
            getSession().delete(assoc);
        }
        
        // check that the child now has zero parents
        parentAssocs = contentNode.getParentAssocs();
        assertEquals("Expected exactly 0 parent assocs", 0, parentAssocs.size());
    }
}