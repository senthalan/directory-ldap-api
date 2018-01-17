/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.directory.api.ldap.extras.controls.transaction;

import org.apache.directory.api.ldap.model.message.controls.AbstractControl;
import org.apache.directory.api.util.Strings;

/**
 * The Transaction Specification control. It's defined in RFC 5805.
 * This control is sent with every update once a transaction is started.
 * It contains the Transaction ID. 
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TransactionSpecificationImpl extends AbstractControl implements TransactionSpecification
{
    /** The Transaction Specification identifier */
    private byte[] identifier;


    /**
     * Default constructor
     */
    public TransactionSpecificationImpl()
    {
        super( OID );
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getIdentifier()
    {
        return identifier;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setIdentifier( byte[] identifier )
    {
        // Copy the byte[]
        if ( identifier != null )
        {
            this.identifier = new byte[identifier.length];
            System.arraycopy( identifier, 0, this.identifier, 0, identifier.length );
        }
    }
    
    
    /**
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        if ( identifier != null )
        {
            return "Transaction specification ID=null";
        }
        else
        {
            return "Transaction specification ID=" + Strings.dumpBytes( identifier );
        }
    }
}
