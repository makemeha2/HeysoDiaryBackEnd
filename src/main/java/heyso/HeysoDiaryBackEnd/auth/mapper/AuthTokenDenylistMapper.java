package heyso.HeysoDiaryBackEnd.auth.mapper;

import java.time.LocalDateTime;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.auth.model.AuthTokenDenylistEntry;

@Mapper
public interface AuthTokenDenylistMapper {
    int insertIgnore(AuthTokenDenylistEntry entry);

    boolean existsActiveJti(
            @Param("jti") String jti,
            @Param("now") LocalDateTime now);

    int deleteExpired(@Param("now") LocalDateTime now);
}
