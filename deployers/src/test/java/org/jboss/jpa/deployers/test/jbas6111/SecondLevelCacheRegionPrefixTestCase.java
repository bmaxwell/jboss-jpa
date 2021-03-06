/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.jpa.deployers.test.jbas6111;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.jpa.deployers.test.common.MockRegionFactory;
import org.jboss.jpa.deployers.test.common.PersistenceTestCase;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.junit.After;
import org.junit.Test;

/**
 * Test for JBAS-6111 compliance.
 * 
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class SecondLevelCacheRegionPrefixTestCase extends PersistenceTestCase
{
   @After
   public void after() throws Exception
   {
      MockRegionFactory.SESSION_FACTORY_PROPERTIES.clear();
   }
   
   @Test
   public void testRegionPrefixes() throws Exception
   {
      String spec = "/org/jboss/jpa/deployers/test/jbas6111/pu";
      URL url = getClass().getResource(spec);
      VirtualFile file = VFS.getRoot(url);
      VFSDeployment deployment = VFSDeploymentFactory.getInstance().createVFSDeployment(file);
      delegate.getMainDeployer().deploy(deployment);
      
      try
      {         
         assertEquals("Expected number of property sets found", 2, MockRegionFactory.SESSION_FACTORY_PROPERTIES.size());
         Set<String> regionPrefixes = new HashSet<String>();
         for (Properties properties : MockRegionFactory.SESSION_FACTORY_PROPERTIES)
         {
            regionPrefixes.add(properties.getProperty("hibernate.cache.region_prefix"));               
         }
         
         assertTrue("Synthetic region prefix created", regionPrefixes.contains("persistence.unit:unitName=#noprefix"));
         assertTrue("Declared region prefix respected", regionPrefixes.contains("a_prefix")); 
      }
      finally
      {      
         delegate.getMainDeployer().undeploy(deployment);
      }
   }
}
