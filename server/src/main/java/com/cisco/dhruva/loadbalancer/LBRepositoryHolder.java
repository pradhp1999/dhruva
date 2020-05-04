package com.cisco.dhruva.loadbalancer;

import java.util.HashMap;

/**
 * An interface that can be used by any class which wants to store a server group
 * repository.
 */

public interface LBRepositoryHolder {

    /**
     * Retrieve a hashmap containing the current server group repository.
     */
    public HashMap getServerGroups();

}