package com.atguigu.lease.web.admin.schedule;

import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.web.admin.service.LeaseAgreementService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ScheduleTask {

//    //每隔5秒执行一次
//    //注解+cron表达式
//    @Scheduled(cron = "0/5 * * * * ?")
//    public void test1() {
//        System.out.println(new Date().toLocaleString());
//    }
//}

    @Autowired
    private LeaseAgreementService leaseAgreementService;

    //每天晚上八点，查询哪些出租过期了，更新租约状态
    @Scheduled(cron = "0 0 20 * * ?")
    public void updateLeaseStatus() {
        LambdaUpdateWrapper<LeaseAgreement> wrapper=new LambdaUpdateWrapper<>();
        wrapper.in(LeaseAgreement::getStatus, LeaseStatus.SIGNED,LeaseStatus.WITHDRAWING);
        wrapper.lt(LeaseAgreement::getLeaseEndDate,new Date());
        wrapper.set(LeaseAgreement::getStatus,LeaseStatus.EXPIRED);
        leaseAgreementService.update(wrapper);
    }
}