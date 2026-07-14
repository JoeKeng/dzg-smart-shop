package com.dzg.system.domain.convert;

import io.github.linpeilie.BaseMapper;
import com.dzg.system.api.domain.vo.RemoteDictTypeVo;
import com.dzg.system.domain.vo.SysDictTypeVo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * 字典类型转换器
 *
 * @author liyaoheng
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysDictTypeVoConvert extends BaseMapper<SysDictTypeVo, RemoteDictTypeVo> {

}
