/**
 * created May 19, 2006
 *
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * Copyright 2006 Marc Woerlein
 *
 * This file is part of parsemis.
 *
 * Licence:
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package eu.unitn.disi.db.resum.multithread;

import eu.unitn.disi.db.resum.utilities.Settings;
import java.io.Serializable;
import java.util.ArrayList;
import sa.edu.kaust.grami.dataStructures.Generic;
import sa.edu.kaust.grami.dataStructures.HPListGraph;

/**
 * This class is for locally storing all final settings, the database and the
 * corresponding relabeling functions.
 *
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * @param <NodeType> the type of the node labels (will be hashed and checked
 * with .equals(..))
 * @param <EdgeType> the type of the edge labels (will be hashed and checked
 * with .equals(..))
 */
public class LocalEnvironment<NodeType, EdgeType> implements Serializable {

    private static final long serialVersionUID = 1L;

    public transient static LocalEnvironment environ;

    private static long CLK_TICKS = 100;
    
    private static native long getClockTicks();

    private static native long getCPUtime();
    
    /**
     * the minimal size of a stack to be splitable
     */
    public final int splitSize;
    /**
     * the maximal depth of nodes transfered with a stack split
     */
    public final int maxSplitDepth;
    /**
     * the maximal number of nodes transfered with a stack split
     */
    public final int maxSplitCount;
    
    transient public MiningStack<NodeType, EdgeType>[] stack;

    ArrayList<HPListGraph<NodeType, EdgeType>> returnSet = null;

    private transient GThreadEnvironment<NodeType, EdgeType>[] threadEnvs;

    private final int threadCount;
    

    static {
        try {
            System.loadLibrary("java-time");
            CLK_TICKS = getClockTicks();
        } catch (final UnsatisfiedLinkError e) {
        }
    }

    /**
     * @return the current CPU-time of the java process
     */
    public static long currentCPUMillis() {
        try {
            return (getCPUtime() * 1000) / CLK_TICKS;
        } catch (final UnsatisfiedLinkError e) {
            return System.currentTimeMillis();
        }
    }
    
    
    private LocalEnvironment() {
        this.splitSize = Settings.splitSize;
        this.maxSplitCount = Settings.maxSplitCount;
        this.maxSplitDepth = Settings.maxSplitDepth;
        this.threadCount = Settings.threadCount;
        this.threadEnvs = new GThreadEnvironment[threadCount];
        this.stack = new MiningStack[threadCount];
    }

    /**
     * creates and sets the local environment
     *
     * @param <NodeType>
     * @param <EdgeType>
     * @return the newly created environment
     */
    public final static <NodeType, EdgeType> LocalEnvironment<NodeType, EdgeType> create() {
        final LocalEnvironment<NodeType, EdgeType> n = new LocalEnvironment<NodeType, EdgeType>();
        environ = n;
        return n;
    }

    /**
     * @param <NodeType>
     * @param <EdgeType>
     * @param dummy to determine generic types
     * @return the current local environment
     */
    public final static <NodeType, EdgeType> LocalEnvironment<NodeType, EdgeType> env(final Generic<NodeType, EdgeType> dummy) {
        return environ;
    }

    /**
     * sets the local environment to the given one used to initialize the local
     * environments on remote machines of the JavaParty environment
     *
     * @param env
     * @param <NodeType> the type of the node labels
     * @param <EdgeType> the type of the edge labels
     * @return the newly set environment
     */
    public final static <NodeType, EdgeType> LocalEnvironment<NodeType, EdgeType> set(
            final LocalEnvironment<NodeType, EdgeType> env) {
        if (env.threadEnvs == null) {
            env.threadEnvs = new GThreadEnvironment[env.threadCount];
        }
        if (env.stack == null) {
            env.stack = new MiningStack[env.threadCount];
        }
        LocalEnvironment.environ = env;
        return environ;
    }

    public final ArrayList<HPListGraph<NodeType, EdgeType>> getReturnSet() {
        if (returnSet == null) {
            returnSet = new ArrayList<HPListGraph<NodeType, EdgeType>> ();
        }
        return returnSet;
    }

    /**
     * @param idx
     * @return the ThreadEnvironment connected with the given index
     */
    public GThreadEnvironment<NodeType, EdgeType> getThreadEnv(final int idx) {
        if (threadEnvs == null) {
            synchronized (this) {
                if (threadEnvs == null) {
                    final GThreadEnvironment<NodeType, EdgeType>[] t = new GThreadEnvironment[threadCount];
                    threadEnvs = t;
                }
            }
        }
        synchronized (threadEnvs) {
            if (threadEnvs[idx] == null) {
                final GThreadEnvironment<NodeType, EdgeType> tenv = new GThreadEnvironment(idx);
                threadEnvs[idx] = tenv;
                return tenv;
            } else {
                return threadEnvs[idx];
            }
        }
    }

    public final void setReturnSet(final ArrayList<HPListGraph<NodeType, EdgeType>> set) {
        returnSet = set;
    }
}
