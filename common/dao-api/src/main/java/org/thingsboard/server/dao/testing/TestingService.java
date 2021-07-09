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
package org.thingsboard.server.dao.testing;

import com.google.common.util.concurrent.ListenableFuture;
import org.thingsboard.server.common.data.*;
import org.thingsboard.server.common.data.device.DeviceSearchQuery;
import org.thingsboard.server.common.data.id.*;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.dao.device.provision.ProvisionRequest;

import java.util.List;

public interface TestingService {

    TestingInfo findTestingInfoById(TenantId tenantId, TestingId deviceId);

    Testing saveTesting(Testing testing);

    PageData<Testing>findTestingsByTenantId(TenantId tenantId, PageLink pageLink);
    PageData<Testing> findTestingsByTenantIdAndType(TenantId tenantId, String sensorType, PageLink pageLink);
    Testing findTestingById(TenantId tenantId, TestingId testingId);
    void deleteTesting(TenantId tenantId, TestingId testingId);






}
