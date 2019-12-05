package org.dragonli.service.modules.accountmanagerservice.executor;

import org.dragonli.service.modules.accountmanagerservice.dto.FundFlowDto;
import org.dragonli.service.modules.accountservice.entity.enums.BusinessFlowType;
import org.dragonli.service.modules.accountservice.entity.models.AccountAdjustmentEntity;
import org.dragonli.service.modules.accountservice.entity.models.BusinessEntity;
import org.dragonli.service.modules.accountservice.repository.AccountAdjustmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class AdjustmentExecutor {
    @Autowired
    AccountAdjustmentRepository accountAdjustmentRepository;
    @Autowired
    BusinessExecutor businessExecutor;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AccountAdjustmentEntity saveOne(AccountAdjustmentEntity accountAdjustmentEntity) throws Exception {
        return accountAdjustmentRepository.save(accountAdjustmentEntity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BusinessEntity createBusinessForAdjustment(Long id) throws Exception {
        AccountAdjustmentEntity accountAdjustmentEntity = accountAdjustmentRepository.getOne(id);
        List<FundFlowDto> funds = new ArrayList<>(1);
        funds.add(new FundFlowDto(accountAdjustmentEntity.getAccountId(), accountAdjustmentEntity.getFlowAmount()));
        BusinessEntity businessEntity = businessExecutor.createBusiness(accountAdjustmentEntity.getOrderId(), BusinessFlowType.accountAdjustment,
                accountAdjustmentEntity.getId(), funds,accountAdjustmentEntity.getRemark());

        accountAdjustmentEntity.setBusinessId(businessEntity.getId());
        accountAdjustmentRepository.save(accountAdjustmentEntity);

        return businessEntity;
    }
}
