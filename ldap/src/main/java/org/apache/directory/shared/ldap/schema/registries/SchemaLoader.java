/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.shared.ldap.schema.registries;


import java.util.Collection;


/**
 * Loads schemas into registres.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public interface SchemaLoader
{
    /**
     * Sets listener used to notify of newly loaded schemas.
     * 
     * @param listener the listener to notify (only one is enough for us)
     */
    void setListener( SchemaLoaderListener listener );
    
    
    /**
     * Gets a schema object based on it's name.
     * 
     * @param schemaName the name of the schema to load
     * @return the Schema object associated with the name
     * @throws Exception if any problems while trying to find the associated Schema
     */
    Schema getSchema( String schemaName ) throws Exception;
    
    
    /**
     * Loads a collection of schemas.  A best effort should be made to load the dependended 
     * schemas that these schemas may rely on even if they are not included in the collection.
     * 
     * @param schemas the collection of schemas to load
     * @param registries the registries to populate with these schemas
     * @throws Exception if any kind of problems are encountered during the load
     */
    void loadWithDependencies( Collection<Schema> schemas, Registries registries ) throws Exception;
    
    
    /**
     * Loads a single schema at least and possibly it's dependencies.  
     * 
     * @param schema the schema to load
     * @param registries the registries to populate with these schemas
     * @throws Exception if any kind of problems are encountered during the load
     */
    void loadWithDependencies( Schema schema, Registries registries ) throws Exception;
    
    
    /**
     * Loads all available enabled schemas.
     *
     * @param registries the registry to load all enabled schemas into
     * @throws Exception if there are any failures
     */
    void loadAllEnabled( Registries registries ) throws Exception;
    
    
    /**
     * Loads a single schema.  Do not try to resolve dependencies while implementing this method.
     * 
     * @param schema the schema to load
     * @param registries the registries to populate with these schemas
     * @param isDepLoad tells the loader if this load request is to satisfy a dependency
     * @throws Exception if any kind of problems are encountered during the load
     */
    void load( Schema schema, Registries registries, boolean isDepLoad ) throws Exception;
}
