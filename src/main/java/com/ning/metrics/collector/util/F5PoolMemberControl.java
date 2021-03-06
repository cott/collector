/*
 * Copyright 2010-2011 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.metrics.collector.util;

import com.google.inject.Inject;
import com.ning.metrics.collector.binder.config.CollectorConfig;
import iControl.CommonEnabledState;
import iControl.CommonIPPortDefinition;
import iControl.CommonStatistic;
import iControl.CommonStatisticType;
import iControl.CommonULong64;
import iControl.Interfaces;
import iControl.LocalLBObjectStatus;
import iControl.LocalLBPoolMemberMemberMonitorState;
import iControl.LocalLBPoolMemberMemberObjectStatus;
import iControl.LocalLBPoolMemberMemberSessionState;
import iControl.LocalLBPoolMemberMemberStatisticEntry;
import iControl.LocalLBPoolMemberMemberStatistics;
import org.apache.axis.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.weakref.jmx.Managed;

import java.util.ArrayList;

/**
 * Control the membership to a f5 BIG-IP SLB VIP.
 * This class is meant to be used via JMX.
 * <p/>
 * For environments where JMX is not secure, the password can be passed as a system property.
 * <p/>
 * Inspired by http://devcentral.f5.com/wiki/default.aspx/iControl/JavaPoolMemberControl.html
 */
public class F5PoolMemberControl
{
    private static final Logger log = Logger.getLogger(F5PoolMemberControl.class);

    private final String hostname;
    private final String username;
    private final String password;
    private final String poolName;

    @Inject
    public F5PoolMemberControl(CollectorConfig config)
    {
        hostname = config.getF5Hostname();
        username = config.getF5Username();
        password = config.getF5Password();
        poolName = config.getF5PoolName();
    }

    private static Interfaces getInterface(String hostname, String username, String password)
    {
        Interfaces m_interfaces = new Interfaces();
        m_interfaces.initialize(hostname, username, password);

        return m_interfaces;
    }

    /**
     * Return a list of all pools (use the parameters specified in the system properties)
     *
     * @return a list of pool names
     * @throws Exception generic exception if something goes wrong
     */
    @Managed(description = "Get the list of available pools")
    public String[] getPoolList() throws Exception
    {
        return getPoolList(hostname, username, password);
    }

    /**
     * Return a list of all pools
     *
     * @param hostname SLB VIP hostname
     * @param username SLB VIP username
     * @param password SLB VIP password
     * @return a list of pool names
     * @throws Exception generic exception if something goes wrong
     */
    @Managed(description = "Get the list of available pools")
    public String[] getPoolList(String hostname, String username, String password) throws Exception
    {
        log.info(String.format("Retrieving pool list for %s (username: %s)", hostname, username));
        try {
            return getInterface(hostname, username, password).getLocalLBPool().get_list();
        }
        catch (AxisFault e) {
            log.warn(String.format("Unable to get pool list: %s", e.getFaultString()));
            throw e;
        }
        catch (Exception e) {
            log.warn(String.format("Unable to get pool list: %s", e.getLocalizedMessage()));
            throw e;
        }
    }

    /**
     * Given a pool name, retrieve the pool members definitions (use the parameters specified in the system properties)
     *
     * @return list of CommonIPPortDefinition for all members
     * @throws Exception generic exception if something goes wrong
     */
    @Managed(description = "Get the list of members in a pool")
    public String[] getPoolMembers() throws Exception
    {
        return getPoolMembers(poolName, hostname, username, password);
    }

    /**
     * Given a pool name, retrieve the pool members definitions
     *
     * @param poolName pool name to lookup
     * @param hostname SLB VIP hostname
     * @param username SLB VIP username
     * @param password SLB VIP password
     * @return list of CommonIPPortDefinition for all members
     * @throws Exception generic exception if something goes wrong
     */
    @Managed(description = "Get the list of members in a pool")
    public String[] getPoolMembers(String poolName, String hostname, String username, String password) throws Exception
    {
        log.info(String.format("Retrieving pool members for %s (hostname: %s, username: %s)", poolName, hostname, username));

        String[] poolNames = {poolName};
        CommonIPPortDefinition[][] membersDefinitions;
        try {
            membersDefinitions = getInterface(hostname, username, password).getLocalLBPool().get_member(poolNames);
        }
        catch (AxisFault e) {
            log.warn(String.format("Unable to get pool member: %s", e.getFaultString()));
            throw e;
        }
        catch (Exception e) {
            log.warn(String.format("Unable to get pool member: %s", e.getLocalizedMessage()));
            throw e;
        }
        log.info(String.format("Found pool members for %s (hostname: %s, username: %s): %s", poolName, hostname, username, toString(membersDefinitions)));

        ArrayList<String> res = new ArrayList<String>();
        for (CommonIPPortDefinition[] memberDefinitions : membersDefinitions) {
            for (CommonIPPortDefinition memberDefinition : memberDefinitions) {
                res.add(String.format("%s:%d", memberDefinition.getAddress(), memberDefinition.getPort()));
            }
        }

        return res.toArray(new String[res.size()]);
    }

