package org.safehaus.kiskis.mgmt.hadoop.services;

import org.safehaus.kiskis.mgmt.api.hadoop.Hadoop;

/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private Hadoop hadoopManager;

    public Hadoop getHadoopManager() {
        return hadoopManager;
    }

    public void setHadoopManager(Hadoop hadoopManager) {
        this.hadoopManager = hadoopManager;
    }

    @Override
    public String installCluster(String clusterName) {
        return null;
    }

    @Override
    public String uninstallCluster(String clusterName) {
        return null;
    }
}
