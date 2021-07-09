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

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.thingsboard.server.common.data.*;

import org.thingsboard.server.common.data.id.*;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.EntitySearchDirection;

import org.thingsboard.server.common.data.security.DeviceCredentials;
import org.thingsboard.server.common.data.tenant.profile.DefaultTenantProfileConfiguration;
import org.thingsboard.server.dao.customer.CustomerDao;
import org.thingsboard.server.dao.entity.AbstractEntityService;
import org.thingsboard.server.dao.entityview.EntityViewService;
import org.thingsboard.server.dao.event.EventService;
import org.thingsboard.server.dao.exception.DataValidationException;
import org.thingsboard.server.dao.service.DataValidator;
import org.thingsboard.server.dao.service.PaginatedRemover;
import org.thingsboard.server.dao.service.Validator;
import org.thingsboard.server.dao.tenant.TbTenantProfileCache;
import org.thingsboard.server.dao.tenant.TenantDao;
import org.thingsboard.server.dao.util.mapping.JacksonUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.thingsboard.server.common.data.CacheConstants.DEVICE_CACHE;
import static org.thingsboard.server.dao.DaoUtil.toUUIDs;
import static org.thingsboard.server.dao.model.ModelConstants.NULL_UUID;
import static org.thingsboard.server.dao.service.Validator.validateId;
import static org.thingsboard.server.dao.service.Validator.validateIds;
import static org.thingsboard.server.dao.service.Validator.validatePageLink;
import static org.thingsboard.server.dao.service.Validator.validateString;

@Service
@Slf4j
public class TestingServiceImpl extends AbstractEntityService implements TestingService {

    public static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";
    public static final String INCORRECT_TESTING_PROFILE_ID = "Incorrect testingProfileId ";
    public static final String INCORRECT_PAGE_LINK = "Incorrect page link ";
    public static final String INCORRECT_CUSTOMER_ID = "Incorrect customerId ";
    public static final String INCORRECT_TESTING_ID = "Incorrect testingId ";

    @Autowired
    private TestingDao testingDao;

    @Autowired
    private TenantDao tenantDao;

    @Autowired
    private CustomerDao customerDao;


    @Autowired
    private EntityViewService entityViewService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private EventService eventService;

    @Autowired
    @Lazy
    private TbTenantProfileCache tenantProfileCache;




    @CacheEvict(cacheNames = DEVICE_CACHE, key = "{#testing.tenantId, #testing.name}")
    @Override
    public Testing saveTesting(Testing testing) {
        return doSaveTesting(testing, null);
    }

    private Testing doSaveTesting(Testing testing, String accessToken) {
        log.trace("Executing saveTesting [{}]", testing);
        testingValidator.validate(testing, Testing::getTenantId);
        System.out.println("--------Do save testisng: " + testing);
        Testing savedTesting;
        try {
            System.out.println("--------  instide try Do save testisng: " + testing);
            System.out.println("--------  instide try Do save tenantId: " + testing.getTenantId());


            savedTesting = testingDao.save(testing.getTenantId(), testing);

            System.out.println("----::: ::: ::: "+ savedTesting);
        } catch (Exception t) {
            ConstraintViolationException e = extractConstraintViolationException(t).orElse(null);
            if (e != null && e.getConstraintName() != null && e.getConstraintName().equalsIgnoreCase("testing_name_unq_key")) {
                // remove testing from cache in case null value cached in the distributed redis.
                removeTestingFromCache(testing.getTenantId(), testing.getName());
                throw new DataValidationException("Testing with such name already exists!");
            } else {
                throw t;
            }
        }

        return savedTesting;
    }

    private void removeTestingFromCache(TenantId tenantId, String name) {
        List<Object> list = new ArrayList<>();
        list.add(tenantId);
        list.add(name);
        Cache cache = cacheManager.getCache(DEVICE_CACHE);
        cache.evict(list);
    }