    /**
     * Find the status of a member (use the parameters specified in the system properties)
     *
     * @param memberAddress server IP to lookup
     * @return list of CommonIPPortDefinition for all members
     * @throws Exception generic exception if something goes wrong
     */
    @Managed(description = "Get the list of members in a pool")
    public String[] getPoolMemberStatuses(String memberAddress) throws Exception
    {
        return getPoolMemberStatuses(memberAddress, poolName, hostname, username, password);
    }

    /**
     * Find the status of a member
     *
     * @param memberAddress server IP to lookup
     * @param poolName      pool name to lookup
     * @param hostname      SLB VIP hostname
     * @param username      SLB VIP username
     * @param password      SLB VIP password
     * @return list of CommonIPPortDefinition for all members
     * @throws Exception generic exception if something goes wrong
     */
    @Managed(description = "Get the list of members in a pool")
    public String[] getPoolMemberStatuses(String memberAddress, String poolName, String hostname, String username, String password) throws Exception
    {
        String[] poolNames = {poolName};
        ArrayList<String> memberStatuses = new ArrayList<String>();

        log.info(String.format("Retrieving pool member status for %s (pool: %s, hostname: %s, username: %s)", memberAddress, poolName, hostname, username));
        LocalLBPoolMemberMemberObjectStatus[][] members;
        try {
            members = getInterface(hostname, username, password).getLocalLBPoolMember().get_object_status(poolNames);
        }
        catch (AxisFault e) {
            log.warn(String.format("Unable to get pool member: %s", e.getFaultString()));
            throw e;
        }
        catch (Exception e) {
            log.warn(String.format("Unable to get pool member: %s", e.getLocalizedMessage()));
            throw e;
        }

        for (LocalLBPoolMemberMemberObjectStatus[] statuses : members) {
            for (LocalLBPoolMemberMemberObjectStatus status : statuses) {
                if (status.getMember().getAddress().equals(memberAddress)) {
                    memberStatuses.add(toString(status.getObject_status()));
                }
            }
        }

        log.info(String.format("Found pool member status for %s (pool: %s, hostname: %s, username: %s): %s", memberAddress, poolName, hostname, username, memberStatuses));
        return memberStatuses.toArray(new String[memberStatuses.size()]);
    }


    /**
     * Add a server to a pool (use the parameters specified in the system properties)
     *
     * @param memberAddress server IP to add
     * @param memberPort    server port
     * @return the new status of the server
     * @throws Exception generic exception if something goes wrong
     */
    @Managed(description = "Add a member to a pool")
    public String[] addPoolMember(String memberAddress, int memberPort) throws Exception
    {
        return addPoolMember(memberAddress, memberPort, poolName, hostname, username, password);
    }

    /**
     * Add a server to a pool
     *
     * @param memberAddress server IP to add
     * @param memberPort    server port
     * @param poolName      pool name
     * @param hostname      SLB VIP hostname
     * @param username      SLB VIP username
     * @param password      SLB VIP password
     * @return the new status of the server
     * @throws Exception generic exception if something goes wrong
     */
    @Managed(description = "Add a member to a pool")
    public String[] addPoolMember(String memberAddress, int memberPort, String poolName, String hostname, String username, String password) throws Exception
    {
        final Interfaces m_interfaces = getInterface(hostname, username, password);
        final CommonIPPortDefinition definition = new CommonIPPortDefinition(memberAddress, (long) memberPort);

        String[] poolNames = {poolName};
        CommonIPPortDefinition[] definitions = {definition};
        CommonIPPortDefinition[][] membersDefinitions = {definitions};
        try {
            m_interfaces.getLocalLBPool().add_member(poolNames, membersDefinitions);
            log.info(String.format("Added %s to pool %s (hostname: %s, username: %s)", memberAddress, poolName, hostname, username));
        }
        catch (AxisFault e) {
            log.warn(String.format("Unable to add pool member: %s", e.getFaultString()));
            throw e;
        }
        catch (Exception e) {
            log.warn(String.format("Unable to add pool member: %s", e.getLocalizedMessage()));
            throw e;
        }

        return getPoolMemberStatuses(poolName, memberAddress, hostname, username, password);
    }

