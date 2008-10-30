/**
* This file is part of AMEE.
*
* AMEE is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 3 of the License, or
* (at your option) any later version.
*
* AMEE is free software and is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
* Created by http://www.dgen.net.
* Website http://www.amee.cc
*/
//$Id: Ejb.java,v 1.15 2006/04/15 22:28:06 gavin Exp $
package gc.seam;


/**
 * A seam component that bootstraps the embedded EJB container
 *
 * @author Gavin King
 */
//@Scope(ScopeType.APPLICATION)
//@Intercept(NEVER)
//@Startup
//@Name("org.jboss.seam.core.ejb")
//public class Ejb {
//    private static final Log log = LogFactory.getLog(Ejb.class);
//
//    private EJB3StandaloneDeployer deployer;
//
//    @Create
//    public void startup() throws Exception {
//        log.info("starting the embedded EJB container");
//        EJB3StandaloneBootstrap.boot(null);
//        deploy("META-INF/jboss-beans.xml");
//        deploy("jboss-beans.xml");
//
//        deployer = EJB3StandaloneBootstrap.createDeployer();
//        //deployer.getArchivesByResource().add("seam.properties");
//
//        // need to set the InitialContext properties that deployer will use
//        // to initial EJB containers
//        deployer.setJndiProperties(Naming.getInitialContextProperties());
//
//        deployer.create();
//        deployer.start();
//        EJB3StandaloneBootstrap.scanClasspath();
//    }
//
//    private void deploy(String name) {
//        if (Thread.currentThread().getContextClassLoader().getResource(name) != null) {
//            EJB3StandaloneBootstrap.deployXmlResource(name);
//        }
//    }
//
//    @Destroy
//    public void shutdown() throws Exception {
//        log.info("stopping the embedded EJB container");
//        deployer.stop();
//        deployer.destroy();
//        deployer = null;
//        EJB3StandaloneBootstrap.shutdown();
//    }
//
//}
