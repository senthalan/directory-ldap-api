/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.shared.ldap.schema.registries;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NamingException;

import org.apache.directory.shared.asn1.primitives.OID;
import org.apache.directory.shared.ldap.schema.SchemaObject;
import org.apache.directory.shared.ldap.schema.SchemaObjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Common schema object registry interface.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class SchemaObjectRegistry<T extends SchemaObject> implements Iterable<T>
{
    /** static class logger */
    private static final Logger LOG = LoggerFactory.getLogger( SchemaObjectRegistry.class );

    /** A speedup for debug */
    private static final boolean DEBUG = LOG.isDebugEnabled();
    
    /** a map of SchemaObject looked up by OID */
    protected final Map<String, T> byOid;
    
    /** a map of SchemaObject looked up by name */
    protected final Map<String, T> byName;
    
    /** The SchemaObject type */
    protected SchemaObjectType type;

    /** the global OID Registry */
    protected final OidRegistry oidRegistry;
    

    /**
     * Creates a new SchemaObjectRegistry instance.
     */
    protected SchemaObjectRegistry( SchemaObjectType schemaObjectType, OidRegistry oidRegistry )
    {
        byOid = new ConcurrentHashMap<String, T>();
        byName = new ConcurrentHashMap<String, T>();
        type = schemaObjectType;
        this.oidRegistry = oidRegistry;
    }
    
    
    /**
     * Checks to see if an SchemaObject exists in the registry, by its
     * OID or name. 
     * 
     * @param oid the object identifier or name of the SchemaObject
     * @return true if a SchemaObject definition exists for the oid, false
     * otherwise
     */
    public boolean contains( String oid )
    {
        return byOid.containsKey( oid );
    }
    
    
    /**
     * Gets the name of the schema this schema object is associated with.
     *
     * @param id the object identifier or the name
     * @return the schema name
     * @throws NamingException if the schema object does not exist
     */
    public String getSchemaName( String oid ) throws NamingException
    {
        if ( ! OID.isOID( oid ) )
        {
            String msg = "Looks like the arg is not a numeric OID";
            LOG.warn( msg );
            throw new NamingException( msg );
        }
        
        SchemaObject schemaObject = byOid.get( oid );

        if ( schemaObject != null )
        {
            return schemaObject.getSchemaName();
        }
        
        String msg = "OID " + oid + " not found in oid to schema name map!";
        LOG.warn( msg );
        throw new NamingException( msg );
    }

    
    /**
     * Modify all the SchemaObject using a schemaName when this name changes.
     *
     * @param originalSchemaName The original Schema name
     * @param newSchemaName The new Schema name
     */
    public void renameSchema( String originalSchemaName, String newSchemaName )
    {
        // Loop on all the SchemaObjects stored and remove those associated
        // with the give schemaName
        for ( T schemaObject : this )
        {
            if ( originalSchemaName.equalsIgnoreCase( schemaObject.getSchemaName() ) )
            {
                schemaObject.setSchemaName( newSchemaName );
                
                if ( DEBUG )
                {
                    LOG.debug( "Renamed {} schemaName to {}", schemaObject, newSchemaName );
                }
            }
        }
    }
    
    
    /**
     * Gets an iterator over the registered schema objects in the registry.
     *
     * @return an Iterator of homogeneous schema objects
     */
    public Iterator<T> iterator()
    {
        return byOid.values().iterator();
    }

    
    /**
     * Gets an iterator over the registered schema objects'OID in the registry.
     *
     * @return an Iterator of OIDs
     */
    public Iterator<String> oidsIterator()
    {
        return byOid.keySet().iterator();
    }

    
    /**
     * Looks up a SchemaObject by its unique Object Identifier or by name.
     *
     * @param oid the object identifier or name
     * @return the SchemaObject instance for the id
     * @throws NamingException if the SchemaObject does not exist
     */
    public T lookup( String oid ) throws NamingException
    {
        if ( oid == null )
        {
            return null;
        }
        
        if ( ! OID.isOID( oid ) )
        {
        	oid = getOidByName( oid );
        }
        
        T schemaObject = byOid.get( oid );

        if ( schemaObject == null )
        {
            String msg = type.name() + " for OID " + oid + " does not exist!";
            LOG.debug( msg );
            throw new NamingException( msg );
        }

        if ( DEBUG )
        {
    		LOG.debug( "Found {} with oid: {}", schemaObject, oid );
        }
        
        return schemaObject;
    }
    
    
    /**
     * Registers a new SchemaObject with this registry.
     *
     * @param schemaObject the SchemaObject to register
     * @throws NamingException if the SchemaObject is already registered or
     * the registration operation is not supported
     */
    public void register( T schemaObject ) throws NamingException
    {
        String oid = schemaObject.getOid();
        
        if ( byOid.containsKey( oid ) )
        {
            String msg = type.name() + " with OID " + oid + " already registered!";
            LOG.warn( msg );
            throw new NamingException( msg );
        }

        byOid.put( oid, schemaObject );
        
        /*
         * add the aliases/names to the name map along with their toLowerCase
         * versions of the name: this is used to make sure name lookups work
         */
        for ( String name : schemaObject.getNames() )
        {
        	byName.put( name, schemaObject );
        	byName.put( name.toLowerCase(), schemaObject );
        }
        
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "registered " + schemaObject.getName() + " for OID {}", oid );
        }
        
        // And register the oid -> schemaObject relation
        oidRegistry.register( schemaObject );
    }


    /**
     * Removes the SchemaObject registered with this registry, using its
     * numeric OID.
     * 
     * @param numericOid the numeric identifier
     * @throws NamingException if the numeric identifier is invalid
     */
    public void unregister( String numericOid ) throws NamingException
    {
        if ( !OID.isOID( numericOid ) )
        {
            String msg = "OID " + numericOid + " is not a numeric OID";
            LOG.error( msg );
            throw new NamingException( msg );
        }

        SchemaObject schemaObject = byOid.remove( numericOid );
        
        /* byName Cleanup
         * --------------
         * 
         * We iterate throw all the Entry objects in the byName hash.  This
         * costs a bit since it is a full scan of the hash but it's in memory
         * and can up to 10K objects (well within our range) work reasonably.
         * As we iterate we push keys to remove into the keysToRemove list to
         * be removed later after this first iteration to prevent concurrent
         * modification issues during iteration.
         * 
         * Next we loop again on the keysToRemove list and remove the entries
         * from the byName map.  This seems elaborate however note that 
         * because we cash different case varying permutations of the name so
         * lookups work properly, we cannot just rely on removing entry by 
         * key as they are in the names list.  This is why we have to scan the
         * entire entry set and check to see if it is in fact the schema 
         * object in question.
         */
        List<String> keysToRemove = new ArrayList<String>(); 
        for ( Entry<String, T> entry : byName.entrySet() )
        {
        	if ( entry.getValue().equals( schemaObject ) )
        	{
        		keysToRemove.add( entry.getKey() );
        	}
        }
        for ( String key : keysToRemove )
        {
        	byName.remove( key );
        }
        
        // And remove the SchemaObject from the oidRegistry
        oidRegistry.unregister( numericOid );
        
        if ( DEBUG )
        {
            LOG.debug( "Removed {} with oid {} from the registry", schemaObject, numericOid );
        }
    }
    
    
    /**
     * Unregisters all SchemaObjects defined for a specific schema from
     * this registry.
     * 
     * @param schemaName the name of the schema whose SchemaObjects will be removed from
     */
    public void unregisterSchemaElements( String schemaName ) throws NamingException
    {
        if ( schemaName == null )
        {
            return;
        }
        
        // Loop on all the SchemaObjects stored and remove those associated
        // with the give schemaName
        for ( T schemaObject : this )
        {
            if ( schemaName.equalsIgnoreCase( schemaObject.getSchemaName() ) )
            {
                String oid = schemaObject.getOid();
                SchemaObject removed = byOid.remove( oid );
                oidRegistry.unregister( oid );
                
                if ( DEBUG )
                {
                    LOG.debug( "Removed {} with oid {} from the registry", removed, oid );
                }
            }
        }
    }
    
    
    /**
     * Gets the numericOid for a name/alias if one is associated.  To prevent
     * lookup failures due to case variance in the name, a failure to lookup the
     * OID, will trigger a lookup using a lower cased version of the name and 
     * the name that failed to match will automatically be associated with the
     * OID.
     * 
     * @param name The name we are looking the oid for
     * @return The numericOID associated with this name
     * @throws NamingException If the OID can't be found
     */
    public String getOidByName( String name ) throws NamingException
    {
    	if ( ! byName.containsKey( name ) )
    	{
    		// last resort before giving up check with lower cased version
        	String lowerCased = name.toLowerCase();
    		
        	// ok this name is not for a schema object in the registry
    		if ( ! byName.containsKey( lowerCased ) )
    		{
    	        throw new NamingException( "Can't find an OID for the name " + name );
    		}

    		// we found the schema object using lower cased name
			T schemaObject = byName.get( lowerCased );
			
        	// provided name argument has case variance so we add it to 
			// map in case the same lookup is attempted again in future
			byName.put( name, schemaObject );
			return schemaObject.getOid();
    	}
    	
    	// we found the schema object by key on the first lookup attempt
        T schemaObject = byName.get( name );
        return schemaObject.getOid();
    }


    /**
     * Checks to see if an alias/name is associated with an OID and it's 
     * respective schema object in this registry.  Unlike the getOidByName()
     * method this method does not throw an exception when the name is not
     * found.
     * 
     * @param name The name we are looking for
     * @return true if the name or it's cannonical form is mapped to a 
     * schemaObject in this registry, false otherwise.
     * @throws NamingException If the OID can't be found
     */
    public boolean containsName( String name ) throws NamingException
    {
    	if ( ! byName.containsKey( name ) )
    	{
    		// last resort before giving up check with lower cased version
        	return byName.containsKey( name.toLowerCase() );
    	}

        return true;
    }
}
