package org.dragonli.service.modules.accountmanagerservice;

import com.alibaba.dubbo.config.annotation.Reference;
import org.dragonli.service.modules.account.interfaces.AccountChangeService;
import org.dragonli.service.modules.account.interfaces.AccountManagerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AccountManagerApplication.class)
public class AccountManagerServiceTest extends AbstractTransactionalJUnit4SpringContextTests {
    @Reference
    AccountManagerService accountManagerService;
    @Reference
    AccountChangeService accountChangeService;

    @Test
    @Rollback(false)
    public void testAdjustment1() throws Exception {

        System.out.println("0 accountChangeService == null ?" + (accountChangeService == null));

//        accountManagerService
        String orderId = "adjust-admin-yy-zz-" + (new Random()).nextInt(1000000);
        System.out.println("==== order id: " + orderId + " ====");
        accountManagerService.accountAdjustment(orderId, 1L, "GENERAL_RECEIVE", "CNY", BigDecimal.TEN,
                "adjustment-to-admin-unit-test");
        String status = null;
        for (int i = 0; i < 100; i++) {
            Thread.sleep(32L);
            Map<String, Object> result = accountManagerService.adjustmentStatus(orderId);
            status = result.get("status").toString();
            if ((Boolean) result.get("isFinish")) break;
        }
        System.out.println("===status is :===" + status);
    }

    @Test
    @Rollback(false)
    public void testAdjustment2() throws Exception {

        System.out.println("0 accountChangeService == null ?" + (accountChangeService == null));

//        accountManagerService
        String orderId = "adjust-user-yy-zz-" + (new Random()).nextInt(1000000);
        System.out.println("==== order id: " + orderId + " ====");
        accountManagerService.accountAdjustment(orderId, 2L, "", "CNY", new BigDecimal("5.5"),
                "adjustment-to-user-unit-test");
        String status = null;
        for (int i = 0; i < 100; i++) {
            Thread.sleep(32L);
            Map<String, Object> result = accountManagerService.adjustmentStatus(orderId);
            status = result.get("status").toString();
            if ((Boolean) result.get("isFinish")) break;
        }
        System.out.println("===status is :===" + status);
    }

    @Test
    @Rollback(false)
    public void testPayment() throws Exception {

        System.out.println("0 accountChangeService == null ?" + (accountChangeService == null));

//        accountManagerService
        String orderId = "pay-user-yy-zz-" + (new Random()).nextInt(1000000);
        System.out.println("==== order id: " + orderId + " ====");
        Object r = accountManagerService.payment(2L, "", "GENERAL_RECEIVE", new BigDecimal("1.5"), "CNY", orderId,
                "adjustment-to-user-unit-test", false);
        if(r == null){
            System.out.println("balance not enough");
            return;
        }
        String status = null;
        for (int i = 0; i < 100; i++) {
            Thread.sleep(32L);
            Map<String, Object> result = accountManagerService.paymentStatus(orderId);
            status = result.get("status").toString();
            if ((Boolean) result.get("isFinish")) break;
        }
        System.out.println("===status is :===" + status);
    }

//    public  static void main(String[] args){
//        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("bootstrap.yml");
//
//        context.start();
//
//        UserService userService = (UserService) context.getBean(UserService.class);
//        System.out.println(userService==null);
//    }
}