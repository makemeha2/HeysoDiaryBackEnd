package heyso.HeysoDiaryBackEnd.comCd.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.comCd.model.CommonCode;
import heyso.HeysoDiaryBackEnd.comCd.model.CommonCodeGroup;

@Mapper
public interface CommonCodeMapper {
    /* ------------------------------ 관리자: 그룹 CRUD ------------------------------ */
    List<CommonCodeGroup> selectCommonCodeGroupListForAdmin();

    CommonCodeGroup selectCommonCodeGroupById(@Param("groupId") String groupId);

    int insertCommonCodeGroup(CommonCodeGroup commonCodeGroup);

    int updateCommonCodeGroup(CommonCodeGroup commonCodeGroup);

    int deleteCommonCodeGroup(@Param("groupId") String groupId,
            @Param("updatedId") Long updatedId);

    /* ------------------------------ 관리자: 코드 CRUD ------------------------------ */
    List<CommonCode> selectCommonCodeListForAdmin(@Param("groupId") String groupId);

    CommonCode selectCommonCodeById(@Param("groupId") String groupId,
            @Param("codeId") String codeId);

    int insertCommonCode(CommonCode commonCode);

    int updateCommonCode(CommonCode commonCode);

    int deleteCommonCode(@Param("groupId") String groupId,
            @Param("codeId") String codeId,
            @Param("updatedId") Long updatedId);

    /* ------------------------------ 사용자: 활성 코드 조회 ------------------------------ */
    List<CommonCodeGroup> selectActiveCommonCodeGroupList();

    List<CommonCode> selectActiveCommonCodeListByGroupId(@Param("groupId") String groupId);
}
