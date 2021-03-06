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

package org.dcm4chee.wizard.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.model.IModel;
import org.dcm4che3.net.Dimse;
import org.dcm4chee.proxy.conf.ForwardRule;

/**
 * @author Robert David <robert.david@agfa.com>
 */
public class DimseCollectionModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private Dimse[] dimses;

    public DimseCollectionModel(ForwardRule forwardRule, int size) {
        dimses = forwardRule == null ? new Dimse[size] : forwardRule.getDimse().toArray(new Dimse[size]);
    }

    public DimseModel getDimseModel(int index) {
        if (index < dimses.length)
            return new DimseModel(this, index);
        else
            throw new IllegalArgumentException("Wrong index, must be less than " + dimses.length);
    }

    public class DimseModel implements IModel<Dimse> {

        private static final long serialVersionUID = 1L;

        DimseCollectionModel model;
        int index;

        public DimseModel(DimseCollectionModel model, int index) {
            this.model = model;
            this.index = index;
        }

        public Dimse getObject() {
            return index < dimses.length ? dimses[index] : null;
        }

        public void setObject(Dimse dimse) {
            model.dimses[index] = dimse;
        }

        public void detach() {
        }
    }

    public Set<Dimse> getDimses() {
        Set<Dimse> result = new HashSet<Dimse>();
        for (Dimse dimse : dimses)
            if (dimse != null)
                result.add(dimse);
        return result;
    }
}
