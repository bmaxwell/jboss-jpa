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
package org.jboss.jpa.deployers.test.deployment;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.deployers.client.spi.IncompleteDeploymentException;
import org.jboss.deployers.plugins.attachments.AttachmentsImpl;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.jpa.deployers.test.common.PersistenceTestCase;
import org.jboss.jpa.spi.PersistenceUnit;
import org.jboss.jpa.spi.PersistenceUnitRegistry;
import org.jboss.metadata.jpa.spec.PersistenceMetaData;
import org.jboss.metadata.jpa.spec.PersistenceUnitMetaData;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class DeploymentTestCase extends PersistenceTestCase
{
   @Test
   public void test1() throws Exception
   {
      String spec = "/org/jboss/jpa/deployers/test/deployment/pu";
      URL url = getClass().getResource(spec);
      VirtualFile file = VFS.getRoot(url);
      VFSDeployment deployment = VFSDeploymentFactory.getInstance().createVFSDeployment(file);
      delegate.getMainDeployer().deploy(deployment);
      //delegate.getMainDeployer().checkComplete(deployment);
      
      String name = "persistence.unit:unitName=#dummy";
      PersistenceUnit pu = delegate.getBean(name, PersistenceUnit.class);
      assertNotNull(pu);
      
      assertNotNull("Persistence unit not found in PersistenceUnitRegistry", PersistenceUnitRegistry.getPersistenceUnit(name));
      
      delegate.getMainDeployer().undeploy(deployment);
      
      assertNull("Persistence unit still found in PersistenceUnitRegistry", PersistenceUnitRegistry.getPersistenceUnit(name));
   }

   /**
    * See EJB 3.0 6.3 page 145 attribute name is required.
    */
   @Test
   public void testUnnamed() throws Exception
   {
      String spec = "/org/jboss/jpa/deployers/test/deployment/unnamed";
      URL url = getClass().getResource(spec);
      VirtualFile file = VFS.getRoot(url);
      
      // Force an illegal set of meta data
      PersistenceUnitMetaData persistenceUnit = new PersistenceUnitMetaData();
      List<PersistenceUnitMetaData> persistenceUnits = new ArrayList<PersistenceUnitMetaData>();
      persistenceUnits.add(persistenceUnit);
      PersistenceMetaData attachment = new PersistenceMetaData();
      attachment.setPersistenceUnits(persistenceUnits);
      MutableAttachments attachments = new AttachmentsImpl();
      attachments.addAttachment(PersistenceMetaData.class, attachment);
      
      VFSDeployment deployment = VFSDeploymentFactory.getInstance().createVFSDeployment(file);
      deployment.setPredeterminedManagedObjects(attachments);
      try
      {
         delegate.getMainDeployer().deploy(deployment);
         fail("It should not have been possible to deploy with a persistence unit name");
      }
      catch(IncompleteDeploymentException e)
      {
         Throwable t = e.getIncompleteDeployments().getDeploymentsInError().values().iterator().next();
         assertTrue(t.getCause().getMessage().startsWith("Persistence unit is unnamed"));
      }
   }
}
