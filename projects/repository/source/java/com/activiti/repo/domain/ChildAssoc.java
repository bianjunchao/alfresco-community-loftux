package com.activiti.repo.domain;

import com.activiti.repo.ref.ChildAssocRef;
import com.activiti.repo.ref.QName;

/**
 * Represents a special type of association between nodes, that of the
 * parent-child relationship.
 * 
 * @author Derek Hulley
 */
public interface ChildAssoc
{
    /**
     * Performs the necessary work on the provided nodes to ensure that a bidirectional
     * association is properly set up.
     * <p>
     * The association attributes still have to be set up.
     * 
     * @param parentNode
     * @param childNode
     * 
     * @see #setName(String)
     * @see #setIsPrimary(boolean)
     */
    public void buildAssociation(ContainerNode parentNode, Node childNode);
    
    /**
     * Performs the necessary work on the {@link #getParent() parent} and {@link #getChild() child} nodes
     * to maintain the inverse association sets
     */
    public void removeAssociation();
    
    public ChildAssocRef getChildAssocRef();

    public Long getId();

    public void setId(Long id);

    public ContainerNode getParent();

    public void setParent(ContainerNode node);

    public Node getChild();

    public void setChild(Node node);
    
    /**
     * @return Returns the qualified name of this association 
     */
    public QName getQName();

    /**
     * @param qname the qualified name of the association
     */
    public void setQName(QName qname);

    public boolean getIsPrimary();

    public void setIsPrimary(boolean isPrimary);
}
