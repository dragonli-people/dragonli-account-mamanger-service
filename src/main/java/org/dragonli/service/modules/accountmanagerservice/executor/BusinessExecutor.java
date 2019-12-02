package org.dragonli.service.modules.accountmanagerservice.executor;

import com.alibaba.fastjson.JSON;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.dragonli.service.modules.account.interfaces.AccountChangeService;
import org.dragonli.service.modules.accountmanagerservice.dto.FundFlowDto;
import org.dragonli.service.modules.accountservice.entity.enums.BusinessFlowType;
import org.dragonli.service.modules.accountservice.entity.enums.BusinessStatus;
import org.dragonli.service.modules.accountservice.entity.enums.CurrencyType;
import org.dragonli.service.modules.accountservice.entity.enums.EvidenceStatus;
import org.dragonli.service.modules.accountservice.entity.models.AccountEntity;
import org.dragonli.service.modules.accountservice.entity.models.AssetEntity;
import org.dragonli.service.modules.accountservice.entity.models.BusinessEntity;
import org.dragonli.service.modules.accountservice.entity.models.FundFlowEvidenceEntity;
import org.dragonli.service.modules.accountservice.repository.AccountsRepository;
import org.dragonli.service.modules.accountservice.repository.AssetRepository;
import org.dragonli.service.modules.accountservice.repository.BusinessRepository;
import org.dragonli.service.modules.accountservice.repository.FundFlowEvidenceRepository;
import org.dragonli.service.modules.accountservice.utils.FundFlowEvidenceTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Component
public class BusinessExecutor {
    @Autowired
    BusinessRepository businessRepository;
    @Autowired
    AccountsRepository accountsRepository;
    @Autowired
    AssetRepository assetRepository;
    @Autowired
    FundFlowEvidenceRepository fundFlowEvidenceRepository;
    @Reference
    AccountChangeService accountService;

    @Autowired
    FundFlowEvidenceTool fundFlowEvidenceTool;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BusinessEntity createBusiness(String orderId, BusinessFlowType flowType, Long referenceId,
            List<FundFlowDto> funds) {

        BusinessEntity business = businessRepository.findByOrderId(orderId);
        if (business != null) return business;
        business = new BusinessEntity();

        business.setCurrentStep(1);
        business.setType(flowType);
        business.setSteps(funds.size());
        business.setStepParameter(JSON.toJSONString(funds));
        business.setOrderId(orderId);
        business.setReferenceId(referenceId);
        business.setStatus(BusinessStatus.INIT);
        business.setCreatedAt(System.currentTimeMillis());
        business.setUpdatedAt(System.currentTimeMillis());
        business.setVersion(0);
        business = businessRepository.save(business);

        List<FundFlowEvidenceEntity> evidences = new ArrayList<>(funds.size());
        int step = 0;
        for (FundFlowDto stepInfo : funds) {
            Long accountId = stepInfo.getAccountId();
            BigDecimal amount = stepInfo.getAmount();
            AccountEntity account = accountsRepository.get(accountId);
            AssetEntity asset = assetRepository.get(account.getAssetId());
            step++;

            FundFlowEvidenceEntity fund = fundFlowEvidenceTool.initFundFlowEvidenceEntity(asset.getCurrency(), business.getId(),accountId
                    ,amount, business.getOrderId(),step++,true);
//            FundFlowEvidenceEntity fund = new FundFlowEvidenceEntity();
//            fund.setAccountId(accountId);
//            fund.setFlowAmount(amount);
//            fund.setTimeout(System.currentTimeMillis()+3000L);
//            fund.setUserId(account.getUserId());
//            fund.setCurrency(asset.getCurrency());
//            fund.setFlowStatus(EvidenceStatus.INIT);
//            fund.setBusinessId(business.getId());
//
//            fund.setStep(step);
//            fund.setOrderId(business.getOrderId() + "@" + step);
//
//            fund = fundFlowEvidenceRepository.save(fund);
            evidences.add(fund);
        }

//        accountService.addChangeRecord(keep.getId());
        return business;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean beginFundForBusiness(Long id) throws Exception {
        BusinessEntity business = businessRepository.get(id);
        List<FundFlowEvidenceEntity> list = fundFlowEvidenceRepository.findByBusinessIdAndStepAfter(id, 0);
        FundFlowEvidenceEntity first = list.stream().filter(v -> !v.getFlowStatus().isFinalStatus).sorted(
                Comparator.comparing(FundFlowEvidenceEntity::getStep)).findFirst().orElse(null);
        if( first == null ) return false;
        accountService.addChangeRecord(first.getId());
        return true;
    }
}
