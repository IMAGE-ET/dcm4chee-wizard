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
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2012
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
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

package org.dcm4chee.wizard.page;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.DimseRSP;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4chee.wizard.common.component.ExtendedForm;
import org.dcm4chee.wizard.common.component.MainWebPage;
import org.dcm4chee.wizard.common.component.secure.SecureSessionCheckPage;
import org.dcm4chee.wizard.tree.ConfigTreeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class DicomEchoPage extends SecureSessionCheckPage {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(DicomEchoPage.class);

    private static final ResourceReference baseCSS = new CssResourceReference(MainWebPage.class, "base-style.css");

    private transient ApplicationEntity sourceAE;
    private transient ApplicationEntity destinationAE;

    private boolean error;
    private Model<String> resultModel;

    final String sourceAET = System.getProperty("org.dcm4chee.wizard.config.aeTitle");

    public DicomEchoPage(final ModalWindow window, ApplicationEntity destinationAE) {
        super();

        this.destinationAE = destinationAE;

        setOutputMarkupId(true);
        final ExtendedForm form = new ExtendedForm("form");
        form.setResourceIdPrefix("dicom.list.applicationEntity.echo.");
        add(form);

        form.add(new Label("sourceAET.label", new ResourceModel("dicom.list.applicationEntity.echo.sourceAET.label")));
        form.add(new Label("sourceAET", sourceAET));

        form.add(new Label("destinationAET.label", new ResourceModel(
                "dicom.list.applicationEntity.echo.destinationAET.label")));
        form.add(new Label("destinationAET", destinationAE.getAETitle()));

        form.add(new Label("result.label", new ResourceModel("dicom.list.applicationEntity.echo.result.label")));
        form.add(new TextArea<String>("result", (resultModel = new Model<String>())).setEscapeModelStrings(false).add(
                new AttributeModifier("class", new Model<String>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public String getObject() {
                        return error ? "error" : "text";
                    }
                })));

        form.add(new IndicatingAjaxButton("echo", new ResourceModel("dicom.list.applicationEntity.echo.echoBtn.text"),
                form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                doEcho();
                target.add(form);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                if (target != null)
                    target.add(form);
            }
        });
        form.add(new AjaxFallbackButton("cancel",
                new ResourceModel("dicom.list.applicationEntity.echo.cancelBtn.text"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                window.close(target);
            }

            @Override
            protected void onError(AjaxRequestTarget arg0, Form<?> arg1) {
            }
        }.setDefaultFormProcessing(false));

        doEcho();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        if (DicomEchoPage.baseCSS != null)
            response.render(CssHeaderItem.forReference(DicomEchoPage.baseCSS));
    }

    private void doEcho() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            resultModel.setObject("Fetching Source AE from configuration ...");
            sourceAE = ConfigTreeProvider.get().getApplicationEntity(sourceAET);
            resultModel.setObject(resultModel.getObject() + " OK\n");
            if (sourceAE == null)
                throw new Exception("Could not fetch source AE");
            else {
                resultModel.setObject(resultModel.getObject() + "Connecting to Destination AE ...");
                sourceAE.getDevice().setExecutor(executorService);
                AAssociateRQ request = new AAssociateRQ();
                request.addPresentationContext(new PresentationContext(1, UID.VerificationSOPClass,
                        new String[] { UID.ImplicitVRLittleEndian }));
                Association association = sourceAE.connect(DicomEchoPage.this.destinationAE, request);
                resultModel.setObject(resultModel.getObject() + " OK\nProcessing CECHO request ...");
                DimseRSP response = association.cecho();
                response.next();
                resultModel.setObject(resultModel.getObject() + " OK\nResult is: "
                        + response.getCommand().getInt(Tag.Status, -1) + "\nEcho completed successfully.\n");
                association.release();
            }
        } catch (Exception e) {
            log.error("Error processing echo request", e);
            error = true;
            resultModel.setObject(resultModel.getObject() + "\nERROR: \n" + e.getMessage() + "\n");
        } finally {
            executorService.shutdownNow();
        }
    }
}
