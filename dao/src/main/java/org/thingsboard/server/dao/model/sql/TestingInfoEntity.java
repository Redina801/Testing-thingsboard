package org.thingsboard.server.dao.model.sql;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.server.common.data.DeviceInfo;
import org.thingsboard.server.common.data.TestingInfo;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class TestingInfoEntity extends AbstractTestingEntity<TestingInfo>{
    public static final Map<String,String> testingInfoColumnMap = new HashMap<>();
    static {
        testingInfoColumnMap.put("customerTitle", "c.title");
    }

    private String customerTitle;
    private boolean customerIsPublic;

    public TestingInfoEntity() {
        super();
    }

    public TestingInfoEntity(TestingEntity testingEntity,
                            String customerTitle,
                            Object customerAdditionalInfo
                            ) {
        super(testingEntity);
        this.customerTitle = customerTitle;
        if (customerAdditionalInfo != null && ((JsonNode)customerAdditionalInfo).has("isPublic")) {
            this.customerIsPublic = ((JsonNode)customerAdditionalInfo).get("isPublic").asBoolean();
        } else {
            this.customerIsPublic = false;
        }
    }

    @Override
    public TestingInfo toData() {
        return new TestingInfo(super.toTesting(), customerTitle, customerIsPublic);
    }

    @Override
    public void setSearchText(String searchText) {

    }
}
