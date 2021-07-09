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
package org.thingsboard.server.dao.sql.testing;

import com.google.common.util.concurrent.ListenableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thingsboard.server.common.data.*;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.testing.TestingDao;
import org.thingsboard.server.dao.model.sql.TestingEntity;
import org.thingsboard.server.dao.sql.JpaAbstractSearchTextDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Valerii Sosliuk on 5/6/2017.
 */
@Component
public class JpaTestingDao extends JpaAbstractSearchTextDao<TestingEntity, Testing> implements TestingDao {

    @Autowired
    private TestingRepository testingRepository;

    @Override
    protected Class<TestingEntity> getEntityClass() {
        return TestingEntity.class;
    }

    @Override
    protected CrudRepository<TestingEntity, UUID> getCrudRepository() {
        return testingRepository;
    }



    @Override
    public Long countByTenantId(TenantId tenantId) {
        return testingRepository.countByTenantId(tenantId.getId());
    }


    @Override
    public TestingInfo findTestingInfoById(TenantId tenantId, UUID testingId) {
        System.out.println("----------: ktu jemi te jpaDaoTesting: "+ testingId);
        //TestingInfo ti = DaoUtil.getData(testingRepository.findTestingInfoById(testingId));
        return DaoUtil.getData(testingRepository.findTestingInfoById(testingId));
    }

    @Override
    public PageData<Testing>findTestingsByTenantId(UUID tenantId, PageLink pageLink) {
        if (StringUtils.isEmpty(pageLink.getTextSearch())) {
            return DaoUtil.toPageData(
                    testingRepository.findByTenantId(
                            tenantId,
                            DaoUtil.toPageable(pageLink)));
        } else {
            return DaoUtil.toPageData(
                    testingRepository.findByTenantId(
                            tenantId,
                            DaoUtil.toPageable(pageLink)));
        }
    }

    @Override
    public PageData<Testing> findTestingsByTenantIdAndType(UUID tenantId, String sensorType, PageLink pageLink) {
        return DaoUtil.toPageData(
                testingRepository.findByTenantIdAndType(
                        tenantId,
                        sensorType,

                        DaoUtil.toPageable(pageLink)));
    }


    @Override
    public Testing findTestingByTenantIdAndId(TenantId tenantId, UUID id) {
        return DaoUtil.getData(testingRepository.findByTenantIdAndId(tenantId.getId(), id));
    }



}
