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
package org.thingsboard.server.controller;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.thingsboard.rule.engine.api.msg.DeviceCredentialsUpdateNotificationMsg;
import org.thingsboard.rule.engine.api.msg.DeviceNameOrTypeUpdateMsg;
import org.thingsboard.server.common.data.*;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.device.DeviceSearchQuery;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.*;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleEvent;
import org.thingsboard.server.common.data.security.DeviceCredentials;
import org.thingsboard.server.common.msg.TbMsg;
import org.thingsboard.server.common.msg.TbMsgDataType;
import org.thingsboard.server.common.msg.TbMsgMetaData;
import org.thingsboard.server.dao.device.claim.ClaimResponse;
import org.thingsboard.server.dao.device.claim.ClaimResult;
import org.thingsboard.server.dao.exception.IncorrectParameterException;
import org.thingsboard.server.dao.model.ModelConstants;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.model.SecurityUser;
import org.thingsboard.server.service.security.permission.Operation;
import org.thingsboard.server.service.security.permission.Resource;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@TbCoreComponent
@RequestMapping("/api")
public class TestingController  extends BaseController {

    private static final String TESTING_ID = "testingId";
    private static final String TESTING_NAME = "testingName";
    private static final String TENANT_ID = "tenantId";


    @RequestMapping(value = "/testing", method = RequestMethod.POST)
    public Testing testing(@RequestBody Testing testing) {
        System.out.println("--------: " + testing);
        try {
            testing.setTenantId(getCurrentUser().getTenantId());
            checkEntity(testing.getId(), testing, Resource.TESTING);
            return testingService.saveTesting(testing);
        } catch (ThingsboardException e) {
            e.printStackTrace();
        }
        System.out.println("e po po qenka kshu kjo");
        return testing;
    }
    @RequestMapping(value = "/testing/update", method = RequestMethod.POST)
    public Testing updatetesting(@RequestBody Testing testing) {
        System.out.println("--------:UPDATE " + testing);
        try {
            testing.setTenantId(getCurrentUser().getTenantId());
            checkEntity(testing.getId(), testing, Resource.DEVICE);
            return testingService.saveTesting(testing);
        } catch (ThingsboardException e) {
            e.printStackTrace();
        }
        System.out.println("e po po qenka kshu kjo");
        return testing;
    }



    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/testings", params = {"pageSize", "page"}, method = RequestMethod.GET)
    @ResponseBody
    public PageData<Testing> getTenantTestings(
            @RequestParam int pageSize,
            @RequestParam int page,
            @RequestParam(required = false) String sensorType,
            @RequestParam(required = false) String sortProperty,
            @RequestParam(required = false) String sortOrder) throws ThingsboardException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            PageLink pageLink = createPageLink(pageSize, page, sensorType, sortProperty, sortOrder);
            if (sensorType != null && sensorType.trim().length() > 0) {
                return checkNotNull(testingService.findTestingsByTenantIdAndType(tenantId, sensorType, pageLink));
            } else {
                return checkNotNull(testingService.findTestingsByTenantId(tenantId, pageLink));
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }



    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/testing/info/{testingId}", method = RequestMethod.GET)
    @ResponseBody
    public TestingInfo getTestingInfoById(@PathVariable(TESTING_ID) String strDeviceId) throws ThingsboardException {
        checkParameter(TESTING_ID, strDeviceId);
        try {
            TestingId deviceId = new TestingId(toUUID(strDeviceId));
            System.out.println("---------: ktu jemi te testing controller: "+ toUUID(strDeviceId));
            return checkTestingInfoId(deviceId, Operation.READ);

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    //@PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/testing/{testingId}", method = RequestMethod.GET)
    @ResponseBody
    public TestingInfo getTestingById(@PathVariable(TESTING_ID) String strDeviceId) throws ThingsboardException {
        checkParameter(TESTING_ID, strDeviceId);
        try {
            TestingId deviceId = new TestingId(toUUID(strDeviceId));
            Testing testingObject = checkTestingId(deviceId, Operation.READ);
            return new TestingInfo(testingObject,"",true);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/testing/{testingId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteTesting(@PathVariable(TESTING_ID) String strTestingId) throws ThingsboardException {
        checkParameter(TESTING_ID, strTestingId);
        try {
            TestingId testingId = new TestingId(toUUID(strTestingId));
            Testing testing = checkTestingId(testingId, Operation.DELETE);
            testingService.deleteTesting(getCurrentUser().getTenantId(), testingId);

           // tbClusterService.onDeviceDeleted(testing, null);
         //   tbClusterService.onEntityStateChange(device.getTenantId(), deviceId, ComponentLifecycleEvent.DELETED);

            logEntityAction(testingId, testing,
                    testing.getCustomerId(),
                    ActionType.DELETED, null, strTestingId);

           // deviceStateService.onDeviceDeleted(device);
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.TESTING),
                    null,
                    null,
                    ActionType.DELETED, e, strTestingId);
            throw handleException(e);
        }
    }
























}
