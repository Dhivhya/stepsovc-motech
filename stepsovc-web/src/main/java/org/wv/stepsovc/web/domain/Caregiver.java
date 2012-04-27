package org.wv.stepsovc.web.domain;

import org.codehaus.jackson.annotate.JsonProperty;
import org.ektorp.support.TypeDiscriminator;
import org.motechproject.model.MotechBaseDataObject;

@TypeDiscriminator("doc.type = 'Caregiver'")
public class Caregiver extends MotechBaseDataObject {
    @JsonProperty
    private String name;
    @JsonProperty
    private String id;
    @JsonProperty("phone_number")
    private String phoneNumber;
}
