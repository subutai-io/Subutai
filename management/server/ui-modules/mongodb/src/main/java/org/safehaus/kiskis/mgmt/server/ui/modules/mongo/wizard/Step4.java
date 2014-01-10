/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.ConfigView;
import org.safehaus.kiskis.mgmt.shared.protocol.MongoClusterInfo;

/**
 *
 * @author dilshat
 */
public class Step4 extends Panel {

    private static final Logger LOG = Logger.getLogger(Step4.class.getName());

    public Step4(final Wizard wizard) {

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        content.setMargin(true);

        Label confirmationLbl = new Label("<strong>Please verify the installation configuration "
                + "(you may change it by clicking on Back button)</strong><br/>");
        confirmationLbl.setContentMode(Label.CONTENT_XHTML);

        ConfigView cfgView = new ConfigView("Installation configuration");
        cfgView.addStringCfg("Cluster Name", wizard.getConfig().getClusterName());
        cfgView.addStringCfg("Replica Set Name", wizard.getConfig().getReplicaSetName());
        cfgView.addAgentSetCfg("Configuration servers", wizard.getConfig().getConfigServers());
        cfgView.addAgentSetCfg("Routers", wizard.getConfig().getRouterServers());
        cfgView.addAgentSetCfg("Data Nodes", wizard.getConfig().getDataNodes());

        Button install = new Button("Install");
        install.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                MongoClusterInfo mongoClusterInfo
                        = new MongoClusterInfo(
                                wizard.getConfig().getClusterName(),
                                wizard.getConfig().getReplicaSetName(),
                                wizard.getConfig().getConfigServers(),
                                wizard.getConfig().getRouterServers(),
                                wizard.getConfig().getDataNodes());
                if (wizard.getDbManager().saveMongoClusterInfo(mongoClusterInfo)) {
                    wizard.next();
                } else {
                    show("Could not save new cluster configuration! Please see logs.");
                }
            }
        });

        Button back = new Button("Back");
        back.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.back();
            }
        });

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent(back);
        buttons.addComponent(install);

        content.addComponent(confirmationLbl);

        content.addComponent(cfgView.getCfgTable());

        content.addComponent(buttons);

        addComponent(content);

    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }
}
