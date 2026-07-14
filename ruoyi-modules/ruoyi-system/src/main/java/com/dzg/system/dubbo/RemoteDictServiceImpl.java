package com.dzg.system.dubbo;

import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import com.dzg.common.core.utils.MapstructUtils;
import com.dzg.system.api.RemoteDictService;
import com.dzg.system.api.domain.vo.RemoteDictDataVo;
import com.dzg.system.api.domain.vo.RemoteDictTypeVo;
import com.dzg.system.domain.vo.SysDictDataVo;
import com.dzg.system.domain.vo.SysDictTypeVo;
import com.dzg.system.service.ISysDictTypeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典服务
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
@DubboService
public class RemoteDictServiceImpl implements RemoteDictService {

    private final ISysDictTypeService sysDictTypeService;

    /**
     * remote根据字典类型查询字典
     *
     * @param dictType 字典类型
     * @return RemoteDictTypeVo
     * @see com.dzg.system.domain.convert.SysDictTypeVoConvert
     */
    @Override
    public RemoteDictTypeVo selectDictTypeByType(String dictType) {
        SysDictTypeVo vo = sysDictTypeService.selectDictTypeByType(dictType);
        return MapstructUtils.convert(vo, RemoteDictTypeVo.class);
    }

    /**
     * remote根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     * @see com.dzg.system.domain.convert.SysDictDataVoConvert
     */
    @Override
    public List<RemoteDictDataVo> selectDictDataByType(String dictType) {
        List<SysDictDataVo> list = sysDictTypeService.selectDictDataByType(dictType);
        return MapstructUtils.convert(list, RemoteDictDataVo.class);
    }

}
