package net.sf.j3d.network.slide;

import java.util.Enumeration;
import java.util.Vector;

import net.sf.j3d.network.resources.ResourceDistributionTask;

import org.apache.slide.common.ServiceAccessException;
import org.apache.slide.common.Uri;
import org.apache.slide.content.NodeRevisionContent;
import org.apache.slide.content.NodeRevisionDescriptor;
import org.apache.slide.content.NodeRevisionDescriptors;
import org.apache.slide.content.NodeRevisionNumber;
import org.apache.slide.content.RevisionAlreadyExistException;
import org.apache.slide.content.RevisionDescriptorNotFoundException;
import org.apache.slide.content.RevisionNotFoundException;
import org.apache.slide.lock.LockTokenNotFoundException;
import org.apache.slide.lock.NodeLock;
import org.apache.slide.security.NodePermission;
import org.apache.slide.store.ContentStore;
import org.apache.slide.store.ExtendedStore;
import org.apache.slide.store.LockStore;
import org.apache.slide.store.NodeStore;
import org.apache.slide.store.RevisionDescriptorStore;
import org.apache.slide.store.RevisionDescriptorsStore;
import org.apache.slide.store.SecurityStore;
import org.apache.slide.structure.ObjectAlreadyExistsException;
import org.apache.slide.structure.ObjectNode;
import org.apache.slide.structure.ObjectNotFoundException;
import org.apache.slide.util.Configuration;
import org.apache.slide.util.logger.Logger;

