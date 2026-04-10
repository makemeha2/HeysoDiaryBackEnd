package heyso.HeysoDiaryBackEnd.aiTemplate.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiPromptTemplate;

@Mapper
public interface AiPromptTemplateMapper {

    List<AiPromptTemplate> selectList(@Param("status") String status, @Param("templateType") String templateType, @Param("domainType") String domainType);

    AiPromptTemplate selectById(@Param("templateId") Long templateId);

    AiPromptTemplate selectByKey(@Param("templateKey") String templateKey);

    void insert(AiPromptTemplate template);

    void update(AiPromptTemplate template);

    void updateIsActive(@Param("templateId") Long templateId, @Param("isActive") int isActive, @Param("updatedId") Long updatedId);
}
