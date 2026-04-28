package heyso.HeysoDiaryBackEnd.aiTemplate.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiPromptBinding;

@Mapper
public interface AiPromptBindingMapper {

    List<AiPromptBinding> selectList(@Param("status") String status, @Param("domainType") String domainType);

    AiPromptBinding selectById(@Param("bindingId") Long bindingId);

    AiPromptBinding selectByDomainAndFeature(@Param("domainType") String domainType, @Param("featureKey") String featureKey);

    void insert(AiPromptBinding binding);

    void update(AiPromptBinding binding);

    void updateIsActive(@Param("bindingId") Long bindingId, @Param("isActive") int isActive, @Param("updatedId") Long updatedId);
}
