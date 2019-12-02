package org.dragonli.service.modules.accountmanagerservice.executor;

import org.dragonli.service.modules.accountservice.entity.enums.AccountsStatus;
import org.dragonli.service.modules.accountservice.entity.models.AccountEntity;
import org.dragonli.service.modules.accountservice.entity.models.AssetEntity;
import org.dragonli.service.modules.accountservice.repository.AccountsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class AccountCreateExecutor {

    @Autowired
    AccountsRepository accountsRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long createChildAccount(Long userId, String reflexId, AssetEntity asset) {
        AccountEntity userAccount = accountsRepository.findByUserIdAndReflexIdAndCurrency(
                userId, reflexId, asset.getCurrency());
        if (userAccount == null) {
            userAccount = new AccountEntity();
            userAccount.setStatus(AccountsStatus.FICTITIOUS);
            userAccount.setAssetId(asset.getId());
            userAccount.setCurrency(asset.getCurrency());
            userAccount.setUserId(userId);
            userAccount.setReflexId(reflexId);
            userAccount.setBalance(BigDecimal.ZERO);
            userAccount.setFrozen(BigDecimal.ZERO);
            userAccount.setOverdraft(BigDecimal.ZERO);
            userAccount.setAccountVersion(0);
            userAccount = accountsRepository.save(userAccount);
        }

        return userAccount.getId();
    }

}