public class ContentStoreWrapper extends ExtendedStore implements
								ResourceDistributionTask.InvalidateListener {
	private DistributedContentStore store;
	private static ContentStoreWrapper current;
	
	public ContentStoreWrapper() {
		this.store = new DistributedContentStore();
		this.store.setInvalidateListener(this);
		
		super.setContentStore(this.store);
		super.setLockStore(this.store);
		super.setNodeStore(this.store);
		super.setRevisionDescriptorsStore(this.store);
		super.setRevisionDescriptorStore(this.store);
		
		super.globalCacheOff = true;
		this.store.log("Use version control = " + Configuration.useVersionControl());
		
		this.current = this;
	}
	
	public static boolean getGlobalCacheOff() {
		if (current == null) return true;
		return current.globalCacheOff;
	}
	
	public void createRevisionContent(Uri uri, NodeRevisionDescriptor desc,
			NodeRevisionContent content)
			throws ServiceAccessException,
			RevisionAlreadyExistException {
		this.store.createRevisionContent(uri, desc, content);
	}

	public void removeRevisionContent(Uri uri,
			NodeRevisionDescriptor desc)
			throws ServiceAccessException {
		this.store.removeRevisionContent(uri, desc);
	}

	public NodeRevisionContent retrieveRevisionContent(Uri uri,
			NodeRevisionDescriptor desc)
			throws ServiceAccessException,
			RevisionNotFoundException {
		super.globalCacheOff = true;
		return this.store.retrieveRevisionContent(uri, desc);
	}

	public void storeRevisionContent(Uri uri,
			NodeRevisionDescriptor desc,
			NodeRevisionContent content)
			throws ServiceAccessException,
			RevisionNotFoundException {
		this.store.storeRevisionContent(uri, desc, content);
	}
	
	public void createObject(Uri uri, ObjectNode node)
			throws ServiceAccessException, ObjectAlreadyExistsException {
		this.store.createObject(uri, node);
	}

	public void removeObject(Uri uri, ObjectNode node)
			throws ServiceAccessException, ObjectNotFoundException {
		this.store.removeObject(uri, node);
	}
	
	public ObjectNode retrieveObject(Uri uri)
			throws ServiceAccessException, ObjectNotFoundException {
		return this.store.retrieveObject(uri);
	}
	
	public void createRevisionDescriptor(Uri uri, NodeRevisionDescriptor desc) throws ServiceAccessException {
		this.store.createRevisionDescriptor(uri, desc);
	}
	
	public void storeRevisionDescriptor(Uri uri, NodeRevisionDescriptor desc)
			throws ServiceAccessException, RevisionDescriptorNotFoundException {
		this.store.storeRevisionDescriptor(uri, desc);
	}
	
	public void removeRevisionDescriptor(Uri uri, NodeRevisionNumber number) throws ServiceAccessException {
		this.store.removeRevisionDescriptor(uri, number);
	}
	
	public NodeRevisionDescriptor retrieveRevisionDescriptor(Uri uri, NodeRevisionNumber num)
			throws ServiceAccessException, RevisionDescriptorNotFoundException {
		return this.store.retrieveRevisionDescriptor(uri, num);
	}
	
	public NodeRevisionDescriptors retrieveRevisionDescriptors(Uri uri)
			throws ServiceAccessException, RevisionDescriptorNotFoundException {
		return this.store.retrieveRevisionDescriptors(uri);
	}

	public void createRevisionDescriptors(Uri uri, NodeRevisionDescriptors desc)
			throws ServiceAccessException {
		this.store.createRevisionDescriptors(uri, desc);
	}

	public void storeRevisionDescriptors(Uri uri, NodeRevisionDescriptors desc)
			throws ServiceAccessException, RevisionDescriptorNotFoundException {
		this.store.storeRevisionDescriptors(uri, desc);
	}

	public void removeRevisionDescriptors(Uri uri) throws ServiceAccessException {
		this.store.removeRevisionDescriptors(uri);
	}
	
	public void storeObject(Uri uri, ObjectNode node)
			throws ServiceAccessException, ObjectNotFoundException {
		this.store.storeObject(uri, node);
	}
	
	public Enumeration enumerateLocks(Uri uri) throws ServiceAccessException {
		return new Vector().elements();
	}

	public void killLock(Uri uri, NodeLock lock) throws ServiceAccessException,
													LockTokenNotFoundException {
	}

	public void putLock(Uri uri, NodeLock lock) throws ServiceAccessException {
	}

	public void removeLock(Uri uri, NodeLock lock) throws ServiceAccessException,
													LockTokenNotFoundException {
	}

	public void renewLock(Uri uri, NodeLock lock) throws ServiceAccessException,
													LockTokenNotFoundException {
	}

	public Enumeration enumeratePermissions(Uri uri) throws ServiceAccessException {
		return this.store.enumeratePermissions(uri);
	}

	public void grantPermission(Uri uri, NodePermission perm)
													throws ServiceAccessException {
		this.store.log("Grant " + perm + " on " + uri, 0);
	}

	public void revokePermission(Uri uri, NodePermission perm)
													throws ServiceAccessException {
		this.store.log("Revoke " + perm + " on " + uri, 0);
	}

	public void revokePermissions(Uri uri) throws ServiceAccessException {
		this.store.log("Revoke all on " + uri, 0);
	}
	
    public void setNodeStore(NodeStore nodeStore) {
    }
    
    public void setSecurityStore(SecurityStore securityStore) {
    }
    
    public void setLockStore(LockStore lockStore) {
    }
    
    public void setRevisionDescriptorsStore(RevisionDescriptorsStore r) { }
    
    public void setRevisionDescriptorStore(RevisionDescriptorStore r) { }
    
    public void setContentStore(ContentStore contentStore) { }
	
	public Logger getLogger() { return this.store; }

	public void fireInvalidate() {
		super.globalCacheOff = true;
		
		if (super.objectsCache != null) super.objectsCache.clear();
		if (super.contentCache != null) super.contentCache.clear();
		if (super.permissionsCache != null) super.permissionsCache.clear();
		if (super.descriptorCache != null) super.descriptorCache.clear();
		if (super.descriptorsCache != null) super.descriptorsCache.clear();
		if (super.locksCache != null) super.locksCache.clear();
	}
}
