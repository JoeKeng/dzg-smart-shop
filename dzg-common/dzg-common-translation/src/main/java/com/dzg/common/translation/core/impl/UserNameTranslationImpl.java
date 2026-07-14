package com.dzg.common.translation.core.impl;

import cn.hutool.core.convert.Convert;
import com.dzg.common.core.constant.CacheNames;
import com.dzg.common.core.utils.StringUtils;
import com.dzg.common.redis.utils.CacheUtils;
import com.dzg.common.translation.annotation.TranslationType;
import com.dzg.common.translation.constant.TransConstant;
import com.dzg.common.translation.core.TranslationInterface;
import com.dzg.system.api.RemoteUserService;
import lombok.AllArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;

/**
 * 用户名翻译实现
 *
 * @author Lion Li
 */
@AllArgsConstructor
@TranslationType(type = TransConstant.USER_ID_TO_NAME)
public class UserNameTranslationImpl implements TranslationInterface<String> {

    @DubboReference
    private RemoteUserService remoteUserService;

    @Override
    public String translation(Object key, String other) {
        Long userId = Convert.toLong(key);
        String username = CacheUtils.get(CacheNames.SYS_USER_NAME, userId);
        if (StringUtils.isNotBlank(username)) {
            return username;
        }
        return remoteUserService.selectUserNameById(userId);
    }
}