    /**
     * Enable a server in a pool (use the parameters specified in the system properties)
     * The server needs to be a member of the pool already.
     *
     * @param memberAddress server IP to add
     * @param memberPort    server port
     * @return the new status of the server
     * @throws Exception generic exception if something goes wrong
     */
    @Managed(description = "Enable a member in a pool")
    public String[] enablePoolMember(String memberAddress, int memberPort) throws Exception
    {
        return enablePoolMember(memberAddress, memberPort, poolName, hostname, username, password);
    }

    /**
     * Enable a server in a pool
     * The server needs to be a member of the pool already.
     *
     * @param memberAddress server IP to add
     * @param memberPort    server port
     * @param poolName      pool name
     * @param hostname      SLB VIP hostname
     * @param username      SLB VIP username
     * @param password      SLB VIP password
     * @return the new status of the server
     * @throws Exception generic exception if something goes wrong
     */
    @Managed(description = "Enable a member in a pool")
    public String[] enablePoolMember(String memberAddress, int memberPort, String poolName, String hostname, String username, String password) throws Exception
    {
        final Interfaces m_interfaces = getInterface(hostname, username, password);
        final CommonIPPortDefinition definition = new CommonIPPortDefinition(memberAddress, (long) memberPort);

        log.info(String.format("Enabling %s to pool %s (hostname: %s, username: %s)", memberAddress, poolName, hostname, username));

        log.info(String.format("Setting monitor state to ENABLED for %s in pool %s (hostname: %s, username: %s)", memberAddress, poolName, hostname, username));
        setMonitorState(m_interfaces, definition, poolName, CommonEnabledState.STATE_ENABLED);
        log.info(String.format("Setting session state to ENABLED for %s in pool %s (hostname: %s, username: %s)", memberAddress, poolName, hostname, username));
        setSessionState(m_interfaces, definition, poolName, CommonEnabledState.STATE_ENABLED);

        return getPoolMemberStatuses(poolName, memberAddress, hostname, username, password);
    }

    /**
     * Disable a server from a pool (use the parameters specified in the system properties)
     *
     * @param memberAddress server IP to remove
     * @param memberPort    server port
     * @return the new status of the server
     * @throws Exception generic exception if something goes wrong
     */
    @Managed(description = "Disable a member from a pool")
    public String[] disablePoolMember(String memberAddress, int memberPort) throws Exception
    {
        return disablePoolMember(memberAddress, memberPort, poolName, hostname, username, password);
    }

    /**
     * Disable a server from a pool
     *
     * @param memberAddress server IP to remove
     * @param memberPort    server port
     * @param poolName      pool name
     * @param hostname      SLB VIP hostname
     * @param username      SLB VIP username
     * @param password      SLB VIP password
     * @return the new status of the server
     * @throws Exception generic exception if something goes wrong
     */
    @Managed(description = "Disable a member from a pool")
    public String[] disablePoolMember(String memberAddress, int memberPort, String poolName, String hostname, String username, String password) throws Exception
    {
        final Interfaces m_interfaces = getInterface(hostname, username, password);
        final CommonIPPortDefinition definition = new CommonIPPortDefinition(memberAddress, (long) memberPort);

        log.info(String.format("Setting session state to DISABLED for %s in pool %s (hostname: %s, username: %s)", memberAddress, poolName, hostname, username));
        setSessionState(m_interfaces, definition, poolName, CommonEnabledState.STATE_DISABLED);

        log.info(String.format("Waiting for current connections to drop to zero for %s in pool %s (hostname: %s, username: %s)", memberAddress, poolName, hostname, username));
        CommonIPPortDefinition[] memberDefinitions = {definition};
        CommonIPPortDefinition[][] membersDefinitions = {memberDefinitions};

        long cur_connections = 1L;
        final String[] poolNames = {poolName};
        while (cur_connections > 0L) {
            LocalLBPoolMemberMemberStatistics[] memberStatistics;
            try {
                memberStatistics = m_interfaces.getLocalLBPoolMember().get_statistics(poolNames, membersDefinitions);
            }
            catch (AxisFault e) {
                log.warn(String.format("Unable to get statistics: %s", e.getFaultString()));
                throw e;
            }
            catch (Exception e) {
                log.warn(String.format("Unable to get statistics: %s", e.getLocalizedMessage()));
                throw e;
            }

            LocalLBPoolMemberMemberStatistics memberStats = memberStatistics[0];

            LocalLBPoolMemberMemberStatisticEntry[] statisticEntries = memberStats.getStatistics();
            LocalLBPoolMemberMemberStatisticEntry statisticEntry = statisticEntries[0];

            CommonStatistic[] statistics = statisticEntry.getStatistics();

            for (CommonStatistic stats : statistics) {
                CommonStatisticType type = stats.getType();
                CommonULong64 value64 = stats.getValue();

                if (type.equals(CommonStatisticType.STATISTIC_SERVER_SIDE_CURRENT_CONNECTIONS)) {
                    cur_connections = value64.getLow();
                    log.info(String.format("Current connections: %s (%s in pool %s, hostname: %s, username: %s)", cur_connections, memberAddress, poolName, hostname, username));
                }
            }
            Thread.currentThread();
            Thread.sleep(1000L);
        }

        log.info(String.format("Setting monitor state to DISABLED for %s in pool %s (hostname: %s, username: %s)", memberAddress, poolName, hostname, username));
        setMonitorState(m_interfaces, definition, poolName, CommonEnabledState.STATE_DISABLED);

        return getPoolMemberStatuses(poolName, memberAddress, hostname, username, password);
    }

