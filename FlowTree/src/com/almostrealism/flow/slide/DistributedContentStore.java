/*
 * Copyright (C) 2006  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package com.almostrealism.flow.slide ;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import org.apache.slide.common.ServiceAccessException ;
import org.apache.slide.common.Uri;
import org.apache.slide.content.NodeProperty;
import org.apache.slide.content.NodeRevisionContent;
import org.apache.slide.content.NodeRevisionDescriptor;
import org.apache.slide.content.NodeRevisionDescriptors ;
import org.apache.slide.content.NodeRevisionNumber;
import org.apache.slide.content.RevisionAlreadyExistException;
import org.apache.slide.content.RevisionDescriptorNotFoundException;
import org.apache.slide.content.RevisionNotFoundException ;
import org.apache.slide.lock.LockTokenNotFoundException;
import org.apache.slide.lock.NodeLock;
import org.apache.slide.security.NodePermission;
import org.apache.slide.store.LockStore;
import org.apache.slide.store.SecurityStore ;
import org.apache.slide.store.file.AbstractSimpleStore;
import org.apache.slide.structure.ActionNode;
import org.apache.slide.structure.ObjectAlreadyExistsException;
import org.apache.slide.structure.ObjectNode ;
import org.apache.slide.structure.ObjectNotFoundException;
import org.apache.slide.structure.SubjectNode;
import org.apache.slide.util.logger.Logger;

import com.almostrealism.flow.NetworkClient;
import com.almostrealism.flow.db.Client;
import com.almostrealism.flow.resources.DistributedResource;
import com.almostrealism.flow.resources.ResourceDistributionTask;

public class DistributedContentStore extends AbstractSimpleStore
                                    implements LockStore, SecurityStore, Logger {
    private int logl;
   
    public static boolean verbose = false, superLog = false;
    
    /** If set to true, URL codes (such as %20) will be replaced by the correct
     *  ASCII characters before requests are send the DistributedResourceTask.
     */
    public static boolean replaceUrlCodes = false;
   
    protected ResourceDistributionTask task;
    protected Hashtable cache;
    private Set create;
    private Hashtable users;
   
    public DistributedContentStore() {
        if (Client.getCurrentClient() == null) {
            System.out.println ("DefaultResourceService: Starting network client...");
//            NetworkClient.main(new String[]{"http://jrings.sf.net/node.conf"});
            NetworkClient.main(new String[0]);
        }
       
        this.task = ResourceDistributionTask.getCurrentTask();
       
        this.create = new HashSet();
        this.users = new Hashtable();
    }
   
    /**
     * Replaces the escape codes contained in the specified URL with their proper
     * ASCII characters.
     *
     * @param uri  URL to decode.
     * @return  Decoded URL.
     */
    public static String decodeUrl(String uri) {
    	if (replaceUrlCodes) {
	        uri = uri.replace("%20", " ");
	        uri = uri.replace("%3C", "<");
	        uri = uri.replace("%3E", ">");
	        uri = uri.replace("%23", "#");
	        uri = uri.replace("%25", "%");
	        uri = uri.replace("%7B", "{");
	        uri = uri.replace("%7D", "}");
	        uri = uri.replace("%7C", "|");
	        uri = uri.replace("%5E", "^");
	        uri = uri.replace("%7E", "~");
	        uri = uri.replace("%5B", "[");
	        uri = uri.replace("%5D", "]");
	        uri = uri.replace("%60", "`");
	        uri = uri.replace("%3B", ";");
	        uri = uri.replace("%2F", "/");
	        uri = uri.replace("%3F", "?");
	        uri = uri.replace("%3A", ":");
	        uri = uri.replace("%40", "@");
	        uri = uri.replace("%3D", "=");
	        uri = uri.replace("%26", "&");
	        uri = uri.replace("%24", "$");
	        uri = uri.replace("%2B", "+");
    	}
       
        return uri;
    }
   
    public void setInvalidateListener(ResourceDistributionTask.InvalidateListener l) {
        this.task.setInvalidateListener(l);
    }
   
    public void createRevisionContent(Uri uri, NodeRevisionDescriptor desc,
                                        NodeRevisionContent content)
                                        throws ServiceAccessException,
                                        RevisionAlreadyExistException {
        String suri = decodeUrl(uri.toString());
        this.logMessage("Creating " + suri);
       
        DistributedResource res = this.task.getResource(suri);
       
        if (res != null)
            throw new RevisionAlreadyExistException(uri.toString(), desc.getRevisionNumber());
       
        res = this.task.createResource(suri);
       
        try {
            this.storeRevisionContent (uri, desc, content);
        } catch (RevisionNotFoundException rnf) {
            this.logMessage("Could not load created content (" + uri + ")");
            throw new ServiceAccessException(this, "Could not create " + uri);
        }
    }
   
    public void removeRevisionContent(Uri uri,
                                        NodeRevisionDescriptor desc)
                                        throws ServiceAccessException {
        String suri = decodeUrl(uri.toString());
       
        if (this.task.deleteResource(suri))
            this.logMessage("Removed " + suri);
        else
            this.logMessage("Could not remove " + suri);
    }
   
    public NodeRevisionContent retrieveRevisionContent(Uri uri,
                                        NodeRevisionDescriptor desc)
                                        throws ServiceAccessException,
                                        RevisionNotFoundException {
        String suri = decodeUrl(uri.toString());
       
        if (this.task.isDirectory(suri)) return new NodeRevisionContent();
       
        DistributedResource res = this.task.getResource(suri);
       
        if (res == null && !this.task.isDirectory(suri))
            throw new RevisionNotFoundException(uri.toString(), desc.getRevisionNumber ());
       
        NodeRevisionContent content = new NodeRevisionContent();
        if (res != null) content.setContent(res.getInputStream());
       
        this.logMessage("Retrieve returning " + content);
       
        return content;
    }
   
    public void storeRevisionContent(Uri uri,
                                        NodeRevisionDescriptor desc,
                                        NodeRevisionContent content)
                                        throws ServiceAccessException,
                                        RevisionNotFoundException {
        String suri = decodeUrl(uri.toString());
       
        this.logMessage("Importing " + suri);
       
        DistributedResource res = this.task.getResource(suri);
       
        if (this.task.isDirectory(suri))
            if (!this.task.deleteResource(suri)) return;
        else if (res == null)
            throw new RevisionNotFoundException(uri.toString(), desc.getRevisionNumber());
       
        if (res == null)
            res = this.task.createResource(suri);
       
        if (res == null) return;
       
        try {
            res.loadFromStream(content.streamContent());
        } catch (IOException ioe) {
            throw new ServiceAccessException(this, ioe);
        }
       
        this.create.remove(uri.toString());
       
        this.logMessage("Imported " + suri);
    }
   
    public void setLoggerLevel(int l) { this.logl = l; }
    public void setLoggerLevel(String c, int l) { this.logl = l; }
    public int getLoggerLevel() { return this.logl; }
    public int getLoggerLevel(String channel) { return this.logl; }
    public boolean isEnabled(String channel, int level) { return true; }
    public boolean isEnabled(int level) { return true; }
   
    public Logger getLogger() { return this; }
   
    public void log(Object data, Throwable e, String c, int l) {
        System.out.println ("DistributedContentStore: " + data + " caused by " + c);
    }

    public void log(Object data, String c, int l) { this.log(data, l); }
   
    public void log(Object data, int l) {
        if (DistributedContentStore.verbose && DistributedContentStore.superLog)
            System.out.println("DistributedContentStore: " + data);
    }
   
    public void log(Object data) {
        if (DistributedContentStore.verbose)
            System.out.println("DistributedContentStore: " + data);
    }
   
    public void logMessage(String s) { this.log((Object) s); }
   
    public void createObject(Uri uri, ObjectNode node)
                throws ServiceAccessException, ObjectAlreadyExistsException {
        String s = decodeUrl(uri.toString());
       
        this.logMessage ("Create " + s);
       
        if (this.task.isDirectory(s))
            throw new ObjectAlreadyExistsException(uri.toString());
        else if (this.task.getResource(s) != null)
            throw new ObjectAlreadyExistsException( uri.toString());
        else if (s.startsWith("/users/"))
            return;
       
        this.create.add(s);
       
        // this.task.createDirectory(s);
    }
   
    public void storeObject(Uri uri, ObjectNode object)
                throws ServiceAccessException, ObjectNotFoundException {
        String suri = decodeUrl(uri.toString());
       
        this.logMessage("Store " + suri);
       
        if ( this.create.contains(suri)) return;
        else if (this.task.isDirectory(suri)) return;
        else if (this.task.getResource(suri) != null) return;
        else throw new ObjectNotFoundException(uri.toString());
    }
   
    public void removeObject(Uri uri, ObjectNode node)
                throws ServiceAccessException, ObjectNotFoundException {
        this.task.deleteResource(decodeUrl(uri.toString()));
    }

    public ObjectNode retrieveObject(Uri uri)
                throws ServiceAccessException, ObjectNotFoundException {
        String suri = uri.toString();
       
        if (suri.startsWith("/actions"))
            return new ActionNode(suri);
        else if (suri.startsWith("/roles"))
            return new SubjectNode(suri);
        else if (suri.startsWith("/projector"))
            return new SubjectNode(suri);
        else if (suri.startsWith("/history"))
            return new SubjectNode(suri);
        else if (suri.startsWith("/workingresource"))
            return new SubjectNode(suri);
        else if ( suri.startsWith("/workspace"))
            return new SubjectNode(suri);
       
        if (suri.startsWith("/users")) {
            if (this.users.containsKey(suri)) {
                return (SubjectNode) this.users.get(suri);
            } else {
                SubjectNode s = new SubjectNode(suri);
                this.users.put(suri, s);
                return s;
            }
        }
       
        String duri = decodeUrl(suri);
       
        DistributedResource res = this.task.getResource(duri);
        SubjectNode s = new SubjectNode(suri);
        s.setUri(suri.toString());
        if (res != null) return s;
       
        if (this.task.isDirectory(duri)) {
            String c[] = this.task.getChildren(duri);
           
            for (int i = 0; i < c.length; i++) {
                // c[i] = c[i].substring(c[i].lastIndexOf("/") + 1);
                // this.log("Child " + c[i]);
                SubjectNode cn = new SubjectNode(c[i], new Vector(), new Vector(), new Vector());
                cn.setUri(c[i].toString());
                s.addChild(cn);
            }
           
            this.create.remove(uri.toString());
        } else if (!this.create.contains(uri.toString())) {
            throw new ObjectNotFoundException(uri);
        }
       
        // this.log(s);
       
        return s;
    }
   
    public void storeRevisionDescriptor(Uri uri, NodeRevisionDescriptor desc)
            throws ServiceAccessException, RevisionDescriptorNotFoundException {
        try {
            this.storeObject(uri, null);
        } catch (ObjectNotFoundException onfe) {
            this.create.add(uri.toString());
        }
    }
   
    public NodeRevisionDescriptor retrieveRevisionDescriptor(Uri uri, NodeRevisionNumber num)
                throws ServiceAccessException, RevisionDescriptorNotFoundException {
        NodeRevisionDescriptor r =
            new NodeRevisionDescriptor(new NodeRevisionNumber(1, 0),
                                        NodeRevisionDescriptors.MAIN_BRANCH,
                                        new Vector(), new Hashtable());
       
        String suri = decodeUrl(uri.toString());
       
        DistributedResource res = this.task.getResource(suri);
       
        r.setCreationDate(new Date());
        r.setLastModified(new Date());
        r.setModificationDate(new Date());
       
        NodeProperty p = new NodeProperty( r.RESOURCE_TYPE, "<collection/>");
       
        if (this.task.isDirectory(suri)) {
            r.setContentType(NodeRevisionDescriptor.COLLECTION_TYPE);
            r.setProperty(p);
        } else if (res != null) {
            r.setContentLength(res.getTotalBytes());
            r.setContentType(NodeRevisionDescriptor.CONTENT_TYPE);
            r.setProperty(r.RESOURCE_TYPE, "getcontenttype");
        } else if (!this.create.contains( uri.toString())){
            throw new RevisionDescriptorNotFoundException(uri.toString());
        }
       
        String len = " ";
        long l = r.getContentLength();
        if (l >= 0) len = " (" + l + ") ";
       
        this.logMessage("Retrieved " + uri.toString() + len + r.getContentType() + " " +
                r.getProperty(p.getName()) + " " +
                r.getContentLength ());
       
       
        return r;
    }
   
    public NodeRevisionDescriptors retrieveRevisionDescriptors(Uri uri)
            throws ServiceAccessException, RevisionDescriptorNotFoundException {
        // this.log("Retrieve descriptors " + uri);
       
        String s = decodeUrl(uri.toString());
        if (this.task.isDirectory(s));
        else if (this.task.getResource(s) != null);
        else if (this.create.contains(uri.toString()));
        else throw new RevisionDescriptorNotFoundException(uri.toString());
       
        return super.retrieveRevisionDescriptors(uri);
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
        this.log("Check permission " + uri, 0);
       
        Vector v = new Vector();
       
        v.addElement(new NodePermission(uri.toString(), "all", "all"));
        v.addElement(new NodePermission(uri.toString(), "all",
                        uri.getNamespace().getConfig().getReadObjectAction().getUri()));
        v.addElement(new NodePermission(uri.toString(), "all",
                uri.getNamespace().getConfig().getCreateRevisionContentAction().getUri()));
        v.addElement(new NodePermission(uri.toString (), "all",
                uri.getNamespace().getConfig().getCreateObjectAction().getUri()));
        v.addElement(new NodePermission(uri.toString(), "all",
                uri.getNamespace().getConfig().getCreateRevisionMetadataAction().getUri()));
       
        return v.elements();
    }

    public void grantPermission(Uri uri, NodePermission perm)
                                                    throws ServiceAccessException {
    }

    public void revokePermission(Uri uri, NodePermission perm)
                                                    throws ServiceAccessException {
    }

    public void revokePermissions(Uri uri) throws ServiceAccessException {
    }
}