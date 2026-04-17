package heyso.HeysoDiaryBackEnd.userMng.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserDetailResponse;
import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserDetailRow;
import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserListResponse;
import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserListRow;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AdminUserResponseMapper {

    List<AdminUserListResponse> toListResponses(List<AdminUserListRow> rows);

    AdminUserDetailResponse toDetailResponse(AdminUserDetailRow row);
}