    private static void setSessionState(Interfaces m_interfaces, CommonIPPortDefinition definition, String poolName, CommonEnabledState state) throws Exception
    {
        final String[] poolNames = {poolName};

        LocalLBPoolMemberMemberSessionState memberSessionState = new LocalLBPoolMemberMemberSessionState(definition, state);

        LocalLBPoolMemberMemberSessionState[] memberSessionStates = {memberSessionState};
        LocalLBPoolMemberMemberSessionState[][] membersSessionStates = {memberSessionStates};

        log.info(String.format("Updating SLB VIP session state: %s", toString(memberSessionState)));
        try {
            m_interfaces.getLocalLBPoolMember().set_session_enabled_state(poolNames, membersSessionStates);
            log.info("Update successful");
        }
        catch (AxisFault e) {
            log.warn(String.format("Unable to set session enabled state: %s", e.getFaultString()));
            throw e;
        }
        catch (Exception e) {
            log.warn(String.format("Unable to set session enabled state: %s", e.getLocalizedMessage()));
            throw e;
        }
    }

    private static void setMonitorState(Interfaces m_interfaces, CommonIPPortDefinition definition, String poolName, CommonEnabledState state) throws Exception
    {
        final String[] poolNames = {poolName};

        LocalLBPoolMemberMemberMonitorState memberMonitorState = new LocalLBPoolMemberMemberMonitorState(definition, state);

        LocalLBPoolMemberMemberMonitorState[] memberMonitorStates = {memberMonitorState};
        LocalLBPoolMemberMemberMonitorState[][] membersMonitorStates = {memberMonitorStates};

        log.info(String.format("Updating SLB VIP monitor state: %s", toString(memberMonitorState)));
        try {
            m_interfaces.getLocalLBPoolMember().set_monitor_state(poolNames, membersMonitorStates);
            log.info("Update successful");
        }
        catch (AxisFault e) {
            log.warn(String.format("Unable to set monitor state: %s", e.getFaultString()));
            throw e;
        }
        catch (Exception e) {
            log.warn(String.format("Unable to set monitor state: %s", e.getLocalizedMessage()));
            throw e;
        }
    }

    private String toString(CommonIPPortDefinition[][] membersDefinitions)
    {
        String res = "";
        for (CommonIPPortDefinition[] memberDefinitions : membersDefinitions) {
            for (CommonIPPortDefinition memberDefinition : memberDefinitions) {
                res = String.format("%s%s,", res, toString(memberDefinition));
            }
        }
        return StringUtils.chomp(res, ",");
    }

    private static String toString(LocalLBPoolMemberMemberMonitorState state)
    {
        return String.format("LocalLBPoolMemberMemberMonitorState{member=%s, monitor_state=%s}", toString(state.getMember()), toString(state.getMonitor_state()));
    }

    private static String toString(CommonEnabledState monitor_state)
    {
        return String.format("CommonEnabledState{value=%s}", monitor_state.getValue());
    }

    private static String toString(CommonIPPortDefinition member)
    {
        return String.format("CommonIPPortDefinition{address=%s, port=%d}", member.getAddress(), member.getPort());
    }

    private static String toString(LocalLBPoolMemberMemberSessionState memberSessionState)
    {
        return String.format("LocalLBPoolMemberMemberSessionState{member=%s, session_state=%s}", toString(memberSessionState.getMember()), toString(memberSessionState.getSession_state()));
    }

    private String toString(LocalLBObjectStatus object_status)
    {
        return String.format("LocalLBObjectStatus{availability=%s, enabled=%s, description=%s}",
            object_status.getAvailability_status(), object_status.getEnabled_status(), object_status.getStatus_description());
    }
}

