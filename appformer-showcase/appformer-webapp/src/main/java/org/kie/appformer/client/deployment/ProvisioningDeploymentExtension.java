/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.appformer.client.deployment;

import java.util.Collection;
import java.util.Collections;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import org.guvnor.common.services.project.model.Project;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.jboss.errai.common.client.api.Caller;
import org.kie.appformer.provisioning.client.deployment.DeploymentPopup;
import org.kie.appformer.provisioning.service.GwtWarBuildService;
import org.kie.workbench.common.screens.datasource.management.client.editor.datasource.selector.DataSourceSelector;
import org.kie.workbench.common.screens.projecteditor.client.editor.extension.BuildOptionExtension;

@ApplicationScoped
public class ProvisioningDeploymentExtension implements BuildOptionExtension {

    private static final String LINK_NAME = "Provisioning Mode Deploy";

    @Inject
    private Caller<GwtWarBuildService> buildCaller;

    @Inject
    private DataSourceSelector dataSourceSelector;

    @Inject
    private DeploymentPopup deploymentPopup;

    @Override
    public Collection<Widget> getBuildOptions( Project project ) {
        return Collections.singleton( createNavLink( project ) );
    }

    private Widget createNavLink( final Project project ) {
        return new AnchorListItem( LINK_NAME ) {{
            addClickHandler( createClickHandler( project ) );
        }};
    }

    private ClickHandler createClickHandler( final Project project ) {
        return new ClickHandler( ) {
            @Override
            public void onClick( ClickEvent event ) {
                deploymentPopup.show( project );
            }
        };
    }
}