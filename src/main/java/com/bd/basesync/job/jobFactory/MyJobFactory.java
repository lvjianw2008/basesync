package com.bd.basesync.job.jobFactory;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.quartz.AdaptableJobFactory;
import org.springframework.stereotype.Component;

/**
 * Description: 自定义JobFactory,使用Spring容器管理的Quartz的Bean(Job)
 * <p/>
 * AdaptableJobFactory是Spring提供的SchedulerFactoryBean的默认实例化工厂，将由直接实例化Job，没有被Spring管理
 * User: lishaohua
 * Date: 2017/11/15  13:54
 */
@Component
public class MyJobFactory extends AdaptableJobFactory {

    /**
     * AutowireCapableBeanFactory接口是BeanFactory的子类
     * 可以连接和填充那些生命周期不被Spring管理的已存在的bean实例
     * 具体请参考：http://blog.csdn.net/iycynna_123/article/details/52993542
     */
    @Autowired
    private AutowireCapableBeanFactory capableBeanFactory;

    /**
     * 创建Job实例
     *
     * @param bundle
     * @return
     * @throws Exception
     */
    @Override
    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
        // 实例化对象
        Object jobInstance = super.createJobInstance(bundle);
        // 进行注入（Spring管理该Bean）
        capableBeanFactory.autowireBean(jobInstance);
        //返回对象
        return jobInstance;
    }
}