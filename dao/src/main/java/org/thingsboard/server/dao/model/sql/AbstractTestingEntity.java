/**
 * Copyright © 2016-2021 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.dao.model.sql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.Testing;
import org.thingsboard.server.common.data.device.data.DeviceData;
import org.thingsboard.server.common.data.id.*;
import org.thingsboard.server.dao.model.BaseSqlEntity;
import org.thingsboard.server.dao.model.ModelConstants;
import org.thingsboard.server.dao.model.SearchTextEntity;
import org.thingsboard.server.dao.util.mapping.JacksonUtil;
import org.thingsboard.server.dao.util.mapping.JsonBinaryType;
import org.thingsboard.server.dao.util.mapping.JsonStringType;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonStringType.class),
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@MappedSuperclass
public abstract class AbstractTestingEntity<T extends Testing> extends BaseSqlEntity<T> implements SearchTextEntity<T> {

    @Column(name = "tenant_id", columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "costumer_id", columnDefinition = "uuid")
    private UUID customerId;


    @Column(name = "name")
    private String name;

    @Column(name = "sensor_type")
    private String sensorType;

    @Column(name = "model")
    private String model;

    @Column(name = "protocol")
    private String protocol;

    @Type(type = "json")
    @Column(name = "additional_info")
    private JsonNode additionalInfo;



    public AbstractTestingEntity() {
        super();

    }

    public AbstractTestingEntity(Testing testing) {
        if (testing.getId() != null) {
            this.setUuid(testing.getUuidId());
        }
        this.setCreatedTime(testing.getCreatedTime());
        if (testing.getTenantId() != null) {
            this.tenantId = testing.getTenantId().getId();
        }
        if (testing.getCustomerId() != null) {
            this.customerId = testing.getCustomerId().getId();
        }

        this.name = testing.getName();
        this.sensorType = testing.getSensorType();
        this.model =testing.getModel();
        this.protocol = testing.getProtocol();
        this.additionalInfo = testing.getAdditionalInfo();
    }

    public AbstractTestingEntity(TestingEntity deviceEntity) {
        this.setId(deviceEntity.getId());
        this.setCreatedTime(deviceEntity.getCreatedTime());
        this.tenantId = deviceEntity.getTenantId();
        this.customerId = deviceEntity.getCustomerId();
        this.name = deviceEntity.getName();
        this.sensorType = deviceEntity.getSensorType();
        this.model = deviceEntity.getModel();
        this.protocol = deviceEntity.getProtocol();

        this.additionalInfo = deviceEntity.getAdditionalInfo();
    }

    @Override
    public String getSearchTextSource() {
        return name;
    }



    protected Testing toTesting() {
        Testing testing = new Testing(new TestingId(getUuid()));
        testing.setCreatedTime(createdTime);
        if (tenantId != null) {
            testing.setTenantId(new TenantId(tenantId));
        }
        if (customerId != null) {
            testing.setCustomerId(new CustomerId(customerId));
        }
        testing.setName(name);
        testing.setSensorType(sensorType);
        testing.setModel(model);
        testing.setProtocol(protocol);
        testing.setAdditionalInfo(additionalInfo);
        return testing;
    }

}
