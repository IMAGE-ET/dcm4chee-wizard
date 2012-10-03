/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chee.wizard.war.configuration.simple.edit;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.validation.validator.RangeValidator;
import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.net.Connection;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.common.markup.modal.MessageWindow;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.ConnectionModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.DeviceModel;
import org.dcm4chee.wizard.war.configuration.simple.model.basic.StringArrayModel;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeNode;
import org.dcm4chee.wizard.war.configuration.simple.tree.ConfigTreeProvider;
import org.dcm4chee.wizard.war.configuration.simple.validator.ConnectionValidator;
import org.dcm4chee.wizard.war.configuration.simple.validator.HostnameValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.security.components.SecureWebPage;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class CreateOrEditConnectionPage extends SecureWebPage {
    
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CreateOrEditConnectionPage.class);
    
    private static final ResourceReference BaseCSS = new CssResourceReference(BaseWicketPage.class, "base-style.css");
    
    private MessageWindow msgWin = new MessageWindow("msgWin");

    // mandatory
	private Model<String> hostnameModel;

	// optional
    private Model<String> commonNameModel;
	private IModel<Boolean> installedModel;
	private Model<Integer> portModel;
	private StringArrayModel tlsCipherSuitesModel;
	
	private Model<String> httpProxyModel;
	// [user:password@]host:port
	private IModel<Boolean> tlsNeedClientAuthModel;
	private StringArrayModel tlsProtocolModel;
	// "TLSv1", "SSLv3" if absent
	private Model<Integer> tcpBacklogModel;
	// Maximum queue length for incoming TCP connections; 50 if absent
	private Model<Integer> tcpConnectTimeoutModel;
	// TCP connect timeout in ms; no timeout if absent
	private Model<Integer> tcpCloseDelayModel;
	// TCP socket close delay in ms; 50 ms if absent
	private Model<Integer> tcpSendBufferSizeModel;
	private Model<Integer> tcpReceiveBufferSizeModel;
	private IModel<Boolean> tcpNoDelayModel;
	// Enable/disable TCP_NODELAY (disable/enable Nagle algorithm); disable Nagle algorithm if absent
	private StringArrayModel blacklistedHostnameModel;
	private Model<Integer> sendPDULengthModel;
	// Maximal length of emitted PDUs; 16378 if absent
	private Model<Integer> receivePDULengthModel;
	// Maximal length of received PDUs; 16378 if absent
	private Model<Integer> maxOpsPerformedModel;
	// 1 (=synchronous) if absent
	private Model<Integer> maxOpsInvokedModel;
	// 1 (=synchronous) if absent
	private IModel<Boolean> packPDVModel;
	// enabled if absent
	private IModel<Integer> aarqTimeoutModel;
	// Timeout in ms; no timeout if absent
	private IModel<Integer> aaacTimeoutModel;
	// Timeout in ms; no timeout if absent
	private IModel<Integer> arrpTimeoutModel;
	// Timeout in ms; no timeout if absent
	private IModel<Integer> responseTimeoutModel;
	// Timeout in ms; no timeout if absent
	private IModel<Integer> retrieveTimeoutModel;
	// Timeout in ms; no timeout if absent
	private IModel<Integer> idleTimeoutModel;
	// Timeout in ms; no timeout if absent
    
    public CreateOrEditConnectionPage(final ModalWindow window, final ConnectionModel connectionModel, 
			final ConfigTreeNode deviceNode) {
        super();
        
        msgWin.setTitle("");
        add(msgWin);
        add(new WebMarkupContainer("create-connection-title").setVisible(connectionModel == null));
        add(new WebMarkupContainer("edit-connection-title").setVisible(connectionModel != null));

        setOutputMarkupId(true);
        final BaseForm form = new BaseForm("form") {
        	
			private static final long serialVersionUID = 1L;

			public void onValidate() {
				
        	};
        };
        form.setResourceIdPrefix("dicom.edit.connection.");
        add(form);
        
        try {
        	if (connectionModel == null) {
		        hostnameModel = Model.of();
	        	commonNameModel = Model.of();
				installedModel = Model.of();
		        portModel = Model.of(1);
		        tlsCipherSuitesModel = new StringArrayModel(null);
		    	httpProxyModel = Model.of();
		    	tlsNeedClientAuthModel = Model.of(true);
		    	tlsProtocolModel = new StringArrayModel(new String[] { "SSLv3" });
		    	tcpBacklogModel = Model.of(50);
		    	tcpConnectTimeoutModel = Model.of();
		    	tcpCloseDelayModel = Model.of(50);
		    	tcpSendBufferSizeModel = Model.of();
		    	tcpReceiveBufferSizeModel = Model.of();
		    	tcpNoDelayModel = Model.of(false);
		    	blacklistedHostnameModel = new StringArrayModel(null);
		    	sendPDULengthModel = Model.of(16378);
		    	receivePDULengthModel = Model.of(16378);
		    	maxOpsPerformedModel = Model.of(1);
		    	maxOpsInvokedModel = Model.of(1);
		    	packPDVModel = Model.of(true);
		    	aarqTimeoutModel = Model.of();
		    	aaacTimeoutModel = Model.of();
		    	arrpTimeoutModel = Model.of();	    	
		    	responseTimeoutModel = Model.of();
		    	retrieveTimeoutModel = Model.of();
		    	idleTimeoutModel = Model.of();
        	} else {
        		Connection connection = connectionModel.getConnection();
		        hostnameModel = Model.of(connection.getHostname());
	        	commonNameModel = Model.of(connection.getCommonName());
				installedModel = Model.of(connection.isInstalled());
		        portModel = Model.of(connection.getPort());
		        tlsCipherSuitesModel = new StringArrayModel(connection.getTlsCipherSuites());
		    	httpProxyModel = Model.of(connection.getHttpProxy());
		    	tlsNeedClientAuthModel = Model.of(connection.isTlsNeedClientAuth());
		    	tlsProtocolModel = new StringArrayModel(connection.getTlsProtocols());
		    	tcpBacklogModel = Model.of(connection.getBacklog());
		    	tcpConnectTimeoutModel = Model.of(connection.getConnectTimeout());
		    	tcpCloseDelayModel = Model.of(connection.getSocketCloseDelay());
		    	tcpSendBufferSizeModel = Model.of(connection.getSendBufferSize());
		    	tcpReceiveBufferSizeModel = Model.of(connection.getReceiveBufferSize());
		    	tcpNoDelayModel = Model.of(connection.isTcpNoDelay());
		    	blacklistedHostnameModel = new StringArrayModel(connection.getBlacklist());
		    	sendPDULengthModel = Model.of(connection.getSendPDULength());
		    	receivePDULengthModel = Model.of(connection.getReceivePDULength());
		    	maxOpsPerformedModel = Model.of(connection.getMaxOpsPerformed());
		    	maxOpsInvokedModel = Model.of(connection.getMaxOpsInvoked());
		    	packPDVModel = Model.of(connection.isPackPDV());
		    	aarqTimeoutModel = Model.of(connection.getRequestTimeout());
		    	aaacTimeoutModel = Model.of(connection.getAcceptTimeout());
		    	arrpTimeoutModel = Model.of(connection.getReleaseTimeout());
		    	responseTimeoutModel = Model.of(connection.getResponseTimeout());
		    	retrieveTimeoutModel = Model.of(connection.getRetrieveTimeout());
		    	idleTimeoutModel = Model.of(connection.getIdleTimeout());
	        }
        } catch (ConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        FormComponent<String> hostnameTextField;
		form.add(new Label("hostname.label", new ResourceModel("dicom.edit.connection.hostname.label")))
        .add(hostnameTextField = new TextField<String>("hostname", hostnameModel)
        		.add(new HostnameValidator())
        		.setRequired(true));

        final WebMarkupContainer optionalContainer = new WebMarkupContainer("optionalContainer");
        form.add(optionalContainer
        		.setOutputMarkupId(true)
        		.setOutputMarkupPlaceholderTag(true)
        		.setVisible(false));
        
        TextField<String> commonNameTextField;
		optionalContainer.add(new Label("commonName.label", new ResourceModel("dicom.edit.connection.commonName.label")))
        .add(commonNameTextField = new TextField<String>("commonName", commonNameModel));

        optionalContainer.add(new Label("installed.label", new ResourceModel("dicom.edit.connection.installed.label")))
        .add(new CheckBox("installed", installedModel));

        FormComponent<Integer> portTextField;
		optionalContainer.add(new Label("port.label", new ResourceModel("dicom.edit.connection.port.label")))
        .add(portTextField = new TextField<Integer>("port", portModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(1,65535)));

        optionalContainer.add(new Label("tlsCipherSuites.label", new ResourceModel("dicom.edit.connection.tlsCipherSuites.label")))
        .add(new TextArea<String>("tlsCipherSuites", tlsCipherSuitesModel));

        optionalContainer.add(new Label("httpProxy.label", new ResourceModel("dicom.edit.connection.httpProxy.label")))
        .add(new TextField<String>("httpProxy", httpProxyModel)
        		.add(new HostnameValidator()));
        
        optionalContainer.add(new Label("tlsNeedClientAuth.label", new ResourceModel("dicom.edit.connection.tlsNeedClientAuth.label")))
        .add(new CheckBox("tlsNeedClientAuth", tlsNeedClientAuthModel));

        optionalContainer.add(new Label("tlsProtocol.label", new ResourceModel("dicom.edit.connection.tlsProtocol.label")))
        .add(new TextArea<String>("tlsProtocol", tlsProtocolModel));

        optionalContainer.add(new Label("tcpBacklog.label", new ResourceModel("dicom.edit.connection.tcpBacklog.label")))
        .add(new TextField<Integer>("tcpBacklog", tcpBacklogModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,65535)));

        optionalContainer.add(new Label("tcpConnectTimeout.label", new ResourceModel("dicom.edit.connection.tcpConnectTimeout.label")))
        .add(new TextField<Integer>("tcpConnectTimeout", tcpConnectTimeoutModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,65535)));

        optionalContainer.add(new Label("tcpCloseDelay.label", new ResourceModel("dicom.edit.connection.tcpCloseDelay.label")))
        .add(new TextField<Integer>("tcpCloseDelay", tcpCloseDelayModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,65535)));

        optionalContainer.add(new Label("tcpSendBufferSize.label", new ResourceModel("dicom.edit.connection.tcpSendBufferSize.label")))
        .add(new TextField<Integer>("tcpSendBufferSize", tcpSendBufferSizeModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,65535)));
        
        optionalContainer.add(new Label("tcpReceiveBufferSize.label", new ResourceModel("dicom.edit.connection.tcpReceiveBufferSize.label")))
        .add(new TextField<Integer>("tcpReceiveBufferSize", tcpReceiveBufferSizeModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,65535)));
        
        optionalContainer.add(new Label("tcpNoDelay.label", new ResourceModel("dicom.edit.connection.tcpNoDelay.label")))
        .add(new CheckBox("tcpNoDelay", tcpNoDelayModel));

        optionalContainer.add(new Label("blacklistedHostname.label", new ResourceModel("dicom.edit.connection.blacklistedHostname.label")))
        .add(new TextArea<String>("blacklistedHostname", blacklistedHostnameModel));

        optionalContainer.add(new Label("sendPDULength.label", new ResourceModel("dicom.edit.connection.sendPDULength.label")))
        .add(new TextField<Integer>("sendPDULength", sendPDULengthModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,65535)));
        
        optionalContainer.add(new Label("receivePDULength.label", new ResourceModel("dicom.edit.connection.receivePDULength.label")))
        .add(new TextField<Integer>("receivePDULength", receivePDULengthModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,65535)));

        optionalContainer.add(new Label("maxOpsPerformed.label", new ResourceModel("dicom.edit.connection.maxOpsPerformed.label")))
        .add(new TextField<Integer>("maxOpsPerformed", maxOpsPerformedModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,65535)));

        optionalContainer.add(new Label("maxOpsInvoked.label", new ResourceModel("dicom.edit.connection.maxOpsInvoked.label")))
        .add(new TextField<Integer>("maxOpsInvoked", maxOpsInvokedModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,65535)));

        optionalContainer.add(new Label("packPDV.label", new ResourceModel("dicom.edit.connection.packPDV.label")))
        .add(new CheckBox("packPDV", packPDVModel));

        optionalContainer.add(new Label("aarqTimeout.label", new ResourceModel("dicom.edit.connection.aarqTimeout.label")))
        .add(new TextField<Integer>("aarqTimeout", aarqTimeoutModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,65535)));

        optionalContainer.add(new Label("aaacTimeout.label", new ResourceModel("dicom.edit.connection.aaacTimeout.label")))
        .add(new TextField<Integer>("aaacTimeout", aaacTimeoutModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,65535)));

        optionalContainer.add(new Label("arrpTimeout.label", new ResourceModel("dicom.edit.connection.arrpTimeout.label")))
        .add(new TextField<Integer>("arrpTimeout", arrpTimeoutModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,65535)));

        optionalContainer.add(new Label("responseTimeout.label", new ResourceModel("dicom.edit.connection.responseTimeout.label")))
        .add(new TextField<Integer>("responseTimeout", responseTimeoutModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,65535)));

        optionalContainer.add(new Label("retrieveTimeout.label", new ResourceModel("dicom.edit.connection.retrieveTimeout.label")))
        .add(new TextField<Integer>("retrieveTimeout", retrieveTimeoutModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,65535)));
        
        optionalContainer.add(new Label("idleTimeout.label", new ResourceModel("dicom.edit.connection.idleTimeout.label")))
        .add(new TextField<Integer>("idleTimeout", idleTimeoutModel)
        		.setType(Integer.class)
        		.add(new RangeValidator<Integer>(0,65535)));

        form.add(new Label("optional.label", new ResourceModel("dicom.edit.optional.label")))
        .add(new AjaxCheckBox("optional", new Model<Boolean>()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(optionalContainer.setVisible(this.getModelObject()));
			}
        });
        
		form.add(new ConnectionValidator(((DeviceModel) deviceNode.getModel()).getConnections(), 
				commonNameTextField, hostnameTextField, portTextField, commonNameTextField.getModelObject()));
        
        form.add(new AjaxFallbackButton("submit", new ResourceModel("saveBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    Connection connection = connectionModel == null ? 
                    		new Connection(commonNameModel.getObject(), hostnameModel.getObject()) : 
                    			connectionModel.getConnection();

                    connection.setHostname(hostnameModel.getObject());
                    connection.setCommonName(commonNameModel.getObject());
                    connection.setInstalled(installedModel.getObject());
                    connection.setPort(portModel.getObject().intValue());
                    connection.setTlsCipherSuites(tlsCipherSuitesModel.getArray());
                    connection.setHttpProxy(httpProxyModel.getObject());
                    connection.setTlsNeedClientAuth(tlsNeedClientAuthModel.getObject());
                    connection.setTlsProtocols(tlsProtocolModel.getArray());
                    connection.setBacklog(tcpBacklogModel.getObject());
                    connection.setConnectTimeout(tcpConnectTimeoutModel.getObject());
                    connection.setSocketCloseDelay(tcpCloseDelayModel.getObject());
                    connection.setSendBufferSize(tcpSendBufferSizeModel.getObject());
                    connection.setReceiveBufferSize(tcpReceiveBufferSizeModel.getObject());
                    connection.setTcpNoDelay(tcpNoDelayModel.getObject());
                    connection.setBlacklist(blacklistedHostnameModel.getArray());
                    connection.setSendPDULength(sendPDULengthModel.getObject());
                    connection.setReceivePDULength(receivePDULengthModel.getObject());
                    connection.setMaxOpsPerformed(maxOpsPerformedModel.getObject());                    
                    connection.setMaxOpsInvoked(maxOpsInvokedModel.getObject());
                    connection.setPackPDV(packPDVModel.getObject());
                    connection.setRequestTimeout(aarqTimeoutModel.getObject());
                    connection.setAcceptTimeout(aaacTimeoutModel.getObject());
                    connection.setReleaseTimeout(arrpTimeoutModel.getObject());
                    connection.setResponseTimeout(responseTimeoutModel.getObject());
                    connection.setRetrieveTimeout(retrieveTimeoutModel.getObject());
                    connection.setIdleTimeout(idleTimeoutModel.getObject());

                    if (connectionModel == null) 
                        ((DeviceModel) deviceNode.getModel()).getDevice().addConnection(connection);
                    ConfigTreeProvider.get().mergeDevice(connection.getDevice());
                    deviceNode.setModel(null);
                    window.close(target);
                } catch (Exception e) {
                	log.error("Error modifying connection", e);
                    msgWin.show(target, new ResourceModel(connectionModel == null ? 
                    		"dicom.edit.connection.create.failed" : "dicom.edit.connection.update.failed")
                    		.wrapOnAssignment(this));
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                if (target != null)
                    target.add(form);
            }
        });
        form.add(new AjaxFallbackButton("cancel", new ResourceModel("cancelBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                window.close(target);
            }

			@Override
			protected void onError(AjaxRequestTarget arg0, Form<?> arg1) {
			}
        }.setDefaultFormProcessing(false));
    }
    
    @Override
    public void renderHead(IHeaderResponse response) {
        if (CreateOrEditConnectionPage.BaseCSS != null)
        	response.renderCSSReference(CreateOrEditConnectionPage.BaseCSS);
    }    
}