    private DataValidator<Testing> testingValidator =
            new DataValidator<Testing>() {

                @Override
                protected void validateCreate(TenantId tenantId, Testing testing) {
                    DefaultTenantProfileConfiguration profileConfiguration =
                            (DefaultTenantProfileConfiguration)tenantProfileCache.get(tenantId).getProfileData().getConfiguration();
                    //long maxTestings = profileConfiguration.getMaxTestings();
                    //validateNumberOfEntitiesPerTenant(tenantId, testingDao, maxTestings, EntityType.DEVICE);
                }

    };




    @Override
    public PageData<Testing>findTestingsByTenantId(TenantId tenantId, PageLink pageLink) {
        log.trace("Executing findDevicesByTenantId, tenantId [{}], pageLink [{}]", tenantId, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validatePageLink(pageLink);
        return testingDao.findTestingsByTenantId(tenantId.getId(), pageLink);
    }



    @Override
    public PageData<Testing> findTestingsByTenantIdAndType(TenantId tenantId, String sensorType, PageLink pageLink) {
        log.trace("Executing findTestingssByTenantIdAndSensorType, tenantId [{}], sensorType [{}], pageLink [{}]", tenantId, sensorType, pageLink);
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateString(sensorType, "Incorrect type " + sensorType);
        validatePageLink(pageLink);
        return testingDao.findTestingsByTenantIdAndType(tenantId.getId(), sensorType, pageLink);
    }




    @Override
    public Testing findTestingById(TenantId tenantId, TestingId testingId) {
        log.trace("Executing findDeviceById [{}]", testingId);
        validateId(testingId, INCORRECT_TESTING_ID + testingId);
        if (TenantId.SYS_TENANT_ID.equals(tenantId)) {
            return testingDao.findById(tenantId, testingId.getId());
        } else {
            return testingDao.findTestingByTenantIdAndId(tenantId, testingId.getId());
        }
    }

    @Override
    public TestingInfo findTestingInfoById(TenantId tenantId, TestingId deviceId) {
        log.trace("Executing findDeviceInfoById [{}]", deviceId);
        validateId(deviceId, INCORRECT_TESTING_ID + deviceId);
        return testingDao.findTestingInfoById(tenantId, deviceId.getId());
    }


    @Override
    public void deleteTesting(TenantId tenantId, TestingId testingId) {
        log.trace("Executing deleteDevice [{}]", testingId);
        validateId(testingId, INCORRECT_TESTING_ID + testingId);

        Testing testing = testingDao.findById(tenantId, testingId.getId());
        try {
            List<EntityView> entityViews = entityViewService.findEntityViewsByTenantIdAndEntityIdAsync(testing.getTenantId(), testingId).get();
            if (entityViews != null && !entityViews.isEmpty()) {
                throw new DataValidationException("Can't delete device that has entity views!");
            }
        } catch (ExecutionException | InterruptedException e) {
            log.error("Exception while finding entity views for deviceId [{}]", testingId, e);
            throw new RuntimeException("Exception while finding entity views for deviceId [" + testingId + "]", e);
        }

     //   DeviceCredentials deviceCredentials = deviceCredentialsService.findDeviceCredentialsByDeviceId(tenantId, deviceId);
     //   if (deviceCredentials != null) {
       //     deviceCredentialsService.deleteDeviceCredentials(tenantId, deviceCredentials);
       // }
        deleteEntityRelations(tenantId, testingId);

        removeTestingFromCache(tenantId, testing.getName());

        testingDao.removeById(tenantId, testingId.getId());
    }

//    @Override
//    public void deleteDashboard(TenantId tenantId, DashboardId dashboardId) {
//        log.trace("Executing deleteDashboard [{}]", dashboardId);
//        Validator.validateId(dashboardId, INCORRECT_DASHBOARD_ID + dashboardId);
//        deleteEntityRelations(tenantId, dashboardId);
//        dashboardDao.removeById(tenantId, dashboardId.getId());
//    }















}
