package heyso.HeysoDiaryBackEnd.aiTemplate.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiRuntimeProfile;

@Mapper
public interface AiRuntimeProfileMapper {

    List<AiRuntimeProfile> selectList(@Param("status") String status, @Param("domainType") String domainType);

    AiRuntimeProfile selectById(@Param("runtimeProfileId") Long runtimeProfileId);

    void insert(AiRuntimeProfile profile);

    void update(AiRuntimeProfile profile);

    void updateIsActive(@Param("runtimeProfileId") Long runtimeProfileId, @Param("isActive") int isActive, @Param("updatedId") Long updatedId);
}
