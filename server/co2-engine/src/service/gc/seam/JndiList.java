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
package gc.seam;

import org.jboss.ejb3.Container;
import org.jboss.ejb3.Ejb3Registry;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.Hashtable;
import java.util.Iterator;

@Name("jndiList")
@Scope(ScopeType.APPLICATION)
public class JndiList implements Serializable {

    private void list(Context ctx, String indent, StringBuffer buffer, boolean verbose) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            NamingEnumeration ne = ctx.list("");
            while (ne.hasMore()) {
                NameClassPair pair = (NameClassPair) ne.next();

                String name = pair.getName();
                String className = pair.getClassName();
                boolean recursive = false;
                boolean isLinkRef = false;
                boolean isProxy = false;
                Class c = null;
                try {
                    c = loader.loadClass(className);

                    if (Context.class.isAssignableFrom(c))
                        recursive = true;
                    if (LinkRef.class.isAssignableFrom(c))
                        isLinkRef = true;

                    isProxy = Proxy.isProxyClass(c);
                }
                catch (ClassNotFoundException cnfe) {
                    // If this is a $Proxy* class its a proxy
                    if (className.startsWith("$Proxy")) {
                        isProxy = true;
                        // We have to get the class from the binding
                        try {
                            Object p = ctx.lookup(name);
                            c = p.getClass();
                        }
                        catch (NamingException e) {
                            Throwable t = e.getRootCause();
                            if (t instanceof ClassNotFoundException) {
                                // Get the class name from the exception msg
                                String msg = t.getMessage();
                                if (msg != null) {
                                    // Reset the class name to the CNFE class
                                    className = msg;
                                }
                            }
                        }
                    }
                }

                buffer.append(indent + " +- " + name);

                // Display reference targets
                if (isLinkRef) {
                    // Get the
                    try {
                        Object obj = ctx.lookupLink(name);

                        LinkRef link = (LinkRef) obj;
                        buffer.append("[link -> ");
                        buffer.append(link.getLinkName());
                        buffer.append(']');
                    }
                    catch (Throwable t) {
                        buffer.append("invalid]");
                    }
                }

                // Display proxy interfaces
                if (isProxy) {
                    buffer.append(" (proxy: " + pair.getClassName());
                    if (c != null) {
                        Class[] ifaces = c.getInterfaces();
                        buffer.append(" implements ");
                        for (int i = 0; i < ifaces.length; i++) {
                            buffer.append(ifaces[i]);
                            buffer.append(',');
                        }
                        buffer.setCharAt(buffer.length() - 1, ')');
                    } else {
                        buffer.append(" implements " + className + ")");
                    }
                } else if (verbose) {
                    buffer.append(" (class: " + pair.getClassName() + ")");
                }

                buffer.append('\n');
                if (recursive) {
                    try {
                        Object value = ctx.lookup(name);
                        if (value instanceof Context) {
                            Context subctx = (Context) value;
                            list(subctx, indent + " |  ", buffer, verbose);
                        } else {
                            buffer.append(indent + " |   NonContext: " + value);
                            buffer.append('\n');
                        }
                    }
                    catch (Throwable t) {
                        buffer.append("Failed to lookup: " + name + ", errmsg=" + t.getMessage());
                        buffer.append('\n');
                    }
                }
            }
            ne.close();
        }
        catch (NamingException ne) {
            buffer.append("error while listing context " + ctx.toString() + ": " + ne.toString(true));
        }
    }

    public InitialContext getInitialContext() throws Exception {
        Hashtable props = getInitialContextProperties();
        return new InitialContext(props);
    }

    private Hashtable getInitialContextProperties() {
        Hashtable props = new Hashtable();
        props.put("java.naming.factory.initial", "org.jnp.interfaces.LocalOnlyContextFactory");
        props.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
        return props;
    }

    public String getList() {

        StringBuffer buf = new StringBuffer();
        try {
            buf.append("GLOBAL JNDI\n");
            list(getInitialContext(), "    ", buf, true);
            buf.append("\n\n-------------\n");
            buf.append("java:/ Namespace\n");
            list((Context) (getInitialContext().lookup("java:/")), "    ", buf, true);
            buf.append("\n\n-------------\n");
            buf.append("EJB ENCs");
            Iterator it = Ejb3Registry.getContainers().iterator();
            while (it.hasNext()) {
                Container container = (Container) it.next();
                buf.append("\n   " + container.getObjectName().getCanonicalName() + "\n");
                list(container.getEnc(), "        ", buf, true);
            }
        } catch (Exception e) {
            buf.append(e.getMessage());
        }
        return buf.toString();
    }
}
