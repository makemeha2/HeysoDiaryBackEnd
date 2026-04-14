package heyso.HeysoDiaryBackEnd.aiTemplate.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiPromptTemplateRel;

@Mapper
public interface AiPromptTemplateRelMapper {

    List<AiPromptTemplateRel> selectByParentId(@Param("parentTemplateId") Long parentTemplateId);

    void insert(AiPromptTemplateRel rel);

    void updateIsActive(@Param("relId") Long relId, @Param("isActive") int isActive, @Param("updatedId") Long updatedId);
}
